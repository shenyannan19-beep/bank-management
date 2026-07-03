package com.bank.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户实体类
 */
public class Account {
    private String accountId;
    private String customerId;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private Date openDate;
    private String branch;

    public Account() {}

    public Account(String accountId, String customerId, String accountType,
                   BigDecimal balance, String status, Date openDate, String branch) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.openDate = openDate;
        this.branch = branch;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    @Override
    public String toString() {
        return String.format("账户[ID=%s, 客户ID=%s, 类型=%s, 余额=%.2f, 状态=%s, 开户日期=%s, 支行=%s]",
                accountId, customerId, accountType, balance, status, openDate, branch);
    }
}
