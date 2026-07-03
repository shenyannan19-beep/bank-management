package com.bank.main;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GUI全功能演示 — 自动操作每个标签页并截图
 */
public class GUIDemo {

    static Robot robot;
    static Rectangle screenRect;
    static String ts;
    static File dir;
    static int baseX, tabY;
    static int seq = 0;

    public static void main(String[] args) throws Exception {
        ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        dir = new File("screenshots/demo");
        dir.mkdirs();

        robot = new Robot();
        robot.setAutoDelay(150);
        screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        // 计算GUI窗口在屏幕上的位置
        baseX = Math.max(5, (screenRect.width - 1200) / 2 + 10);
        tabY = 78;

        int[] tabX = { baseX + 60, baseX + 190, baseX + 320, baseX + 450, baseX + 580 };

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  GUI 全功能演示 + 自动截图              ║");
        System.out.println("║  screen: " + screenRect.width + "x" + screenRect.height + "  baseX: " + baseX + "                ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // 启动GUI
        System.out.print("启动GUI...");
        SwingUtilities.invokeLater(() -> {
            BankManagementGUI gui = new BankManagementGUI();
            gui.setVisible(true);
        });
        Thread.sleep(3500);
        System.out.println(" OK");

        // ═══════════ 1. 仪表盘 ═══════════
        snap("仪表盘_概览");

        // ═══════════ 2. 客户管理 ═══════════
        System.out.println("\n--- 客户管理 ---");
        clickTab(tabX[1], tabY);
        snap("客户管理_全部客户列表");

        // 搜索客户
        click(baseX + 500, tabY + 38);
        pasteText("张");
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Thread.sleep(600);
        snap("客户管理_模糊搜索张姓");

        // 刷新回全部
        click(baseX + 680, tabY + 38);
        Thread.sleep(500);

        // 添加客户
        click(baseX + 55, tabY + 38);
        Thread.sleep(900);
        snap("客户管理_添加客户对话框");

        // 填写表单 (对话框在屏幕中央, 约 centerX-250, y~120)
        int dx = screenRect.width / 2 - 220;
        int dy = 125;
        int rowH = 38;
        click(dx + 130, dy);           pasteText("C00099");
        click(dx + 130, dy + rowH);    pasteText("演示用户");
        click(dx + 130, dy + rowH*2);  pasteText("110101199901011299");
        click(dx + 130, dy + rowH*3);  pasteText("13800009999");
        click(dx + 130, dy + rowH*4);  pasteText("深圳");
        click(dx + 130, dy + rowH*5);  pasteText("深圳市南山区演示路99号");
        Thread.sleep(300);

        // 保存按钮 (对话框右下)
        click(dx + 300, dy + rowH*6 + 20);
        Thread.sleep(600);
        snap("客户管理_添加成功");

        // ═══════════ 3. 账户管理 ═══════════
        System.out.println("\n--- 账户管理 ---");
        clickTab(tabX[2], tabY);
        snap("账户管理_全部账户列表");

        // 查询客户C00001的账户
        click(baseX + 480, tabY + 38);
        pasteText("C00001");
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Thread.sleep(600);
        snap("账户管理_按客户C00001查询");

        // 开户
        click(baseX + 580, tabY + 38);  // 刷新
        Thread.sleep(600);
        click(baseX + 55, tabY + 38);   // 开户按钮
        Thread.sleep(900);
        snap("账户管理_开户对话框");

        click(dx + 130, dy);           pasteText("A00000099");
        click(dx + 130, dy + rowH);    pasteText("C00001");
        click(dx + 130, dy + rowH*3);  pasteText("50000");
        click(dx + 130, dy + rowH*4);  pasteText("北京总行");
        click(dx + 300, dy + rowH*5 + 20); // 保存
        Thread.sleep(600);
        snap("账户管理_开户成功");

        // ═══════════ 4. 交易操作 ═══════════
        System.out.println("\n--- 交易操作 ---");
        clickTab(tabX[3], tabY);
        snap("交易操作_三合一操作面板");

        // 三个卡片分别位于: left≈baseX+20, mid≈baseX+400, right≈baseX+780
        int cardY = tabY + 70;
        int col1 = baseX + 60;
        int col2 = baseX + 440;
        int col3 = baseX + 820;
        int inputX = 120;

        // 存款
        click(col1 + inputX, cardY);
        pasteText("A00000001");
        click(col1 + inputX, cardY + 50);
        pasteText("2000");
        click(col1 + 130, cardY + 115); // 存款按钮
        Thread.sleep(800);
        snap("交易操作_存款2000元成功");

        // 取款
        click(col2 + inputX, cardY);
        pasteText("A00000001");
        click(col2 + inputX, cardY + 50);
        pasteText("300");
        click(col2 + 130, cardY + 115); // 取款按钮
        Thread.sleep(800);
        snap("交易操作_取款300元成功");

        // 转账
        click(col3 + inputX, cardY);
        pasteText("A00000001");
        click(col3 + inputX, cardY + 42);
        pasteText("A00000002");
        click(col3 + inputX, cardY + 82);
        pasteText("500");
        click(col3 + 130, cardY + 135); // 转账按钮
        Thread.sleep(800);
        snap("交易操作_转账500元成功");

        // ═══════════ 5. 统计查询 ═══════════
        System.out.println("\n--- 统计查询 ---");
        clickTab(tabX[4], tabY);
        Thread.sleep(800);
        snap("统计查询_支行存款排名");

        // 子标签2: 客户资产
        click(baseX + 180, tabY + 38);
        Thread.sleep(600);
        snap("统计查询_客户资产总汇50人排名");

        // 子标签3: 交易明细
        click(baseX + 310, tabY + 38);
        Thread.sleep(400);
        // 填写查询条件
        int qy = tabY + 72;
        click(baseX + 60, qy);
        pasteText("A00000001");
        click(baseX + 190, qy);
        pasteText("2024-01-01");
        click(baseX + 330, qy);
        pasteText("2026-12-31");
        click(baseX + 470, qy); // 查询按钮
        Thread.sleep(800);
        snap("统计查询_交易明细查询结果");

        // ═══════════ 6. 仪表盘最终状态 ═══════════
        clickTab(tabX[0], tabY);
        snap("仪表盘_操作后最终数据");

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║  ✅ 演示完成！共 " + seq + " 张截图              ║");
        System.out.println("║  保存路径: screenshots/demo/             ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.exit(0);
    }

    // ══════════ 工具方法 ══════════

    static void snap(String name) throws Exception {
        seq++;
        String safeName = name.replace(" ", "_").replace("/", "_");
        String fname = String.format("demo_%02d_%s_%s.png", seq, safeName, ts);
        BufferedImage img = robot.createScreenCapture(screenRect);
        ImageIO.write(img, "PNG", new File(dir, fname));
        System.out.println("  ✅ [" + seq + "/16] " + name + "  →  " + fname);
    }

    static void clickTab(int x, int y) throws Exception {
        robot.mouseMove(x, y);
        robot.delay(100);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(800);
    }

    static void click(int x, int y) {
        robot.mouseMove(x, y);
        robot.delay(80);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(120);
    }

    /** 通过剪贴板粘贴中文文本 */
    static void pasteText(String text) {
        // 选中已有内容
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(50);

        // 写入剪贴板
        StringSelection sel = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        robot.delay(50);

        // Ctrl+V 粘贴
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(80);
    }
}
