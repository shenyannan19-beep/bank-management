package com.bank.main;

import com.bank.dao.*;
import com.bank.entity.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * 银行管理系统 - 控制台交互式主程序
 * 包含完整菜单系统和自动截图功能
 */
public class BankManagementSystem {

    private static Scanner scanner = new Scanner(System.in);
    private static CustomerDAO customerDAO = new CustomerDAO();
    private static AccountDAO accountDAO = new AccountDAO();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                                                  ║");
        System.out.println("║       银 行 管 理 系 统  v1.0                     ║");
        System.out.println("║       Bank Management System                     ║");
        System.out.println("║       数据库系统原理 课程综合实验                  ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        captureScreenshot("00_welcome");

        boolean running = true;
        while (running) {
            showMainMenu();
            captureScreenshot("01_main_menu");

            System.out.print("请选择操作 [0-4]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": customerMenu(); break;
                case "2": accountMenu(); break;
                case "3": transactionMenu(); break;
                case "4": queryMenu(); break;
                case "0":
                    running = false;
                    System.out.println();
                    System.out.println("感谢使用银行管理系统，再见！");
                    break;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
        scanner.close();
    }

    // ==================== 主菜单 ====================
    private static void showMainMenu() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                 主 菜 单                          ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  1. 客户管理 (Customer Management)               ║");
        System.out.println("║  2. 账户管理 (Account Management)                ║");
        System.out.println("║  3. 交易操作 (Transaction Operations)            ║");
        System.out.println("║  4. 统计查询 (Statistics & Query)                ║");
        System.out.println("║  0. 退出系统 (Exit)                              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // ==================== 客户管理菜单 ====================
    private static void customerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("│              客 户 管 理                          │");
            System.out.println("├──────────────────────────────────────────────────┤");
            System.out.println("│  1. 添加客户     2. 删除客户     3. 修改客户      │");
            System.out.println("│  4. 查询客户     5. 所有客户     6. 搜索客户      │");
            System.out.println("│  0. 返回主菜单                                    │");
            System.out.println("└──────────────────────────────────────────────────┘");
            captureScreenshot("02_customer_menu");

            System.out.print("请选择操作 [0-6]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addCustomer(); break;
                case "2": deleteCustomer(); break;
                case "3": updateCustomer(); break;
                case "4": queryCustomer(); break;
                case "5": listAllCustomers(); break;
                case "6": searchCustomers(); break;
                case "0": back = true; break;
                default: System.out.println("无效选择！");
            }
        }
    }

    private static void addCustomer() {
        System.out.println();
        System.out.println("===== 添加新客户 =====");
        captureScreenshot("03_add_customer");

        try {
            System.out.print("客户编号 (如C00051): ");
            String id = scanner.nextLine().trim();
            System.out.print("客户姓名: ");
            String name = scanner.nextLine().trim();
            System.out.print("身份证号 (18位): ");
            String idCard = scanner.nextLine().trim();
            System.out.print("联系电话: ");
            String phone = scanner.nextLine().trim();
            System.out.print("所在城市: ");
            String city = scanner.nextLine().trim();
            System.out.print("详细地址: ");
            String address = scanner.nextLine().trim();

            Customer c = new Customer(id, name, idCard, phone, city, address, new Date());
            boolean result = customerDAO.addCustomer(c);
            System.out.println(result ? "添加成功！" : "添加失败！");
        } catch (Exception e) {
            System.out.println("输入有误: " + e.getMessage());
        }
    }

    private static void deleteCustomer() {
        System.out.println();
        System.out.println("===== 删除客户 =====");
        System.out.print("请输入要删除的客户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;
        customerDAO.deleteCustomer(id);
    }

    private static void updateCustomer() {
        System.out.println();
        System.out.println("===== 修改客户信息 =====");
        System.out.print("请输入要修改的客户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        Customer c = customerDAO.getCustomerById(id);
        if (c == null) {
            System.out.println("客户不存在！");
            return;
        }
        System.out.println("当前信息: " + c);
        System.out.println("（直接回车保留原值）");

        System.out.print("新姓名 [" + c.getCustomerName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) c.setCustomerName(name);

        System.out.print("新身份证 [" + c.getIdCard() + "]: ");
        String idCard = scanner.nextLine().trim();
        if (!idCard.isEmpty()) c.setIdCard(idCard);

        System.out.print("新电话 [" + c.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) c.setPhone(phone);

        System.out.print("新城市 [" + c.getCity() + "]: ");
        String city = scanner.nextLine().trim();
        if (!city.isEmpty()) c.setCity(city);

        System.out.print("新地址 [" + c.getAddress() + "]: ");
        String address = scanner.nextLine().trim();
        if (!address.isEmpty()) c.setAddress(address);

        customerDAO.updateCustomer(c);
    }

    private static void queryCustomer() {
        System.out.println();
        System.out.println("===== 查询客户 =====");
        System.out.print("请输入客户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        Customer c = customerDAO.getCustomerById(id);
        if (c != null) {
            System.out.println("查询结果: " + c);
        } else {
            System.out.println("未找到该客户！");
        }
    }

    private static void listAllCustomers() {
        System.out.println();
        System.out.println("===== 所有客户列表 =====");
        List<Customer> list = customerDAO.getAllCustomers();
        System.out.println("共 " + list.size() + " 位客户");
        System.out.println("──────────────────────────────────────────────");
        for (Customer c : list) {
            System.out.printf("  %s | %s | %s | %s%n",
                    c.getCustomerId(), c.getCustomerName(), c.getPhone(), c.getCity());
        }
        captureScreenshot("04_all_customers");
    }

    private static void searchCustomers() {
        System.out.println();
        System.out.println("===== 搜索客户 =====");
        System.out.print("请输入姓名关键字: ");
        String keyword = scanner.nextLine().trim();
        if (keyword.isEmpty()) return;

        List<Customer> list = customerDAO.searchCustomersByName(keyword);
        System.out.println("找到 " + list.size() + " 位客户");
        for (Customer c : list) {
            System.out.println("  " + c);
        }
    }

    // ==================== 账户管理菜单 ====================
    private static void accountMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("│              账 户 管 理                          │");
            System.out.println("├──────────────────────────────────────────────────┤");
            System.out.println("│  1. 开户          2. 销户         3. 查询账户     │");
            System.out.println("│  4. 客户账户列表  5. 账户余额查询                 │");
            System.out.println("│  0. 返回主菜单                                    │");
            System.out.println("└──────────────────────────────────────────────────┘");
            captureScreenshot("05_account_menu");

            System.out.print("请选择操作 [0-5]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": openAccount(); break;
                case "2": closeAccount(); break;
                case "3": queryAccount(); break;
                case "4": listCustomerAccounts(); break;
                case "5": queryBalance(); break;
                case "0": back = true; break;
                default: System.out.println("无效选择！");
            }
        }
    }

    private static void openAccount() {
        System.out.println();
        System.out.println("===== 开户 =====");
        captureScreenshot("06_open_account");

        try {
            System.out.print("账户编号 (如A00000081): ");
            String accId = scanner.nextLine().trim();
            System.out.print("客户编号: ");
            String custId = scanner.nextLine().trim();
            System.out.print("账户类型 (活期储蓄/定期储蓄): ");
            String type = scanner.nextLine().trim();
            System.out.print("初始存款金额: ");
            String amountStr = scanner.nextLine().trim();
            BigDecimal amount = new BigDecimal(amountStr.isEmpty() ? "0" : amountStr);
            System.out.print("开户支行: ");
            String branch = scanner.nextLine().trim();

            Account acc = new Account(accId, custId, type, amount, "正常", new Date(), branch);
            accountDAO.openAccount(acc);
        } catch (NumberFormatException e) {
            System.out.println("金额格式错误！");
        }
    }

    private static void closeAccount() {
        System.out.println();
        System.out.println("===== 销户 =====");
        System.out.print("请输入要销户的账户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        System.out.print("确认销户 " + id + " ? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            accountDAO.closeAccount(id);
        }
    }

    private static void queryAccount() {
        System.out.println();
        System.out.println("===== 查询账户 =====");
        System.out.print("请输入账户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        Account a = accountDAO.getAccountById(id);
        if (a != null) {
            System.out.println("查询结果: " + a);
        } else {
            System.out.println("未找到该账户！");
        }
    }

    private static void listCustomerAccounts() {
        System.out.println();
        System.out.println("===== 客户账户列表 =====");
        System.out.print("请输入客户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        List<Account> list = accountDAO.getAccountsByCustomer(id);
        System.out.println("该客户共有 " + list.size() + " 个账户");
        for (Account a : list) {
            System.out.println("  " + a);
        }
    }

    private static void queryBalance() {
        System.out.println();
        System.out.println("===== 余额查询 =====");
        System.out.print("请输入账户编号: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return;

        Account a = accountDAO.getAccountById(id);
        if (a != null) {
            System.out.println("──────────────────────────────");
            System.out.println("  账户: " + a.getAccountId());
            System.out.println("  类型: " + a.getAccountType());
            System.out.println("  余额: " + a.getBalance() + " 元");
            System.out.println("  状态: " + a.getStatus());
            System.out.println("──────────────────────────────");
        } else {
            System.out.println("未找到该账户！");
        }
    }

    // ==================== 交易操作菜单 ====================
    private static void transactionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("│              交 易 操 作                          │");
            System.out.println("├──────────────────────────────────────────────────┤");
            System.out.println("│  1. 存款          2. 取款         3. 转账         │");
            System.out.println("│  0. 返回主菜单                                    │");
            System.out.println("└──────────────────────────────────────────────────┘");
            captureScreenshot("07_transaction_menu");

            System.out.print("请选择操作 [0-3]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": doDeposit(); break;
                case "2": doWithdraw(); break;
                case "3": doTransfer(); break;
                case "0": back = true; break;
                default: System.out.println("无效选择！");
            }
        }
    }

    private static void doDeposit() {
        System.out.println();
        System.out.println("===== 存款操作 =====");
        captureScreenshot("08_deposit");

        try {
            System.out.print("账户编号: ");
            String accId = scanner.nextLine().trim();
            System.out.print("存款金额: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("金额必须大于0！");
                return;
            }

            accountDAO.deposit(accId, amount);
        } catch (NumberFormatException e) {
            System.out.println("金额格式错误！");
        }
    }

    private static void doWithdraw() {
        System.out.println();
        System.out.println("===== 取款操作 =====");
        captureScreenshot("09_withdraw");

        try {
            System.out.print("账户编号: ");
            String accId = scanner.nextLine().trim();
            System.out.print("取款金额: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("金额必须大于0！");
                return;
            }

            accountDAO.withdraw(accId, amount);
        } catch (NumberFormatException e) {
            System.out.println("金额格式错误！");
        }
    }

    private static void doTransfer() {
        System.out.println();
        System.out.println("===== 转账操作 =====");
        captureScreenshot("10_transfer");

        try {
            System.out.print("转出账户编号: ");
            String fromId = scanner.nextLine().trim();
            System.out.print("转入账户编号: ");
            String toId = scanner.nextLine().trim();
            System.out.print("转账金额: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("金额必须大于0！");
                return;
            }
            if (fromId.equals(toId)) {
                System.out.println("转出和转入账户不能相同！");
                return;
            }

            accountDAO.transfer(fromId, toId, amount);
        } catch (NumberFormatException e) {
            System.out.println("金额格式错误！");
        }
    }

    // ==================== 查询统计菜单 ====================
    private static void queryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────┐");
            System.out.println("│              统 计 查 询                          │");
            System.out.println("├──────────────────────────────────────────────────┤");
            System.out.println("│  1. 支行存款统计     2. 客户资产汇总              │");
            System.out.println("│  3. 交易明细查询     4. 大额交易视图              │");
            System.out.println("│  0. 返回主菜单                                    │");
            System.out.println("└──────────────────────────────────────────────────┘");
            captureScreenshot("11_query_menu");

            System.out.print("请选择操作 [0-4]: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    captureScreenshot("12_branch_summary");
                    accountDAO.printBranchSummary();
                    break;
                case "2":
                    captureScreenshot("13_customer_assets");
                    accountDAO.printCustomerAssetSummary();
                    break;
                case "3": queryTransactions(); break;
                case "4": queryLargeTransactions(); break;
                case "0": back = true; break;
                default: System.out.println("无效选择！");
            }
        }
    }

    private static void queryTransactions() {
        System.out.println();
        System.out.println("===== 交易明细查询 =====");
        System.out.print("账户编号: ");
        String accId = scanner.nextLine().trim();
        System.out.print("开始日期 (yyyy-MM-dd): ");
        String startDate = scanner.nextLine().trim();
        System.out.print("结束日期 (yyyy-MM-dd): ");
        String endDate = scanner.nextLine().trim();

        if (accId.isEmpty()) return;
        accountDAO.callSpQueryTransactions(accId, startDate, endDate);
    }

    private static void queryLargeTransactions() {
        System.out.println();
        System.out.println("===== 近30天大额交易（≥50000元） =====");
        // 通过AccountDAO的连接直接查询视图
        java.sql.Connection conn = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = com.bank.util.JDBCUtil.getConnection();
            if (conn == null) return;

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM vw_large_transactions ORDER BY 交易日期 DESC");

            System.out.println();
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("  %s | %s | %s | %s | %.2f | %s%n",
                        rs.getString("交易编号"),
                        rs.getString("客户姓名"),
                        rs.getString("交易类型"),
                        rs.getDate("交易日期"),
                        rs.getBigDecimal("交易金额"),
                        rs.getString("备注") != null ? rs.getString("备注") : "");
            }
            System.out.println("  共 " + count + " 条大额交易记录");
        } catch (Exception e) {
            System.err.println("查询大额交易失败: " + e.getMessage());
        } finally {
            com.bank.util.JDBCUtil.close(rs, stmt, conn);
        }
    }

    // ==================== 截图功能 ====================
    private static void captureScreenshot(String menuName) {
        try {
            java.awt.Robot robot = new java.awt.Robot();
            java.awt.Rectangle screenRect = new java.awt.Rectangle(
                java.awt.Toolkit.getDefaultToolkit().getScreenSize());
            java.awt.image.BufferedImage capture = robot.createScreenCapture(screenRect);
            String filename = "screenshots/" + menuName + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";
            new java.io.File("screenshots").mkdirs();
            javax.imageio.ImageIO.write(capture, "PNG", new java.io.File(filename));
            System.out.println("[Screenshot] " + filename);
        } catch (java.awt.HeadlessException e) {
            // 无图形界面环境（如SSH终端、CI环境），静默跳过
            System.out.println("[Screenshot] 无图形界面，跳过截图 (" + menuName + ")");
        } catch (Exception e) {
            System.out.println("[Screenshot] 失败: " + e.getMessage());
        }
    }
}
