package com.bank.dao;

import com.bank.entity.Account;
import com.bank.util.JDBCUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户数据访问对象
 * 包含存款、取款、转账等事务操作，以及存储过程调用
 */
public class AccountDAO {

    /**
     * 开户
     */
    public boolean openAccount(Account account) {
        String sql = "INSERT INTO Account (account_id, customer_id, account_type, balance, status, open_date, branch) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account.getAccountId());
            pstmt.setString(2, account.getCustomerId());
            pstmt.setString(3, account.getAccountType());
            pstmt.setBigDecimal(4, account.getBalance());
            pstmt.setString(5, account.getStatus() != null ? account.getStatus() : "正常");
            pstmt.setDate(6, new java.sql.Date(account.getOpenDate().getTime()));
            pstmt.setString(7, account.getBranch());

            int rows = pstmt.executeUpdate();
            System.out.println("  [OK] 开户成功，账号: " + account.getAccountId());
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("  [ERROR] 开户失败: " + e.getMessage());
            return false;
        } finally {
            JDBCUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 销户
     */
    public boolean closeAccount(String accountId) {
        String sql = "UPDATE Account SET status = N'销户' WHERE account_id = ? AND status = N'正常'";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("  [OK] 销户成功，账号: " + accountId);
            } else {
                System.out.println("  [WARN] 销户失败：账户不存在或已销户");
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("  [ERROR] 销户失败: " + e.getMessage());
            return false;
        } finally {
            JDBCUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 存款操作（使用事务 + ROWLOCK）
     * 隔离级别: TRANSACTION_READ_COMMITTED
     */
    public boolean deposit(String accountId, BigDecimal amount) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            // 设置事务隔离级别
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            // 1. 检查账户是否存在且状态正常（使用ROWLOCK）
            String checkSql = "SELECT balance, status FROM Account WITH (ROWLOCK) " +
                             "WHERE account_id = ? AND status = N'正常'";
            pstmtCheck = conn.prepareStatement(checkSql);
            pstmtCheck.setString(1, accountId);
            rs = pstmtCheck.executeQuery();

            if (!rs.next()) {
                System.err.println("  [ERROR] 账户不存在或状态异常: " + accountId);
                conn.rollback();
                return false;
            }

            // 2. 更新余额
            String updateSql = "UPDATE Account SET balance = balance + ? WHERE account_id = ?";
            pstmtUpdate = conn.prepareStatement(updateSql);
            pstmtUpdate.setBigDecimal(1, amount);
            pstmtUpdate.setString(2, accountId);
            pstmtUpdate.executeUpdate();

            // 3. 插入交易记录
            String transId = generateTransId(conn);
            String insertSql = "INSERT INTO TransactionRecord " +
                "(trans_id, account_id, trans_type, amount, trans_date, trans_time, remark, operator_id) " +
                "VALUES (?, ?, N'存款', ?, CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), N'柜台存款', N'T0007')";
            pstmtInsert = conn.prepareStatement(insertSql);
            pstmtInsert.setString(1, transId);
            pstmtInsert.setString(2, accountId);
            pstmtInsert.setBigDecimal(3, amount);
            pstmtInsert.executeUpdate();

            conn.commit();
            System.out.println("  [OK] 存款成功！账号: " + accountId +
                             ", 金额: " + amount + ", 交易编号: " + transId);
            return true;

        } catch (SQLException e) {
            System.err.println("  [ERROR] 存款失败: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            JDBCUtil.close(rs, pstmtCheck, null);
            JDBCUtil.close(null, pstmtUpdate, null);
            JDBCUtil.close(null, pstmtInsert, conn);
        }
    }

    /**
     * 取款操作（使用事务 + ROWLOCK + 余额检查）
     * 隔离级别: TRANSACTION_READ_COMMITTED
     */
    public boolean withdraw(String accountId, BigDecimal amount) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            // 1. 检查余额（ROWLOCK）
            String checkSql = "SELECT balance, status FROM Account WITH (ROWLOCK) " +
                             "WHERE account_id = ? AND status = N'正常'";
            pstmtCheck = conn.prepareStatement(checkSql);
            pstmtCheck.setString(1, accountId);
            rs = pstmtCheck.executeQuery();

            if (!rs.next()) {
                System.err.println("  [ERROR] 账户不存在或状态异常: " + accountId);
                conn.rollback();
                return false;
            }

            BigDecimal balance = rs.getBigDecimal("balance");
            if (balance.compareTo(amount) < 0) {
                System.err.println("  [ERROR] 余额不足！当前余额: " + balance + ", 取款金额: " + amount);
                conn.rollback();
                return false;
            }

            // 2. 扣减余额
            String updateSql = "UPDATE Account SET balance = balance - ? WHERE account_id = ?";
            pstmtUpdate = conn.prepareStatement(updateSql);
            pstmtUpdate.setBigDecimal(1, amount);
            pstmtUpdate.setString(2, accountId);
            pstmtUpdate.executeUpdate();

            // 3. 插入交易记录
            String transId = generateTransId(conn);
            String insertSql = "INSERT INTO TransactionRecord " +
                "(trans_id, account_id, trans_type, amount, trans_date, trans_time, remark, operator_id) " +
                "VALUES (?, ?, N'取款', ?, CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), N'柜台取款', N'T0007')";
            pstmtInsert = conn.prepareStatement(insertSql);
            pstmtInsert.setString(1, transId);
            pstmtInsert.setString(2, accountId);
            pstmtInsert.setBigDecimal(3, amount);
            pstmtInsert.executeUpdate();

            conn.commit();
            System.out.println("  [OK] 取款成功！账号: " + accountId +
                             ", 金额: " + amount + ", 交易编号: " + transId);
            return true;

        } catch (SQLException e) {
            System.err.println("  [ERROR] 取款失败: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            JDBCUtil.close(rs, pstmtCheck, null);
            JDBCUtil.close(null, pstmtUpdate, null);
            JDBCUtil.close(null, pstmtInsert, conn);
        }
    }

    /**
     * 转账操作（使用事务 + SERIALIZABLE + UPDLOCK,ROWLOCK + 4条SQL）
     * 隔离级别: TRANSACTION_SERIALIZABLE
     */
    public boolean transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        Connection conn = null;
        PreparedStatement pstmtCheckFrom = null;
        PreparedStatement pstmtCheckTo = null;
        PreparedStatement pstmtDeduct = null;
        PreparedStatement pstmtAdd = null;
        PreparedStatement pstmtInsertFrom = null;
        PreparedStatement pstmtInsertTo = null;
        ResultSet rsFrom = null;
        ResultSet rsTo = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            // 设置最高隔离级别保证数据一致性
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // 1. 锁定转出账户并检查余额
            String checkFromSql = "SELECT balance, status FROM Account WITH (UPDLOCK, ROWLOCK) " +
                                 "WHERE account_id = ? AND status = N'正常'";
            pstmtCheckFrom = conn.prepareStatement(checkFromSql);
            pstmtCheckFrom.setString(1, fromAccountId);
            rsFrom = pstmtCheckFrom.executeQuery();

            if (!rsFrom.next()) {
                System.err.println("  [ERROR] 转出账户不存在或状态异常: " + fromAccountId);
                conn.rollback();
                return false;
            }

            BigDecimal fromBalance = rsFrom.getBigDecimal("balance");
            if (fromBalance.compareTo(amount) < 0) {
                System.err.println("  [ERROR] 转出账户余额不足！当前余额: " + fromBalance + ", 转账金额: " + amount);
                conn.rollback();
                return false;
            }

            // 2. 锁定转入账户
            String checkToSql = "SELECT status FROM Account WITH (UPDLOCK, ROWLOCK) " +
                               "WHERE account_id = ? AND status = N'正常'";
            pstmtCheckTo = conn.prepareStatement(checkToSql);
            pstmtCheckTo.setString(1, toAccountId);
            rsTo = pstmtCheckTo.executeQuery();

            if (!rsTo.next()) {
                System.err.println("  [ERROR] 转入账户不存在或状态异常: " + toAccountId);
                conn.rollback();
                return false;
            }

            // 3. 扣减转出账户余额
            String deductSql = "UPDATE Account SET balance = balance - ? WHERE account_id = ?";
            pstmtDeduct = conn.prepareStatement(deductSql);
            pstmtDeduct.setBigDecimal(1, amount);
            pstmtDeduct.setString(2, fromAccountId);
            pstmtDeduct.executeUpdate();

            // 4. 增加转入账户余额
            String addSql = "UPDATE Account SET balance = balance + ? WHERE account_id = ?";
            pstmtAdd = conn.prepareStatement(addSql);
            pstmtAdd.setBigDecimal(1, amount);
            pstmtAdd.setString(2, toAccountId);
            pstmtAdd.executeUpdate();

            // 5. 插入转出交易记录
            String transIdOut = generateTransId(conn);
            String insertOutSql = "INSERT INTO TransactionRecord " +
                "(trans_id, account_id, trans_type, amount, trans_date, trans_time, remark, operator_id) " +
                "VALUES (?, ?, N'转账', ?, CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), " +
                "N'转账至" + toAccountId + "', N'T0007')";
            pstmtInsertFrom = conn.prepareStatement(insertOutSql);
            pstmtInsertFrom.setString(1, transIdOut);
            pstmtInsertFrom.setString(2, fromAccountId);
            pstmtInsertFrom.setBigDecimal(3, amount);
            pstmtInsertFrom.executeUpdate();

            // 6. 插入转入交易记录
            String transIdIn = generateTransId(conn);
            String insertInSql = "INSERT INTO TransactionRecord " +
                "(trans_id, account_id, trans_type, amount, trans_date, trans_time, remark, operator_id) " +
                "VALUES (?, ?, N'存款', ?, CAST(GETDATE() AS DATE), CAST(GETDATE() AS TIME), " +
                "N'来自" + fromAccountId + "转账', N'T0007')";
            pstmtInsertTo = conn.prepareStatement(insertInSql);
            pstmtInsertTo.setString(1, transIdIn);
            pstmtInsertTo.setString(2, toAccountId);
            pstmtInsertTo.setBigDecimal(3, amount);
            pstmtInsertTo.executeUpdate();

            conn.commit();
            System.out.println("  [OK] 转账成功！从 " + fromAccountId + " 转出 " + amount +
                             " 到 " + toAccountId);
            System.out.println("       转出交易编号: " + transIdOut);
            System.out.println("       转入交易编号: " + transIdIn);
            return true;

        } catch (SQLException e) {
            System.err.println("  [ERROR] 转账失败: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            JDBCUtil.close(rsFrom, pstmtCheckFrom, null);
            JDBCUtil.close(rsTo, pstmtCheckTo, null);
            JDBCUtil.close(null, pstmtDeduct, null);
            JDBCUtil.close(null, pstmtAdd, null);
            JDBCUtil.close(null, pstmtInsertFrom, null);
            JDBCUtil.close(null, pstmtInsertTo, conn);
        }
    }

    /**
     * 根据账户ID查询
     */
    public Account getAccountById(String accountId) {
        String sql = "SELECT * FROM Account WHERE account_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return null;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractAccount(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] 查询账户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * 查询客户的所有账户
     */
    public List<Account> getAccountsByCustomer(String customerId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE customer_id = ? ORDER BY open_date";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return list;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] 查询客户账户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * 支行存款统计（聚合查询 + 调用存储过程）
     */
    public void printBranchSummary() {
        Connection conn = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;

            cstmt = conn.prepareCall("{call sp_branch_deposit_summary}");
            rs = cstmt.executeQuery();

            System.out.println();
            System.out.println("  ╔══════════════╦══════════╦══════════════════╦══════════════════╗");
            System.out.println("  ║   支行名称   ║ 账户数量 ║    存款总额      ║    平均余额      ║");
            System.out.println("  ╠══════════════╬══════════╬══════════════════╬══════════════════╣");

            while (rs.next()) {
                String branch = rs.getString("支行名称");
                int count = rs.getInt("账户数量");
                BigDecimal total = rs.getBigDecimal("存款总额");
                BigDecimal avg = rs.getBigDecimal("平均余额");

                System.out.printf("  ║ %-12s ║ %8d ║ %14.2f ║ %14.2f ║%n",
                        branch, count, total, avg);
            }
            System.out.println("  ╚══════════════╩══════════╩══════════════════╩══════════════════╝");

        } catch (SQLException e) {
            System.err.println("  [ERROR] 支行统计查询失败: " + e.getMessage());
            // 回退方案：直接SQL聚合查询
            fallbackBranchSummary(conn);
        } finally {
            JDBCUtil.close(rs, cstmt, conn);
        }
    }

    /**
     * 回退方案：直接使用SQL聚合查询
     */
    private void fallbackBranchSummary(Connection conn) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (conn == null) return;
            stmt = conn.createStatement();
            String sql = "SELECT branch, COUNT(*) AS cnt, SUM(balance) AS total, AVG(balance) AS avg_bal " +
                        "FROM Account WHERE status = N'正常' GROUP BY branch ORDER BY SUM(balance) DESC";
            rs = stmt.executeQuery(sql);

            System.out.println();
            System.out.println("  === 支行存款统计（直接查询） ===");
            while (rs.next()) {
                System.out.printf("  %-12s | 账户数: %3d | 总额: %12.2f | 平均: %10.2f%n",
                        rs.getString("branch"), rs.getInt("cnt"),
                        rs.getBigDecimal("total"), rs.getBigDecimal("avg_bal"));
            }
        } catch (SQLException e2) {
            System.err.println("  [ERROR] 回退查询也失败: " + e2.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, null);
        }
    }

    /**
     * 调用存储过程查询交易明细
     */
    public void callSpQueryTransactions(String accountId, String startDate, String endDate) {
        Connection conn = null;
        CallableStatement cstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;

            cstmt = conn.prepareCall("{call sp_query_transactions(?, ?, ?)}");
            cstmt.setString(1, accountId);
            cstmt.setString(2, startDate);
            cstmt.setString(3, endDate);
            rs = cstmt.executeQuery();

            System.out.println();
            System.out.println("  ╔══════════════╦══════════════╦══════════════════╦══════════════╦══════════════╦══════════════════════════╗");
            System.out.println("  ║   交易编号   ║   交易类型   ║      金额        ║   交易日期   ║   交易时间   ║         备注             ║");
            System.out.println("  ╠══════════════╬══════════════╬══════════════════╬══════════════╬══════════════╬══════════════════════════╣");

            while (rs.next()) {
                System.out.printf("  ║ %-12s ║ %-12s ║ %14.2f ║ %-12s ║ %-12s ║ %-20s ║%n",
                        rs.getString("交易编号"),
                        rs.getString("交易类型"),
                        rs.getBigDecimal("金额"),
                        rs.getDate("交易日期"),
                        rs.getTime("交易时间"),
                        rs.getString("备注") != null ? rs.getString("备注") : "");
            }
            System.out.println("  ╚══════════════╩══════════════╩══════════════════╩══════════════╩══════════════╩══════════════════════════╝");

        } catch (SQLException e) {
            System.err.println("  [ERROR] 存储过程调用失败: " + e.getMessage());
            // 回退方案：直接SQL查询
            fallbackTransactionQuery(accountId, startDate, endDate);
        } finally {
            JDBCUtil.close(rs, cstmt, conn);
        }
    }

    /**
     * 回退方案：直接SQL查询交易记录
     */
    private void fallbackTransactionQuery(String accountId, String startDate, String endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;

            String sql = "SELECT trans_id, trans_type, amount, trans_date, trans_time, remark " +
                        "FROM TransactionRecord WHERE account_id = ? AND trans_date BETWEEN ? AND ? " +
                        "ORDER BY trans_date DESC, trans_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            rs = pstmt.executeQuery();

            System.out.println();
            System.out.println("  === 交易明细（直接查询） ===");
            while (rs.next()) {
                System.out.printf("  %s | %s | %10.2f | %s | %s | %s%n",
                        rs.getString("trans_id"), rs.getString("trans_type"),
                        rs.getBigDecimal("amount"), rs.getDate("trans_date"),
                        rs.getTime("trans_time"), rs.getString("remark"));
            }
        } catch (SQLException e2) {
            System.err.println("  [ERROR] 回退查询也失败: " + e2.getMessage());
        } finally {
            JDBCUtil.close(rs, pstmt, conn);
        }
    }

    /**
     * 打印客户资产汇总（调用视图）
     */
    public void printCustomerAssetSummary() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM vw_customer_summary ORDER BY 总资产 DESC");

            System.out.println();
            System.out.println("  ╔══════════════╦══════════════╦══════════════╦══════════════╦══════════════════╦══════════════╗");
            System.out.println("  ║   客户编号   ║   客户姓名   ║   联系电话   ║   账户数量   ║     总资产       ║   所在城市   ║");
            System.out.println("  ╠══════════════╬══════════════╬══════════════╬══════════════╬══════════════════╬══════════════╣");

            while (rs.next()) {
                System.out.printf("  ║ %-12s ║ %-12s ║ %-12s ║ %12d ║ %14.2f ║ %-12s ║%n",
                        rs.getString("客户编号"),
                        rs.getString("客户姓名"),
                        rs.getString("联系电话"),
                        rs.getInt("账户数量"),
                        rs.getBigDecimal("总资产"),
                        rs.getString("所在城市"));
            }
            System.out.println("  ╚══════════════╩══════════════╩══════════════╩══════════════╩══════════════════╩══════════════╝");

        } catch (SQLException e) {
            System.err.println("  [ERROR] 视图查询失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    /**
     * 生成唯一的交易ID
     */
    private String generateTransId(Connection conn) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT 'T' + RIGHT('0000000000' + CAST(ISNULL(MAX(CAST(SUBSTRING(trans_id,2,10) AS BIGINT)),0)+1 AS VARCHAR),10) " +
                "FROM TransactionRecord WITH (TABLOCKX)");
            if (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            JDBCUtil.close(rs, stmt, null);
        }
        // 回退：使用时间戳
        return "T" + String.format("%010d", System.currentTimeMillis() % 10000000000L);
    }

    /**
     * 从ResultSet中提取Account对象
     */
    private Account extractAccount(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getString("account_id"));
        a.setCustomerId(rs.getString("customer_id"));
        a.setAccountType(rs.getString("account_type"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setStatus(rs.getString("status"));
        a.setOpenDate(rs.getDate("open_date"));
        a.setBranch(rs.getString("branch"));
        return a;
    }
}
