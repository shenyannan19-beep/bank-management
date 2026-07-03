package com.bank.util;

import java.sql.*;
import java.io.*;
import java.nio.file.*;

/**
 * SQL脚本自动执行器
 * 读取SQL文件并按GO分隔符批量执行，支持错误容错
 */
public class SQLExecutor {

    /**
     * 执行SQL文件
     * @param filePath SQL文件的路径
     */
    public static void executeSqlFile(String filePath) {
        Connection conn = null;
        Statement stmt = null;

        try {
            // 先尝试连接master数据库来执行建库脚本
            conn = JDBCUtil.getMasterConnection();
            if (conn == null) {
                // 回退到直接连接bank_management
                conn = JDBCUtil.getConnection();
            }
            if (conn == null) {
                System.err.println("[ERROR] 数据库连接失败！无法执行SQL脚本。");
                System.err.println("请检查以下配置：");
                System.err.println("  1. SQL Server服务是否启动（net start MSSQLSERVER）");
                System.err.println("  2. TCP/IP协议是否启用（端口1433）");
                System.err.println("  3. sa用户名密码是否正确");
                System.err.println("  4. 是否已安装SQL Server");
                return;
            }

            System.out.println("正在读取SQL文件: " + filePath);
            String sql = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

            // 按GO关键字分割批处理（支持GO前后有空白）
            String[] batches = sql.split("(?i)\\nGO\\b|\\nGO\\s*\\n|\\nGO$");

            stmt = conn.createStatement();
            int successCount = 0;
            int failCount = 0;
            int totalBatches = 0;

            for (String batch : batches) {
                batch = batch.trim();

                // 跳过纯注释块和空块
                if (batch.isEmpty() || batch.startsWith("--") && !batch.contains("\n")) {
                    continue;
                }

                // 移除行内注释前的内容检查
                String checkBatch = batch.replaceAll("--[^\n]*", "").trim();
                if (checkBatch.isEmpty()) continue;

                totalBatches++;

                // 获取预览文本
                String preview = batch.replace('\n', ' ').replace('\r', ' ').trim();
                if (preview.length() > 60) preview = preview.substring(0, 60) + "...";

                try {
                    stmt.execute(batch);
                    successCount++;
                    System.out.println("  [OK] " + preview);
                } catch (SQLException e) {
                    failCount++;
                    String errMsg = e.getMessage();
                    // 某些错误可以忽略（如USE语句在没有数据库时执行）
                    if (errMsg.contains("database 'bank_management' does not exist") ||
                        errMsg.contains("Database 'bank_management' does not exist")) {
                        System.out.println("  [SKIP] 数据库尚未创建，跳过: " + preview);
                    } else {
                        System.err.println("  [FAIL] " + errMsg);
                        System.err.println("         SQL: " + preview);
                    }
                }
            }

            System.out.println();
            System.out.println("========================================");
            System.out.println("  SQL脚本执行完成！");
            System.out.println("  成功: " + successCount + " 批");
            System.out.println("  失败: " + failCount + " 批");
            System.out.println("  总计: " + totalBatches + " 批");
            System.out.println("========================================");

        } catch (IOException e) {
            System.err.println("[ERROR] 读取SQL文件失败: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL执行异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            JDBCUtil.close(null, stmt, conn);
        }
    }

    /**
     * 使用sqlcmd命令行工具执行SQL（备用方案）
     */
    public static void executeViaSqlCmd(String filePath) {
        System.out.println("尝试使用 sqlcmd 执行SQL文件: " + filePath);
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "sqlcmd", "-S", "localhost", "-U", "sa", "-P", "YourPassword123",
                "-i", filePath, "-f", "65001"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("sqlcmd 退出码: " + exitCode);
        } catch (Exception e) {
            System.err.println("[ERROR] sqlcmd执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  银行管理系统 - 数据库初始化工具");
        System.out.println("========================================");
        System.out.println();

        // 步骤1：执行建表脚本
        System.out.println("[Step 1] 正在执行建表脚本...");
        System.out.println("----------------------------------------");
        executeSqlFile("sql/setup_database.sql");
        System.out.println();

        // 步骤2：执行数据插入脚本
        System.out.println("[Step 2] 正在导入测试数据...");
        System.out.println("----------------------------------------");
        executeSqlFile("sql/insert_data.sql");
        System.out.println();

        System.out.println("========================================");
        System.out.println("  数据库初始化全部完成！");
        System.out.println("========================================");
    }
}
