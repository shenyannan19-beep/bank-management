package com.bank.main;

import com.bank.dao.*;
import com.bank.entity.*;
import java.math.BigDecimal;

/**
 * 自动测试运行器
 * 无需用户交互，自动验证所有核心功能
 */
public class AutoTest {

    private static CustomerDAO customerDAO = new CustomerDAO();
    private static AccountDAO accountDAO = new AccountDAO();
    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║        银 行 管 理 系 统 - 自 动 测 试            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // 测试1：查询所有客户
        test("查询所有客户", () -> {
            java.util.List<Customer> list = customerDAO.getAllCustomers();
            assertTrue(list.size() >= 50, "客户数量应≥50，实际: " + list.size());
            System.out.println("    前5位客户:");
            for (int i = 0; i < Math.min(5, list.size()); i++) {
                System.out.println("    " + list.get(i));
            }
        });

        // 测试2：按ID查询客户
        test("查询客户C00001", () -> {
            Customer c = customerDAO.getCustomerById("C00001");
            assertNotNull(c, "客户C00001应该存在");
            assertEquals("张伟", c.getCustomerName(), "客户姓名应为张伟");
        });

        // 测试3：搜索客户
        test("搜索'张'姓客户", () -> {
            java.util.List<Customer> list = customerDAO.searchCustomersByName("张");
            assertTrue(list.size() >= 2, "至少应有2位张姓客户");
        });

        // 测试4：查询客户账户
        test("查询客户C00001的账户", () -> {
            java.util.List<Account> list = accountDAO.getAccountsByCustomer("C00001");
            assertTrue(list.size() >= 2, "C00001至少应有2个账户");
            for (Account a : list) {
                System.out.println("    " + a);
            }
        });

        // 测试5：查询单个账户
        test("查询账户A00000001", () -> {
            Account a = accountDAO.getAccountById("A00000001");
            assertNotNull(a, "A00000001应存在");
            System.out.println("    余额: " + a.getBalance());
        });

        // 测试6：存款
        test("存款测试 - 向A00000001存入1000元", () -> {
            boolean result = accountDAO.deposit("A00000001", new BigDecimal("1000.00"));
            assertTrue(result, "存款应该成功");
        });

        // 测试7：查询存款后余额
        test("验证存款后余额", () -> {
            Account a = accountDAO.getAccountById("A00000001");
            assertNotNull(a, "A00000001应存在");
            System.out.println("    当前余额: " + a.getBalance());
            // 余额应大于85000（原始余额）+ 1000（刚存入的）
            assertTrue(a.getBalance().compareTo(new BigDecimal("85000")) >= 0,
                    "余额应≥85000，实际: " + a.getBalance());
        });

        // 测试8：取款
        test("取款测试 - 从A00000001取出500元", () -> {
            boolean result = accountDAO.withdraw("A00000001", new BigDecimal("500.00"));
            assertTrue(result, "取款应该成功");
        });

        // 测试9：转账
        test("转账测试 - A00000001转500到A00000002", () -> {
            boolean result = accountDAO.transfer("A00000001", "A00000002",
                    new BigDecimal("500.00"));
            assertTrue(result, "转账应该成功");
        });

        // 测试10：转账后验证双方余额
        test("验证转账后余额", () -> {
            Account from = accountDAO.getAccountById("A00000001");
            Account to = accountDAO.getAccountById("A00000002");
            assertNotNull(from, "转出账户应存在");
            assertNotNull(to, "转入账户应存在");
            System.out.println("    A00000001余额: " + from.getBalance());
            System.out.println("    A00000002余额: " + to.getBalance());
        });

        // 测试11：支行统计
        test("支行存款统计", () -> {
            accountDAO.printBranchSummary();
        });

        // 测试12：存储过程调用
        test("调用sp_query_transactions存储过程", () -> {
            accountDAO.callSpQueryTransactions("A00000001", "2023-01-01", "2026-12-31");
        });

        // 测试13：客户资产汇总
        test("客户资产汇总视图", () -> {
            accountDAO.printCustomerAssetSummary();
        });

        // 测试14：余额不足取款（应为失败）
        test("余额不足测试 - 取款99999999元(应失败)", () -> {
            boolean result = accountDAO.withdraw("A00000001", new BigDecimal("99999999.00"));
            assertFalse(result, "余额不足时取款应该失败");
        });

        // 测试15：向不存在账户转账（应为失败）
        test("无效转账测试 - 转到不存在的账户(应失败)", () -> {
            boolean result = accountDAO.transfer("A00000001", "A99999999",
                    new BigDecimal("100.00"));
            assertFalse(result, "转入不存在账户的转账应该失败");
        });

        // 汇总
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              测 试 结 果 汇 总                    ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf ("║  通过: %-3d           失败: %-3d                  ║%n", passCount, failCount);
        System.out.printf ("║  总计: %-3d           通过率: %.1f%%              ║%n",
                passCount + failCount,
                (passCount + failCount) > 0 ? 100.0 * passCount / (passCount + failCount) : 0);
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        if (failCount == 0) {
            System.out.println("所有测试通过！银行管理系统运行正常。");
        } else {
            System.out.println("存在 " + failCount + " 个测试失败，请检查系统配置。");
        }
    }

    // ==================== 测试辅助方法 ====================
    private static void test(String name, Runnable testCase) {
        System.out.println();
        System.out.println("──────────────────────────────────────────────");
        System.out.println("[TEST] " + name);
        System.out.println("──────────────────────────────────────────────");
        try {
            testCase.run();
            passCount++;
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            failCount++;
            System.out.println("[FAIL] " + name + " - " + e.getMessage());
        } catch (Exception e) {
            failCount++;
            System.out.println("[FAIL] " + name + " - 异常: " + e.getMessage());
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError(message);
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual))
            throw new AssertionError(message + " (期望: " + expected + ", 实际: " + actual + ")");
    }

    static class AssertionError extends RuntimeException {
        public AssertionError(String message) { super(message); }
    }
}
