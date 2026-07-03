package com.bank.dao;

import com.bank.entity.Customer;
import com.bank.util.JDBCUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户数据访问对象
 * 提供客户的增删改查操作
 */
public class CustomerDAO {

    /**
     * 添加客户
     */
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO Customer (customer_id, customer_name, id_card, phone, city, address, open_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getCustomerId());
            pstmt.setString(2, customer.getCustomerName());
            pstmt.setString(3, customer.getIdCard());
            pstmt.setString(4, customer.getPhone());
            pstmt.setString(5, customer.getCity());
            pstmt.setString(6, customer.getAddress());
            pstmt.setDate(7, new java.sql.Date(customer.getOpenDate().getTime()));

            int rows = pstmt.executeUpdate();
            System.out.println("  [OK] 客户添加成功，影响行数: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("  [ERROR] 添加客户失败: " + e.getMessage());
            return false;
        } finally {
            JDBCUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 删除客户
     */
    public boolean deleteCustomer(String customerId) {
        String sql = "DELETE FROM Customer WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerId);

            int rows = pstmt.executeUpdate();
            System.out.println("  [OK] 客户删除成功，影响行数: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("  [ERROR] 删除客户失败: " + e.getMessage());
            return false;
        } finally {
            JDBCUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 更新客户信息
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customer SET customer_name=?, id_card=?, phone=?, city=?, address=? " +
                     "WHERE customer_id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return false;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customer.getCustomerName());
            pstmt.setString(2, customer.getIdCard());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getCity());
            pstmt.setString(5, customer.getAddress());
            pstmt.setString(6, customer.getCustomerId());

            int rows = pstmt.executeUpdate();
            System.out.println("  [OK] 客户更新成功，影响行数: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("  [ERROR] 更新客户失败: " + e.getMessage());
            return false;
        } finally {
            JDBCUtil.close(null, pstmt, conn);
        }
    }

    /**
     * 根据ID查询客户
     */
    public Customer getCustomerById(String customerId) {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return null;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractCustomer(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] 查询客户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * 查询所有客户
     */
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer ORDER BY customer_id";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return list;

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(extractCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] 查询所有客户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
        return list;
    }

    /**
     * 按姓名模糊搜索客户
     */
    public List<Customer> searchCustomersByName(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE customer_name LIKE ? ORDER BY customer_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return list;

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [ERROR] 搜索客户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * 从ResultSet中提取Customer对象
     */
    private Customer extractCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getString("customer_id"));
        c.setCustomerName(rs.getString("customer_name"));
        c.setIdCard(rs.getString("id_card"));
        c.setPhone(rs.getString("phone"));
        c.setCity(rs.getString("city"));
        c.setAddress(rs.getString("address"));
        c.setOpenDate(rs.getDate("open_date"));
        return c;
    }
}
