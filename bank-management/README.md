# 银行管理系统 (Bank Management System)

## 项目简介

《数据库系统原理》课程综合实验作业。基于 **SQL Server + Java JDBC** 的银行管理系统，支持客户管理、账户管理、存取款、转账、统计查询等功能。

## 技术栈

| 组件 | 技术 |
|------|------|
| 数据库 | Microsoft SQL Server |
| 开发语言 | Java (JDK 8+) |
| 数据库连接 | JDBC (mssql-jdbc 10.2.3) |
| 构建工具 | Maven / javac |

## 项目结构

```
bank-management/
├── src/com/bank/
│   ├── entity/          # 实体类
│   │   ├── Customer.java
│   │   ├── Account.java
│   │   ├── TransactionRecord.java
│   │   ├── Employee.java
│   │   └── Loan.java
│   ├── dao/             # 数据访问层
│   │   ├── CustomerDAO.java
│   │   └── AccountDAO.java
│   ├── util/            # 工具类
│   │   ├── JDBCUtil.java
│   │   └── SQLExecutor.java
│   └── main/            # 主程序
│       ├── BankManagementSystem.java
│       └── AutoTest.java
├── sql/
│   ├── setup_database.sql    # 建库建表脚本（含容错）
│   └── insert_data.sql       # 测试数据（380条）
├── screenshots/              # 截图保存目录
├── pom.xml                   # Maven配置
├── run.bat                   # Windows一键运行
├── run.sh                    # Linux/macOS一键运行
└── README.md
```

## 环境要求

1. **SQL Server** (Express或更高版本)
2. **JDK 8** 或更高版本
3. **Maven 3.6+** (可选，也支持javac直接编译)

## SQL Server 配置

1. 启用 **TCP/IP 协议**（端口1433）
   - 打开 SQL Server Configuration Manager
   - SQL Server 网络配置 → MSSQLSERVER 的协议 → 启用 TCP/IP
   - 右键 TCP/IP → 属性 → IP地址 → IPAll → TCP端口设为 1433
2. 启用 **sa 账户**并设置密码
   - SSMS → 安全性 → 登录名 → sa → 属性 → 设置密码
   - 状态 → 授予连接权限 → 启用登录
3. 确保 **SQL Server 服务**正在运行

## 快速开始

### Windows
```batch
run.bat
```

### Linux/macOS
```bash
chmod +x run.sh
./run.sh
```

### 手动运行

```bash
# 1. 编译（Maven）
mvn compile

# 2. 初始化数据库（两种方式）
# 方式A：使用sqlcmd（推荐）
sqlcmd -S localhost -U sa -P YourPassword123 -i sql/setup_database.sql
sqlcmd -S localhost -U sa -P YourPassword123 -i sql/insert_data.sql

# 方式B：使用Java SQLExecutor
# （需要先下载JDBC驱动到lib/目录）
java -cp "src;lib/mssql-jdbc-10.2.3.jre8.jar" com.bank.util.SQLExecutor

# 3. 运行自动测试
java -cp "src;lib/mssql-jdbc-10.2.3.jre8.jar" com.bank.main.AutoTest

# 4. 启动交互式系统
java -cp "src;lib/mssql-jdbc-10.2.3.jre8.jar" com.bank.main.BankManagementSystem
```

## 功能模块

### 客户管理
- 添加、删除、修改客户信息
- 按ID精确查询、按姓名模糊搜索
- 查看所有客户列表

### 账户管理
- 开户（活期/定期储蓄）
- 销户
- 查询账户余额和详情
- 查询客户名下所有账户

### 交易操作
- **存款** - 事务隔离级别 READ_COMMITTED + ROWLOCK
- **取款** - 含余额检查，事务隔离级别 READ_COMMITTED + ROWLOCK
- **转账** - 含双方余额验证，事务隔离级别 SERIALIZABLE + UPDLOCK/ROWLOCK

### 统计查询
- 支行存款汇总统计
- 客户资产汇总视图
- 交易明细查询（调用存储过程）
- 大额交易监控（≥5万元）

## 数据库对象

| 类型 | 名称 | 说明 |
|------|------|------|
| 表 | Customer | 客户信息表 |
| 表 | Employee | 员工信息表 |
| 表 | Account | 账户信息表 |
| 表 | TransactionRecord | 交易记录表 |
| 表 | Loan | 贷款信息表 |
| 索引 | 8个 | 客户ID、账户状态、支行、交易日期等 |
| 触发器 | trg_update_balance | 交易后自动更新账户余额 |
| 存储过程 | sp_query_transactions | 查询交易明细 |
| 存储过程 | sp_branch_deposit_summary | 支行存款汇总 |
| 存储过程 | sp_customer_accounts | 客户账户列表 |
| 视图 | vw_customer_summary | 客户资产汇总 |
| 视图 | vw_large_transactions | 近30天大额交易 |

## 测试数据

| 数据表 | 记录数 |
|--------|--------|
| Customer | 50条 |
| Employee | 20条 |
| Account | 80条 |
| TransactionRecord | 200条 |
| Loan | 30条 |
| **总计** | **380条** |

## 截图

交互式程序运行时会自动在 `screenshots/` 目录保存以下截图：
1. 欢迎界面 → 主菜单 → 客户管理 → 账户管理 → 交易操作 → 统计查询
2. 各功能操作界面（开户、存款、取款、转账等）
3. 支行统计结果 → 客户资产汇总

## 数据库连接配置

默认连接参数（在 `JDBCUtil.java` 中）：
- 主机：localhost
- 端口：1433
- 数据库：bank_management
- 用户名：通过环境变量 `BANK_DB_USERNAME` 配置（默认：bankadmin）
- 密码：通过环境变量 `BANK_DB_PASSWORD` 配置

### 配置步骤

1. 设置环境变量：
   ```bash
   # Windows
   set BANK_DB_USERNAME=your_username
   set BANK_DB_PASSWORD=your_password

   # Linux/macOS
   export BANK_DB_USERNAME=your_username
   export BANK_DB_PASSWORD=your_password
   ```

2. 或者直接编辑 `src/com/bank/util/JDBCUtil.java` 文件中的默认值。

## 注意事项

- SQL脚本中所有中文字符串使用 `N'...'` 前缀
- 转账使用 `SERIALIZABLE` 隔离级别确保数据一致性
- `setup_database.sql` 支持重复执行（自动删除旧对象）
- 如截图失败（无图形界面环境），不影响系统正常运行
- JDBC驱动会自动从Maven中央仓库下载
