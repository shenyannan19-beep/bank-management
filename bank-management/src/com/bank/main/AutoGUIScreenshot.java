package com.bank.main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GUI自动截图工具
 * 启动GUI后自动切换标签页并截图
 */
public class AutoGUIScreenshot {

    public static void main(String[] args) throws Exception {
        System.out.println("===== GUI自动截图工具 =====");
        System.out.println("正在启动银行管理系统GUI...");

        // 在EDT中启动GUI
        SwingUtilities.invokeLater(() -> {
            BankManagementGUI gui = new BankManagementGUI();
            gui.setVisible(true);
        });

        // 等待GUI渲染完成
        Thread.sleep(3000);

        Robot robot = new Robot();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rect = new Rectangle(screen);
        File dir = new File("screenshots/gui");
        dir.mkdirs();

        // 截图1: 仪表盘 (tab 0)
        captureScreen(robot, rect, dir, "gui_01_dashboard_" + timestamp + ".png");
        System.out.println("✅ 截图1: 仪表盘");

        // 截图2-5: 切换标签页
        // 由于无法直接在外部切换标签，我们通过Robot模拟点击
        // 标签页位置大约在 y=70, 每个标签约150px宽

        int tabY = 72;
        int[] tabX = {50, 210, 370, 520, 670}; // 5个标签的x坐标(估算)

        String[] tabNames = {
            "gui_02_customers",
            "gui_03_accounts",
            "gui_04_transactions",
            "gui_05_statistics"
        };

        for (int i = 1; i < 5; i++) {
            robot.mouseMove(tabX[i], tabY);
            robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(1500); // 等待标签页加载

            java.awt.image.BufferedImage capture = robot.createScreenCapture(rect);
            ImageIO.write(capture, "PNG", new File(dir, tabNames[i-1] + "_" + timestamp + ".png"));
            System.out.println("✅ 截图" + (i+1) + ": " + tabNames[i-1]);
        }

        // 截图6: 交易操作 - 存款测试
        robot.mouseMove(tabX[3], tabY);
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(1000);
        captureScreen(robot, rect, dir, "gui_06_transaction_detail_" + timestamp + ".png");
        System.out.println("✅ 截图6: 交易操作面板");

        // 截图7: 统计查询
        robot.mouseMove(tabX[4], tabY);
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(1000);
        captureScreen(robot, rect, dir, "gui_07_statistics_" + timestamp + ".png");
        System.out.println("✅ 截图7: 统计查询面板");

        System.out.println();
        System.out.println("===== 截图完成！共7张 =====");
        System.out.println("保存路径: screenshots/gui/");

        System.exit(0);
    }

    private static void captureScreen(Robot robot, Rectangle rect, File dir, String filename) throws Exception {
        BufferedImage capture = robot.createScreenCapture(rect);
        ImageIO.write(capture, "PNG", new File(dir, filename));
    }
}
