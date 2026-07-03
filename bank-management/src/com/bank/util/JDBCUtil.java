package com.bank.util;

import java.sql.*;

/**
 * 数据库连接工具类
 * 管理SQL Server数据库连接
 */
public class JDBCUtil {
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    // SQL Server认证方式
    private static final String USERNAME = System.getenv("BANK_DB_USERNAME") != null ?
        System.getenv("BANK_DB_USERNAME") : "bankadmin";
    private static final String PASSWORD = System.getenv("BANK_DB_PASSWORD") != null ?
        System.getenv("BANK_DB_PASSWORD") : "your_password_here";
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=bank_management;" +
        "encrypt=false;" +
        "trustServerCertificate=true;" +
        "useUnicode=true;" +
        "characterEncoding=UTF-8";

    static {
        try {
            Class.forName(DRIVER);
            System.out.println("[OK] SQL Server JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] SQL Server JDBC驱动加载失败: " + e.getMessage());
            System.err.println("请确保已将 mssql-jdbc 的jar包添加到classpath");
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.err.println("[ERROR] 数据库连接失败: " + e.getMessage());
            System.err.println("请检查：");
            System.err.println("  1. SQL Server服务是否启动");
            System.err.println("  2. TCP/IP协议是否启用（端口1433）");
            System.err.println("  3. 用户名密码是否正确（用户: " + USERNAME + "）");
            System.err.println("  4. 数据库 bank_management 是否存在");
            return null;
        }
    }

    /**
     * 获取连接（指定数据库名，用于创建数据库前连接）
     */
    public static Connection getMasterConnection() {
        String masterUrl =
            "jdbc:sqlserver://localhost:1433;" +
            "encrypt=false;" +
            "trustServerCertificate=true;" +
            "useUnicode=true;" +
            "characterEncoding=UTF-8";
        try {
            return DriverManager.getConnection(masterUrl, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[ERROR] 连接master数据库失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 关闭数据库资源
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {
            System.err.println("[WARN] 关闭ResultSet失败: " + e.getMessage());
        }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) {
            System.err.println("[WARN] 关闭Statement失败: " + e.getMessage());
        }
        try { if (conn != null) conn.close(); } catch (SQLException e) {
            System.err.println("[WARN] 关闭Connection失败: " + e.getMessage());
        }
    }
}
