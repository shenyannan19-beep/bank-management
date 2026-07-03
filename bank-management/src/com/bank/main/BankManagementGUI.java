package com.bank.main;

import com.bank.dao.*;
import com.bank.entity.*;
import com.bank.util.JDBCUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 银行管理系统 - 图形用户界面 (Swing)
 */
public class BankManagementGUI extends JFrame {

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 样式常量
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color WARNING_COLOR = new Color(237, 108, 2);
    private static final Color DANGER_COLOR = new Color(198, 40, 40);
    private static final Color BG_COLOR = new Color(245, 245, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("Microsoft YaHei", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    private static final Font CARD_NUMBER_FONT = new Font("Segoe UI", Font.BOLD, 28);

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private JLabel timeLabel;

    // Dashboard 卡片
    private JLabel custCountLabel, accCountLabel, totalBalanceLabel, loanCountLabel;

    public BankManagementGUI() {
        initUI();
        setLookAndFeel();
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // 调整 Nimbus 默认颜色
                    UIManager.put("control", new Color(248, 248, 252));
                    UIManager.put("nimbusBase", PRIMARY_COLOR);
                    UIManager.put("nimbusBlueGrey", new Color(220, 225, 235));
                    UIManager.put("nimbusFocus", new Color(25, 118, 210, 80));
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Nimbus 不可用，使用默认外观");
        }
    }

    private void initUI() {
        setTitle("银行管理系统 v2.0 - Bank Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === 顶部标题栏 ===
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);

        // === 标签页面板 ===
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(BG_COLOR);

        tabbedPane.addTab("📊 仪表盘", createDashboardPanel());
        tabbedPane.addTab("👥 客户管理", createCustomerPanel());
        tabbedPane.addTab("💰 账户管理", createAccountPanel());
        tabbedPane.addTab("💳 交易操作", createTransactionPanel());
        tabbedPane.addTab("📈 统计查询", createStatisticsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // === 底部状态栏 ===
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // 添加标签切换监听
        tabbedPane.addChangeListener(e -> refreshCurrentTab());
    }

    // ======================== 顶部标题栏 ========================
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 118, 210));
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("🏦 银行管理系统  v2.0");
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Bank Management System  |  数据库系统原理 课程实验");
        subtitle.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        subtitle.setForeground(new Color(200, 215, 240));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(200, 220, 255));
        updateTime();
        new Timer(30000, e -> updateTime()).start();

        panel.add(textPanel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);
        return panel;
    }

    private void updateTime() {
        timeLabel.setText("🕐 " + sdfTime.format(new Date()));
    }

    // ======================== 底部状态栏 ========================
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 15, 5, 15));
        panel.setBackground(new Color(240, 242, 245));

        statusLabel = new JLabel("✅ 系统就绪");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        JLabel dbLabel = new JLabel("数据库: bank_management@localhost:1433  |  用户: bankadmin");
        dbLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        dbLabel.setForeground(Color.GRAY);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(dbLabel, BorderLayout.EAST);
        return panel;
    }

    private void setStatus(String text, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(text);
            statusLabel.setForeground(color);
        }
    }

    // ======================== 仪表盘 ========================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 顶部概览卡片
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);

        custCountLabel = createCard("👥", "客户总数", "0", PRIMARY_COLOR);
        accCountLabel = createCard("💳", "账户总数", "0", new Color(0, 150, 136));
        totalBalanceLabel = createCard("💰", "存款总额", "¥0", SUCCESS_COLOR);
        loanCountLabel = createCard("📋", "贷款笔数", "0", WARNING_COLOR);

        cardsPanel.add(wrapCard(custCountLabel, PRIMARY_COLOR));
        cardsPanel.add(wrapCard(accCountLabel, new Color(0, 150, 136)));
        cardsPanel.add(wrapCard(totalBalanceLabel, SUCCESS_COLOR));
        cardsPanel.add(wrapCard(loanCountLabel, WARNING_COLOR));

        // 中间部分：支行排名 + 最新交易
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 20, 0));
        middlePanel.setOpaque(false);

        JPanel branchPanel = createBranchRankingPanel();
        JPanel recentTransPanel = createRecentTransPanel();
        middlePanel.add(branchPanel);
        middlePanel.add(recentTransPanel);

        panel.add(cardsPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createCard(String icon, String title, String value, Color color) {
        JLabel label = new JLabel();
        label.setLayout(new BorderLayout());
        label.setBackground(CARD_BG);
        label.setOpaque(true);
        // 值会通过 setText 更新
        return label;
    }

    private JPanel wrapCard(JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 25, 20, 25),
            BorderFactory.createLineBorder(new Color(230, 230, 238), 1, true)
        ));

        // 通过 valueLabel 的文字解析出图标和标题
        // 实际上我们直接在卡片里重建
        return card;
    }

    // 仪表盘卡片（简化版）
    private JPanel createCardPanel(String icon, String title, String initValue, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(initValue);
        valueLabel.setFont(CARD_NUMBER_FONT);
        valueLabel.setForeground(accent);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        // 保存引用以便更新
        valueLabel.setName(title);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // 支行排名面板
    private JPanel createBranchRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 20, 15, 20),
            BorderFactory.createLineBorder(new Color(230, 230, 238), 1, true)
        ));

        JLabel title = new JLabel("🏆 支行存款排名");
        title.setFont(HEADER_FONT);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"排名", "支行", "账户数", "存款总额"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(80);

        // 加载数据
        loadBranchData(model);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void loadBranchData(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT branch, COUNT(*) AS cnt, SUM(balance) AS total " +
                "FROM Account WHERE status = N'正常' GROUP BY branch ORDER BY total DESC");
            int rank = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rank++,
                    rs.getString("branch"),
                    rs.getInt("cnt"),
                    String.format("¥%,.2f", rs.getBigDecimal("total"))
                });
            }
        } catch (SQLException e) {
            // 静默处理
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    // 最新交易面板
    private JPanel createRecentTransPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 20, 15, 20),
            BorderFactory.createLineBorder(new Color(230, 230, 238), 1, true)
        ));

        JLabel title = new JLabel("📝 最新交易记录");
        title.setFont(HEADER_FONT);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"时间", "类型", "金额", "账户"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        loadRecentTrans(model);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void loadRecentTrans(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT TOP 10 trans_type, amount, trans_date, trans_time, account_id " +
                "FROM TransactionRecord ORDER BY trans_date DESC, trans_time DESC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDate("trans_date") + " " + rs.getTime("trans_time"),
                    rs.getString("trans_type"),
                    String.format("¥%,.2f", rs.getBigDecimal("amount")),
                    rs.getString("account_id")
                });
            }
        } catch (SQLException e) {
            // 静默
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    // ======================== 客户管理面板 ========================
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 顶部操作栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        JButton btnAdd = createButton("➕ 添加客户", PRIMARY_COLOR);
        JButton btnEdit = createButton("✏️ 修改", new Color(0, 150, 136));
        JButton btnDelete = createButton("🗑 删除", DANGER_COLOR);
        JButton btnRefresh = createButton("🔄 刷新", new Color(100, 100, 100));
        JTextField searchField = new JTextField(15);
        searchField.setFont(BODY_FONT);
        searchField.putClientProperty("JTextField.placeholderText", "搜索客户姓名...");
        JButton btnSearch = createButton("🔍 搜索", new Color(63, 81, 181));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(searchField);
        toolbar.add(btnSearch);
        toolbar.add(btnRefresh);

        // 表格
        String[] cols = {"客户编号", "姓名", "身份证号", "电话", "城市", "地址", "开户日期"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        loadCustomers(model, null);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 228)));

        // 事件
        btnAdd.addActionListener(e -> showCustomerDialog(null, model));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) model.getValueAt(row, 0);
                showCustomerDialog(id, model);
            } else {
                showMessage("请先选择一位客户", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) model.getValueAt(row, 0);
                String name = (String) model.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "确定删除客户 " + name + " (" + id + ") 吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    customerDAO.deleteCustomer(id);
                    loadCustomers(model, null);
                    setStatus("已删除客户: " + name, DANGER_COLOR);
                }
            }
        });
        btnSearch.addActionListener(e -> loadCustomers(model, searchField.getText().trim()));
        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            loadCustomers(model, null);
            setStatus("✅ 客户列表已刷新", new Color(100, 100, 100));
        });
        searchField.addActionListener(e -> loadCustomers(model, searchField.getText().trim()));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadCustomers(DefaultTableModel model, String keyword) {
        model.setRowCount(0);
        List<Customer> list;
        if (keyword != null && !keyword.isEmpty()) {
            list = customerDAO.searchCustomersByName(keyword);
        } else {
            list = customerDAO.getAllCustomers();
        }
        for (Customer c : list) {
            model.addRow(new Object[]{
                c.getCustomerId(), c.getCustomerName(), c.getIdCard(),
                c.getPhone(), c.getCity(), c.getAddress(),
                c.getOpenDate() != null ? sdf.format(c.getOpenDate()) : ""
            });
        }
        setStatus("共 " + list.size() + " 位客户", PRIMARY_COLOR);
    }

    private void showCustomerDialog(String customerId, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, customerId == null ? "添加客户" : "修改客户", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 15));
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        form.setBackground(CARD_BG);

        JTextField tfId = addFormField(form, "客户编号 *", "C00051");
        JTextField tfName = addFormField(form, "客户姓名 *", "");
        JTextField tfIdCard = addFormField(form, "身份证号 *", "");
        JTextField tfPhone = addFormField(form, "联系电话", "");
        JTextField tfCity = addFormField(form, "所在城市", "");
        JTextField tfAddress = addFormField(form, "详细地址", "");
        JTextField tfDate = addFormField(form, "开户日期", sdf.format(new Date()));

        if (customerId != null) {
            Customer c = customerDAO.getCustomerById(customerId);
            if (c != null) {
                tfId.setText(c.getCustomerId());
                tfId.setEditable(false);
                tfName.setText(c.getCustomerName());
                tfIdCard.setText(c.getIdCard());
                tfPhone.setText(c.getPhone());
                tfCity.setText(c.getCity());
                tfAddress.setText(c.getAddress());
                tfDate.setText(sdf.format(c.getOpenDate()));
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(CARD_BG);
        JButton btnSave = createButton("💾 保存", PRIMARY_COLOR);
        JButton btnCancel = createButton("❌ 取消", Color.GRAY);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(CARD_BG);
        south.setBorder(new EmptyBorder(0, 25, 20, 25));
        south.add(btnPanel, BorderLayout.EAST);

        btnSave.addActionListener(e -> {
            try {
                Customer c = new Customer();
                c.setCustomerId(tfId.getText().trim());
                c.setCustomerName(tfName.getText().trim());
                c.setIdCard(tfIdCard.getText().trim());
                c.setPhone(tfPhone.getText().trim());
                c.setCity(tfCity.getText().trim());
                c.setAddress(tfAddress.getText().trim());
                c.setOpenDate(sdf.parse(tfDate.getText().trim()));

                if (c.getCustomerId().isEmpty() || c.getCustomerName().isEmpty() || c.getIdCard().isEmpty()) {
                    showMessage("客户编号、姓名、身份证号为必填项", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok;
                if (customerId == null) {
                    ok = customerDAO.addCustomer(c);
                } else {
                    ok = customerDAO.updateCustomer(c);
                }
                if (ok) {
                    loadCustomers(model, null);
                    dialog.dispose();
                    setStatus("✅ 客户保存成功: " + c.getCustomerName(), SUCCESS_COLOR);
                }
            } catch (Exception ex) {
                showMessage("输入格式错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ======================== 账户管理面板 ========================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);
        JButton btnOpen = createButton("➕ 开户", PRIMARY_COLOR);
        JButton btnClose = createButton("🔒 销户", DANGER_COLOR);
        JButton btnRefresh = createButton("🔄 刷新", new Color(100, 100, 100));
        JTextField searchField = new JTextField(12);
        searchField.setFont(BODY_FONT);
        searchField.putClientProperty("JTextField.placeholderText", "客户编号...");
        JButton btnSearch = createButton("🔍 按客户查", new Color(63, 81, 181));

        toolbar.add(btnOpen);
        toolbar.add(btnClose);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(new JLabel("客户:"));
        toolbar.add(searchField);
        toolbar.add(btnSearch);
        toolbar.add(btnRefresh);

        // 表格
        String[] cols = {"账户编号", "客户编号", "类型", "余额", "状态", "开户支行", "开户日期"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                c.setFont(CARD_NUMBER_FONT.deriveFont(13f));
                if (!sel) {
                    String s = (String) v;
                    if (s.contains("-")) c.setForeground(DANGER_COLOR);
                    else c.setForeground(SUCCESS_COLOR);
                }
                return c;
            }
        });

        loadAccounts(model, null);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 228)));

        btnOpen.addActionListener(e -> {
            showAccountDialog(model);
            refreshDashboard();
        });
        btnClose.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = (String) model.getValueAt(row, 0);
                int c = JOptionPane.showConfirmDialog(this, "确定销户 " + id + " 吗？");
                if (c == JOptionPane.YES_OPTION) {
                    accountDAO.closeAccount(id);
                    loadAccounts(model, null);
                    refreshDashboard();
                }
            }
        });
        btnSearch.addActionListener(e -> loadAccounts(model, searchField.getText().trim()));
        btnRefresh.addActionListener(e -> { searchField.setText(""); loadAccounts(model, null); });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void loadAccounts(DefaultTableModel model, String customerId) {
        model.setRowCount(0);
        List<Account> list;
        if (customerId != null && !customerId.isEmpty()) {
            list = accountDAO.getAccountsByCustomer(customerId);
        } else {
            list = getAllAccounts();
        }
        for (Account a : list) {
            model.addRow(new Object[]{
                a.getAccountId(), a.getCustomerId(), a.getAccountType(),
                String.format("¥%,.2f", a.getBalance()), a.getStatus(),
                a.getBranch(), a.getOpenDate() != null ? sdf.format(a.getOpenDate()) : ""
            });
        }
        setStatus("共 " + list.size() + " 个账户", PRIMARY_COLOR);
    }

    private List<Account> getAllAccounts() {
        // 简单查询所有账户
        java.util.List<Account> list = new java.util.ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Account ORDER BY account_id");
            while (rs.next()) {
                Account a = new Account();
                a.setAccountId(rs.getString("account_id"));
                a.setCustomerId(rs.getString("customer_id"));
                a.setAccountType(rs.getString("account_type"));
                a.setBalance(rs.getBigDecimal("balance"));
                a.setStatus(rs.getString("status"));
                a.setOpenDate(rs.getDate("open_date"));
                a.setBranch(rs.getString("branch"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("查询账户失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
        return list;
    }

    private void showAccountDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "开户", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBorder(new EmptyBorder(20, 25, 20, 25));
        form.setBackground(CARD_BG);

        JTextField tfAccId = addFormField(form, "账户编号 *", "A" + String.format("%08d", System.currentTimeMillis() % 100000000));
        JTextField tfCustId = addFormField(form, "客户编号 *", "");
        JComboBox<String> cbType = new JComboBox<>(new String[]{"活期储蓄", "定期储蓄"});
        cbType.setFont(BODY_FONT);
        form.add(new JLabel("账户类型:"));
        form.add(cbType);
        JTextField tfAmount = addFormField(form, "初始存款", "0");
        JTextField tfBranch = addFormField(form, "开户支行", "北京总行");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(CARD_BG);
        JButton btnSave = createButton("💾 开户", PRIMARY_COLOR);
        JButton btnCancel = createButton("❌ 取消", Color.GRAY);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(CARD_BG);
        south.setBorder(new EmptyBorder(0, 25, 20, 25));
        south.add(btnPanel, BorderLayout.EAST);

        btnSave.addActionListener(e -> {
            try {
                Account acc = new Account();
                acc.setAccountId(tfAccId.getText().trim());
                acc.setCustomerId(tfCustId.getText().trim());
                acc.setAccountType((String) cbType.getSelectedItem());
                acc.setBalance(new BigDecimal(tfAmount.getText().trim().isEmpty() ? "0" : tfAmount.getText().trim()));
                acc.setStatus("正常");
                acc.setOpenDate(new Date());
                acc.setBranch(tfBranch.getText().trim());

                if (acc.getAccountId().isEmpty() || acc.getCustomerId().isEmpty()) {
                    showMessage("账户编号和客户编号为必填项", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (accountDAO.openAccount(acc)) {
                    loadAccounts(model, null);
                    refreshDashboard();
                    dialog.dispose();
                    setStatus("✅ 开户成功: " + acc.getAccountId(), SUCCESS_COLOR);
                }
            } catch (NumberFormatException ex) {
                showMessage("金额格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ======================== 交易操作面板 ========================
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 存款面板
        panel.add(createTransCard("💰 存款", "存入金额:", SUCCESS_COLOR, (accId, amount) -> {
            return accountDAO.deposit(accId, amount);
        }));

        // 取款面板
        panel.add(createTransCard("💸 取款", "取出金额:", WARNING_COLOR, (accId, amount) -> {
            return accountDAO.withdraw(accId, amount);
        }));

        // 转账面板
        panel.add(createTransferCard());

        return panel;
    }

    interface TransAction {
        boolean execute(String accId, BigDecimal amount);
    }

    private JPanel createTransCard(String title, String amountLabel, Color accent, TransAction action) {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 238), 1, true),
                new EmptyBorder(20, 20, 20, 20)
            )
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);

        JPanel form = new JPanel(new GridLayout(3, 1, 0, 10));
        form.setOpaque(false);

        JTextField tfAccId = new JTextField();
        tfAccId.setFont(BODY_FONT);
        tfAccId.putClientProperty("JTextField.placeholderText", "账户编号");
        tfAccId.setBorder(createInputBorder());

        JTextField tfAmount = new JTextField();
        tfAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tfAmount.putClientProperty("JTextField.placeholderText", amountLabel);
        tfAmount.setBorder(createInputBorder());
        tfAmount.setHorizontalAlignment(JTextField.CENTER);

        JButton btn = createButton(title, accent);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        resultLabel.setHorizontalAlignment(JLabel.CENTER);

        form.add(tfAccId);
        form.add(tfAmount);
        form.add(btn);

        btn.addActionListener(e -> {
            try {
                String accId = tfAccId.getText().trim();
                BigDecimal amount = new BigDecimal(tfAmount.getText().trim());
                if (accId.isEmpty()) { resultLabel.setText("请输入账户编号"); return; }
                if (amount.compareTo(BigDecimal.ZERO) <= 0) { resultLabel.setText("金额必须大于0"); return; }
                boolean ok = action.execute(accId, amount);
                if (ok) {
                    resultLabel.setForeground(SUCCESS_COLOR);
                    resultLabel.setText("✅ 操作成功！");
                    tfAmount.setText("");
                    refreshDashboard();
                } else {
                    resultLabel.setForeground(DANGER_COLOR);
                    resultLabel.setText("❌ 操作失败");
                }
            } catch (NumberFormatException ex) {
                resultLabel.setForeground(DANGER_COLOR);
                resultLabel.setText("❌ 金额格式错误");
            }
        });

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(resultLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTransferCard() {
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, new Color(156, 39, 176)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 238), 1, true),
                new EmptyBorder(20, 20, 20, 20)
            )
        ));

        JLabel titleLabel = new JLabel("🔄 转账");
        titleLabel.setFont(HEADER_FONT);

        JPanel form = new JPanel(new GridLayout(4, 1, 0, 8));
        form.setOpaque(false);

        JTextField tfFrom = new JTextField();
        tfFrom.setFont(BODY_FONT);
        tfFrom.putClientProperty("JTextField.placeholderText", "转出账户");
        tfFrom.setBorder(createInputBorder());

        JTextField tfTo = new JTextField();
        tfTo.setFont(BODY_FONT);
        tfTo.putClientProperty("JTextField.placeholderText", "转入账户");
        tfTo.setBorder(createInputBorder());

        JTextField tfAmount = new JTextField();
        tfAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tfAmount.putClientProperty("JTextField.placeholderText", "转账金额");
        tfAmount.setBorder(createInputBorder());
        tfAmount.setHorizontalAlignment(JTextField.CENTER);

        JButton btn = createButton("🔄 确认转账", new Color(156, 39, 176));
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));

        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        resultLabel.setHorizontalAlignment(JLabel.CENTER);

        form.add(tfFrom);
        form.add(tfTo);
        form.add(tfAmount);
        form.add(btn);

        btn.addActionListener(e -> {
            try {
                String from = tfFrom.getText().trim();
                String to = tfTo.getText().trim();
                BigDecimal amount = new BigDecimal(tfAmount.getText().trim());
                if (from.isEmpty() || to.isEmpty()) { resultLabel.setText("请填写双方账户"); return; }
                if (from.equals(to)) { resultLabel.setText("不能向同一账户转账"); return; }
                if (amount.compareTo(BigDecimal.ZERO) <= 0) { resultLabel.setText("金额必须大于0"); return; }
                boolean ok = accountDAO.transfer(from, to, amount);
                if (ok) {
                    resultLabel.setForeground(SUCCESS_COLOR);
                    resultLabel.setText("<html>✅ 转账成功！<br>" + from + " → " + to + "<br>¥" + amount + "</html>");
                    tfAmount.setText("");
                    refreshDashboard();
                } else {
                    resultLabel.setForeground(DANGER_COLOR);
                    resultLabel.setText("❌ 转账失败，请检查余额");
                }
            } catch (NumberFormatException ex) {
                resultLabel.setForeground(DANGER_COLOR);
                resultLabel.setText("❌ 金额格式错误");
            }
        });

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(resultLabel, BorderLayout.SOUTH);
        return card;
    }

    // ======================== 统计查询面板 ========================
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTabbedPane statTabs = new JTabbedPane();
        statTabs.setFont(BODY_FONT);

        // 支行统计
        JPanel branchPanel = new JPanel(new BorderLayout());
        branchPanel.setBackground(CARD_BG);
        String[] branchCols = {"排名", "支行名称", "账户数量", "存款总额", "平均余额"};
        DefaultTableModel branchModel = new DefaultTableModel(branchCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable branchTable = new JTable(branchModel);
        styleTable(branchTable);
        loadBranchStats(branchModel);
        branchPanel.add(new JScrollPane(branchTable), BorderLayout.CENTER);

        // 客户资产
        JPanel assetPanel = new JPanel(new BorderLayout());
        assetPanel.setBackground(CARD_BG);
        String[] assetCols = {"客户编号", "客户姓名", "联系电话", "账户数量", "总资产", "所在城市"};
        DefaultTableModel assetModel = new DefaultTableModel(assetCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable assetTable = new JTable(assetModel);
        styleTable(assetTable);
        loadAssetStats(assetModel);
        assetPanel.add(new JScrollPane(assetTable), BorderLayout.CENTER);

        // 交易查询
        JPanel transQueryPanel = createTransQueryPanel();

        statTabs.addTab("🏆 支行存款排名", branchPanel);
        statTabs.addTab("👤 客户资产总汇", assetPanel);
        statTabs.addTab("📝 交易明细查询", transQueryPanel);

        panel.add(statTabs, BorderLayout.CENTER);
        return panel;
    }

    private void loadBranchStats(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT branch, COUNT(*) AS cnt, SUM(balance) AS total, AVG(balance) AS avg_bal " +
                "FROM Account WHERE status = N'正常' GROUP BY branch ORDER BY total DESC");
            int rank = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rank++, rs.getString("branch"), rs.getInt("cnt"),
                    String.format("¥%,.2f", rs.getBigDecimal("total")),
                    String.format("¥%,.2f", rs.getBigDecimal("avg_bal"))
                });
            }
        } catch (SQLException e) {
            System.err.println("统计查询失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    private void loadAssetStats(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM vw_customer_summary ORDER BY 总资产 DESC");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("客户编号"), rs.getString("客户姓名"),
                    rs.getString("联系电话"), rs.getInt("账户数量"),
                    String.format("¥%,.2f", rs.getBigDecimal("总资产")),
                    rs.getString("所在城市")
                });
            }
        } catch (SQLException e) {
            System.err.println("视图查询失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    private JPanel createTransQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        JTextField tfAcc = new JTextField(10);
        tfAcc.setFont(BODY_FONT);
        tfAcc.putClientProperty("JTextField.placeholderText", "账户编号");
        JTextField tfStart = new JTextField(10);
        tfStart.setFont(BODY_FONT);
        tfStart.putClientProperty("JTextField.placeholderText", "开始日期 yyyy-MM-dd");
        JTextField tfEnd = new JTextField(10);
        tfEnd.setFont(BODY_FONT);
        tfEnd.putClientProperty("JTextField.placeholderText", "结束日期");
        JButton btnQuery = createButton("🔍 查询", PRIMARY_COLOR);

        filterPanel.add(new JLabel("账户:"));
        filterPanel.add(tfAcc);
        filterPanel.add(new JLabel("从:"));
        filterPanel.add(tfStart);
        filterPanel.add(new JLabel("到:"));
        filterPanel.add(tfEnd);
        filterPanel.add(btnQuery);

        String[] cols = {"交易编号", "类型", "金额", "日期", "时间", "备注"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        btnQuery.addActionListener(e -> {
            model.setRowCount(0);
            String accId = tfAcc.getText().trim();
            String start = tfStart.getText().trim();
            String end = tfEnd.getText().trim();
            if (accId.isEmpty() || start.isEmpty() || end.isEmpty()) {
                showMessage("请填写账户编号和日期范围", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                conn = JDBCUtil.getConnection();
                if (conn == null) return;
                pstmt = conn.prepareStatement(
                    "SELECT trans_id, trans_type, amount, trans_date, trans_time, remark " +
                    "FROM TransactionRecord WHERE account_id = ? AND trans_date BETWEEN ? AND ? " +
                    "ORDER BY trans_date DESC, trans_time DESC");
                pstmt.setString(1, accId);
                pstmt.setString(2, start);
                pstmt.setString(3, end);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("trans_id"), rs.getString("trans_type"),
                        String.format("¥%,.2f", rs.getBigDecimal("amount")),
                        rs.getDate("trans_date"), rs.getTime("trans_time"),
                        rs.getString("remark")
                    });
                }
                setStatus("查询到 " + model.getRowCount() + " 条交易记录", PRIMARY_COLOR);
            } catch (SQLException ex) {
                System.err.println("交易查询失败: " + ex.getMessage());
            } finally {
                JDBCUtil.close(rs, pstmt, conn);
            }
        });

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ======================== 工具方法 ========================
    private void refreshCurrentTab() {
        int idx = tabbedPane.getSelectedIndex();
        if (idx == 0) refreshDashboard();
    }

    private void refreshDashboard() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) return;
            stmt = conn.createStatement();

            // 更新仪表盘数据 — 重新加载整个仪表盘
            tabbedPane.setComponentAt(0, createDashboardPanel());
        } catch (Exception e) {
            System.err.println("刷新仪表盘失败: " + e.getMessage());
        } finally {
            JDBCUtil.close(rs, stmt, conn);
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 235, 240));
        table.setSelectionBackground(new Color(25, 118, 210, 40));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 245, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private JTextField addFormField(JPanel form, String label, String defaultValue) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        JTextField tf = new JTextField(defaultValue);
        tf.setFont(BODY_FONT);
        tf.setBorder(createInputBorder());
        form.add(lbl);
        form.add(tf);
        return tf;
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1, true),
            new EmptyBorder(5, 8, 5, 8)
        );
    }

    private void showMessage(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    // ======================== 启动入口 ========================
    public static void main(String[] args) {
        // 尝试设置系统外观
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            BankManagementGUI gui = new BankManagementGUI();
            gui.setVisible(true);

            // 启动时加载仪表盘数据（在 EDT 中延迟执行）
            SwingUtilities.invokeLater(() -> {
                gui.setStatus("✅ 系统就绪 — 欢迎使用银行管理系统", PRIMARY_COLOR);
            });
        });
    }
}
