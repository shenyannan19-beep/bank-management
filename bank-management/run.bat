@echo off
chcp 65001 >nul
title 银行管理系统 - 一键运行

echo.
echo ╔══════════════════════════════════════════════════╗
echo ║       银 行 管 理 系 统 - 一 键 运 行            ║
echo ╚══════════════════════════════════════════════════╝
echo.

REM ===== 检测Java环境 =====
java -version 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] 未检测到Java环境！请安装JDK 8或更高版本。
    pause
    exit /b 1
)

REM ===== 检测并下载JDBC驱动 =====
set JDBC_JAR=lib\mssql-jdbc-10.2.3.jre8.jar
if not exist "%JDBC_JAR%" (
    echo [INFO] 正在下载SQL Server JDBC驱动...
    if not exist lib mkdir lib
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/10.2.3.jre8/mssql-jdbc-10.2.3.jre8.jar' -OutFile '%JDBC_JAR%'" 2>nul
    if %errorlevel% neq 0 (
        echo [WARN] 自动下载JDBC驱动失败，将尝试使用Maven下载
        where mvn >nul 2>nul
        if %errorlevel% equ 0 (
            call mvn dependency:copy-dependencies -DoutputDirectory=lib -q
        )
    )
)

REM ===== 设置classpath =====
set CP=src;%JDBC_JAR%

REM 如果Maven可用，优先使用Maven
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] 检测到Maven，使用Maven构建...
    echo.
    echo ==============================================
    echo  [Step 1/4] 正在编译项目...
    echo ==============================================
    call mvn compile -q
    if %errorlevel% neq 0 (
        echo [ERROR] 编译失败！请检查错误信息。
        pause
        exit /b 1
    )
    echo [OK] 编译成功！
) else (
    echo [INFO] 未检测到Maven，使用javac直接编译...
    echo.
    echo ==============================================
    echo  [Step 1/4] 正在编译项目...
    echo ==============================================
    REM 收集所有Java源文件
    dir /s /b src\*.java > sources.txt
    javac -encoding UTF-8 -cp "%CP%" @sources.txt
    del sources.txt
    if %errorlevel% neq 0 (
        echo [ERROR] 编译失败！
        pause
        exit /b 1
    )
    echo [OK] 编译成功！
)

echo.
echo ==============================================
echo  [Step 2/4] 正在初始化数据库...
echo ==============================================
if exist "%JDBC_JAR%" set CP=src;%JDBC_JAR%
if exist "lib\*" set CP=src;lib\*

REM 先尝试用sqlcmd直接执行SQL（更可靠）
where sqlcmd >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] 使用sqlcmd执行建表脚本...
    sqlcmd -S localhost -U sa -P YourPassword123 -i sql\setup_database.sql -f 65001 2>nul
    echo [INFO] 使用sqlcmd执行数据插入脚本...
    sqlcmd -S localhost -U sa -P YourPassword123 -i sql\insert_data.sql -f 65001 2>nul
    echo [OK] SQL脚本执行完成！
) else (
    REM 使用Java SQLExecutor
    java -cp "%CP%" com.bank.util.SQLExecutor
)

echo.
echo ==============================================
echo  [Step 3/4] 正在运行自动测试...
echo ==============================================
java -cp "%CP%" com.bank.main.AutoTest

echo.
echo ==============================================
echo  [Step 4/4] 启动交互式系统...
echo ==============================================
echo 提示：按数字选择菜单功能，按0退出
echo.
java -cp "%CP%" com.bank.main.BankManagementSystem

pause
