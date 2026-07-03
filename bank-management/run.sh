#!/bin/bash
# run.sh - 银行管理系统一键运行脚本

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║       银 行 管 理 系 统 - 一 键 运 行            ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# 检测Java环境
if ! command -v java &> /dev/null; then
    echo "[ERROR] 未检测到Java环境！请安装JDK 8或更高版本。"
    exit 1
fi

# 项目根目录
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# JDBC驱动
JDBC_JAR="lib/mssql-jdbc-10.2.3.jre8.jar"
if [ ! -f "$JDBC_JAR" ]; then
    echo "[INFO] 正在下载SQL Server JDBC驱动..."
    mkdir -p lib
    curl -sL -o "$JDBC_JAR" \
        "https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/10.2.3.jre8/mssql-jdbc-10.2.3.jre8.jar" 2>/dev/null
    if [ ! -f "$JDBC_JAR" ]; then
        echo "[WARN] 无法下载JDBC驱动，请手动下载放到lib/目录"
    fi
fi

CP="src:$JDBC_JAR"

# 检测Maven
if command -v mvn &> /dev/null; then
    echo "[INFO] 检测到Maven，使用Maven构建..."
    echo ""
    echo "=============================================="
    echo "  [Step 1/4] 正在编译项目..."
    echo "=============================================="
    mvn compile -q
    if [ $? -ne 0 ]; then
        echo "[ERROR] 编译失败！"
        exit 1
    fi
    echo "[OK] 编译成功！"
else
    echo "[INFO] 使用javac直接编译..."
    echo ""
    echo "=============================================="
    echo "  [Step 1/4] 正在编译项目..."
    echo "=============================================="
    find src -name "*.java" > sources.txt
    javac -encoding UTF-8 -cp "$CP" @sources.txt
    rm -f sources.txt
    if [ $? -ne 0 ]; then
        echo "[ERROR] 编译失败！"
        exit 1
    fi
    echo "[OK] 编译成功！"
fi

echo ""
echo "=============================================="
echo "  [Step 2/4] 正在初始化数据库..."
echo "=============================================="

# 尝试使用sqlcmd执行SQL
if command -v sqlcmd &> /dev/null; then
    echo "[INFO] 使用sqlcmd执行SQL脚本..."
    sqlcmd -S localhost -U sa -P YourPassword123 -i sql/setup_database.sql -f 65001 2>/dev/null
    sqlcmd -S localhost -U sa -P YourPassword123 -i sql/insert_data.sql -f 65001 2>/dev/null
    echo "[OK] SQL脚本执行完成！"
else
    java -cp "$CP" com.bank.util.SQLExecutor
fi

echo ""
echo "=============================================="
echo "  [Step 3/4] 正在运行自动测试..."
echo "=============================================="
java -cp "$CP" com.bank.main.AutoTest

echo ""
echo "=============================================="
echo "  [Step 4/4] 启动交互式系统..."
echo "=============================================="
echo "提示：按数字选择菜单功能，按0退出"
echo ""
java -cp "$CP" com.bank.main.BankManagementSystem
