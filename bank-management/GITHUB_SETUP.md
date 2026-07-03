# GitHub 推送指南

## 步骤 1：创建 GitHub 仓库

1. 访问 [github.com](https://github.com) 并登录
2. 点击右上角的 "+" 按钮，选择 "New repository"
3. 填写仓库信息：
   - **Repository name**: `bank-management`
   - **Description**: 银行管理系统 - 数据库系统原理课程实验
   - **Visibility**: 选择 Public（公开）或 Private（私有）
   - **不要**勾选 "Initialize this repository with a README"
4. 点击 "Create repository"

## 步骤 2：推送代码到 GitHub

在本地项目目录中运行以下命令：

```bash
# 添加远程仓库（替换 YOUR_USERNAME 为你的 GitHub 用户名）
git remote add origin https://github.com/YOUR_USERNAME/bank-management.git

# 推送代码到 GitHub
git push -u origin master
```

## 步骤 3：验证推送

1. 刷新 GitHub 仓库页面
2. 确认所有文件已上传
3. 检查 README.md 是否正确显示

## 分享链接

推送成功后，你的项目链接将是：
```
https://github.com/YOUR_USERNAME/bank-management
```

## 注意事项

1. **敏感信息**：已将数据库密码替换为环境变量，请勿提交真实密码
2. **环境变量**：使用者需要配置 `BANK_DB_USERNAME` 和 `BANK_DB_PASSWORD` 环境变量
3. **许可证**：如需添加开源许可证，请在 GitHub 仓库设置中添加

## 后续更新

如果需要更新代码：

```bash
# 添加修改的文件
git add .

# 提交更改
git commit -m "描述你的更改"

# 推送到 GitHub
git push
```