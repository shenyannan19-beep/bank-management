-- =============================================
-- 银行管理系统 - 数据库初始化脚本（带完整容错）
-- 数据库系统原理 课程综合实验
-- =============================================
SET NOCOUNT ON;
GO

-- =============================================
-- 步骤1：删除已存在的数据库（开发环境容错）
-- =============================================
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'bank_management')
BEGIN
    PRINT '检测到已存在的数据库，正在删除...';
    ALTER DATABASE bank_management SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE bank_management;
    PRINT '已删除旧数据库 bank_management';
END
ELSE
BEGIN
    PRINT '未检测到旧数据库，跳过删除步骤';
END
GO

-- =============================================
-- 步骤2：创建数据库
-- =============================================
CREATE DATABASE bank_management COLLATE Chinese_PRC_CI_AS;
PRINT '数据库 bank_management 创建成功（排序规则: Chinese_PRC_CI_AS）';
GO

USE bank_management;
GO

-- =============================================
-- 步骤3：删除已存在的数据库对象（按依赖顺序反向删除）
-- =============================================
PRINT '正在清理已存在的数据库对象...';

-- 删除触发器
IF OBJECT_ID('trg_update_balance', 'TR') IS NOT NULL
BEGIN DROP TRIGGER trg_update_balance; PRINT '  已删除触发器: trg_update_balance'; END
-- 删除视图
IF OBJECT_ID('vw_large_transactions', 'V') IS NOT NULL
BEGIN DROP VIEW vw_large_transactions; PRINT '  已删除视图: vw_large_transactions'; END
IF OBJECT_ID('vw_customer_summary', 'V') IS NOT NULL
BEGIN DROP VIEW vw_customer_summary; PRINT '  已删除视图: vw_customer_summary'; END
-- 删除存储过程
IF OBJECT_ID('sp_customer_accounts', 'P') IS NOT NULL
BEGIN DROP PROCEDURE sp_customer_accounts; PRINT '  已删除存储过程: sp_customer_accounts'; END
IF OBJECT_ID('sp_branch_deposit_summary', 'P') IS NOT NULL
BEGIN DROP PROCEDURE sp_branch_deposit_summary; PRINT '  已删除存储过程: sp_branch_deposit_summary'; END
IF OBJECT_ID('sp_query_transactions', 'P') IS NOT NULL
BEGIN DROP PROCEDURE sp_query_transactions; PRINT '  已删除存储过程: sp_query_transactions'; END
-- 删除表（按外键依赖顺序反向）
IF OBJECT_ID('TransactionRecord', 'U') IS NOT NULL
BEGIN DROP TABLE TransactionRecord; PRINT '  已删除表: TransactionRecord'; END
IF OBJECT_ID('Loan', 'U') IS NOT NULL
BEGIN DROP TABLE Loan; PRINT '  已删除表: Loan'; END
IF OBJECT_ID('Account', 'U') IS NOT NULL
BEGIN DROP TABLE Account; PRINT '  已删除表: Account'; END
IF OBJECT_ID('Employee', 'U') IS NOT NULL
BEGIN DROP TABLE Employee; PRINT '  已删除表: Employee'; END
IF OBJECT_ID('Customer', 'U') IS NOT NULL
BEGIN DROP TABLE Customer; PRINT '  已删除表: Customer'; END

PRINT '对象清理完成';
GO

-- =============================================
-- 步骤4：创建数据表
-- =============================================
PRINT '正在创建数据表...';

-- 4.1 客户表
CREATE TABLE Customer (
    customer_id     VARCHAR(10)     PRIMARY KEY,
    customer_name   NVARCHAR(50)    NOT NULL,
    id_card         VARCHAR(18)     NOT NULL UNIQUE,
    phone           VARCHAR(15),
    city            NVARCHAR(50),
    address         NVARCHAR(200),
    open_date       DATE            NOT NULL
);
PRINT '  ✓ 客户表 Customer 创建成功';

-- 4.2 员工表
CREATE TABLE Employee (
    employee_id     VARCHAR(10)     PRIMARY KEY,
    employee_name   NVARCHAR(50)    NOT NULL,
    position        NVARCHAR(30),
    department      NVARCHAR(30),
    phone           VARCHAR(15),
    hire_date       DATE,
    branch          NVARCHAR(50)
);
PRINT '  ✓ 员工表 Employee 创建成功';

-- 4.3 账户表
CREATE TABLE Account (
    account_id      VARCHAR(10)     PRIMARY KEY,
    customer_id     VARCHAR(10)     NOT NULL,
    account_type    NVARCHAR(20)    NOT NULL,
    balance         DECIMAL(18,2)   NOT NULL DEFAULT 0.00,
    status          NVARCHAR(10)    NOT NULL DEFAULT N'正常',
    open_date       DATE            NOT NULL,
    branch          NVARCHAR(50),
    CONSTRAINT FK_Account_Customer FOREIGN KEY (customer_id)
        REFERENCES Customer(customer_id) ON UPDATE CASCADE,
    CONSTRAINT CHK_Account_Balance CHECK (balance >= 0),
    CONSTRAINT CHK_Account_Status CHECK (status IN (N'正常', N'冻结', N'销户'))
);
PRINT '  ✓ 账户表 Account 创建成功';

-- 4.4 交易记录表
CREATE TABLE TransactionRecord (
    trans_id        VARCHAR(12)     PRIMARY KEY,
    account_id      VARCHAR(10)     NOT NULL,
    trans_type      NVARCHAR(20)    NOT NULL,
    amount          DECIMAL(18,2)   NOT NULL,
    trans_date      DATE            NOT NULL,
    trans_time      TIME,
    remark          NVARCHAR(200),
    operator_id     VARCHAR(10),
    CONSTRAINT FK_Trans_Account FOREIGN KEY (account_id)
        REFERENCES Account(account_id) ON UPDATE CASCADE,
    CONSTRAINT CHK_Trans_Amount CHECK (amount > 0),
    CONSTRAINT CHK_Trans_Type CHECK (trans_type IN (N'存款', N'取款', N'转账', N'利息结算', N'手续费'))
);
PRINT '  ✓ 交易记录表 TransactionRecord 创建成功';

-- 4.5 贷款表
CREATE TABLE Loan (
    loan_id         VARCHAR(10)     PRIMARY KEY,
    customer_id     VARCHAR(10)     NOT NULL,
    loan_type       NVARCHAR(50)    NOT NULL,
    amount          DECIMAL(18,2)   NOT NULL,
    interest_rate   DECIMAL(5,2)    NOT NULL,
    term_months     INT             NOT NULL,
    status          NVARCHAR(20)    NOT NULL DEFAULT N'申请中',
    apply_date      DATE            NOT NULL,
    approver_id     VARCHAR(10),
    CONSTRAINT FK_Loan_Customer FOREIGN KEY (customer_id)
        REFERENCES Customer(customer_id) ON UPDATE CASCADE,
    CONSTRAINT FK_Loan_Employee FOREIGN KEY (approver_id)
        REFERENCES Employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT CHK_Loan_Amount CHECK (amount > 0),
    CONSTRAINT CHK_Loan_Term CHECK (term_months > 0),
    CONSTRAINT CHK_Loan_Status CHECK (status IN (N'申请中', N'审批通过', N'放款中', N'还款中', N'已结清', N'逾期'))
);
PRINT '  ✓ 贷款表 Loan 创建成功';
GO

-- =============================================
-- 步骤5：创建索引
-- =============================================
PRINT '正在创建索引...';

CREATE INDEX IX_Account_Customer ON Account(customer_id);
CREATE INDEX IX_Account_Status ON Account(status);
CREATE INDEX IX_Account_Branch ON Account(branch);
CREATE INDEX IX_Trans_Account ON TransactionRecord(account_id);
CREATE INDEX IX_Trans_Date ON TransactionRecord(trans_date);
CREATE INDEX IX_Trans_Type ON TransactionRecord(trans_type);
CREATE INDEX IX_Loan_Customer ON Loan(customer_id);
CREATE INDEX IX_Loan_Status ON Loan(status);
PRINT '  ✓ 8个索引创建成功';
GO

-- =============================================
-- 步骤6：创建触发器（自动更新账户余额）
-- =============================================
PRINT '正在创建触发器...';

CREATE TRIGGER trg_update_balance
ON TransactionRecord
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- 存款、利息结算 → 余额增加
    UPDATE Account SET balance = balance + i.amount
    FROM Account a INNER JOIN inserted i ON a.account_id = i.account_id
    WHERE i.trans_type IN (N'存款', N'利息结算');

    -- 取款、手续费 → 余额减少
    UPDATE Account SET balance = balance - i.amount
    FROM Account a INNER JOIN inserted i ON a.account_id = i.account_id
    WHERE i.trans_type IN (N'取款', N'手续费') AND a.balance >= i.amount;

    -- 转账（转出）→ 余额减少
    UPDATE Account SET balance = balance - i.amount
    FROM Account a INNER JOIN inserted i ON a.account_id = i.account_id
    WHERE i.trans_type = N'转账' AND a.balance >= i.amount;
END;
GO
PRINT '  ✓ 触发器 trg_update_balance 创建成功';
GO

-- =============================================
-- 步骤7：创建存储过程
-- =============================================
PRINT '正在创建存储过程...';

-- 7.1 查询账户交易明细
CREATE PROCEDURE sp_query_transactions
    @p_account_id   VARCHAR(10),
    @p_start_date   DATE,
    @p_end_date     DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT trans_id       AS 交易编号,
           trans_type     AS 交易类型,
           amount         AS 金额,
           trans_date     AS 交易日期,
           trans_time     AS 交易时间,
           remark         AS 备注
    FROM TransactionRecord
    WHERE account_id = @p_account_id
      AND trans_date BETWEEN @p_start_date AND @p_end_date
    ORDER BY trans_date DESC, trans_time DESC;
END;
PRINT '  ✓ 存储过程 sp_query_transactions 创建成功';
GO

-- 7.2 支行存款汇总统计
CREATE PROCEDURE sp_branch_deposit_summary
AS
BEGIN
    SET NOCOUNT ON;
    SELECT branch        AS 支行名称,
           COUNT(*)      AS 账户数量,
           SUM(balance)  AS 存款总额,
           AVG(balance)  AS 平均余额
    FROM Account
    WHERE status = N'正常'
    GROUP BY branch
    ORDER BY SUM(balance) DESC;
END;
PRINT '  ✓ 存储过程 sp_branch_deposit_summary 创建成功';
GO

-- 7.3 查询客户所有账户
CREATE PROCEDURE sp_customer_accounts
    @p_customer_id VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT account_id    AS 账户编号,
           account_type  AS 账户类型,
           balance       AS 余额,
           status        AS 状态,
           branch        AS 开户支行,
           open_date     AS 开户日期
    FROM Account
    WHERE customer_id = @p_customer_id
    ORDER BY open_date;
END;
PRINT '  ✓ 存储过程 sp_customer_accounts 创建成功';
GO

-- =============================================
-- 步骤8：创建视图
-- =============================================
PRINT '正在创建视图...';

-- 8.1 客户资产汇总视图
CREATE VIEW vw_customer_summary AS
SELECT c.customer_id     AS 客户编号,
       c.customer_name   AS 客户姓名,
       c.phone           AS 联系电话,
       COUNT(DISTINCT a.account_id) AS 账户数量,
       ISNULL(SUM(a.balance), 0)    AS 总资产,
       c.city            AS 所在城市
FROM Customer c
LEFT JOIN Account a ON c.customer_id = a.customer_id AND a.status = N'正常'
GROUP BY c.customer_id, c.customer_name, c.phone, c.city;
PRINT '  ✓ 视图 vw_customer_summary 创建成功';
GO

-- 8.2 大额交易视图（近30天，5万元以上）
CREATE VIEW vw_large_transactions AS
SELECT t.trans_id       AS 交易编号,
       t.account_id     AS 账户编号,
       c.customer_name  AS 客户姓名,
       t.trans_type     AS 交易类型,
       t.amount         AS 交易金额,
       t.trans_date     AS 交易日期,
       t.remark         AS 备注
FROM TransactionRecord t
JOIN Account a ON t.account_id = a.account_id
JOIN Customer c ON a.customer_id = c.customer_id
WHERE t.amount >= 50000
  AND t.trans_date >= DATEADD(DAY, -30, CAST(GETDATE() AS DATE));
PRINT '  ✓ 视图 vw_large_transactions 创建成功';
GO

-- =============================================
-- 完成
-- =============================================
PRINT '';
PRINT '============================================';
PRINT '  数据库初始化完成！';
PRINT '  数据库: bank_management';
PRINT '  表: Customer, Employee, Account, TransactionRecord, Loan';
PRINT '  索引: 8个';
PRINT '  触发器: 1个 (trg_update_balance)';
PRINT '  存储过程: 3个';
PRINT '  视图: 2个';
PRINT '============================================';
GO
