package com.bank.entity;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

/**
 * 交易记录实体类
 */
public class TransactionRecord {
    private String transId;
    private String accountId;
    private String transType;
    private BigDecimal amount;
    private Date transDate;
    private Time transTime;
    private String remark;
    private String operatorId;

    public TransactionRecord() {}

    public TransactionRecord(String transId, String accountId, String transType,
                             BigDecimal amount, Date transDate, Time transTime,
                             String remark, String operatorId) {
        this.transId = transId;
        this.accountId = accountId;
        this.transType = transType;
        this.amount = amount;
        this.transDate = transDate;
        this.transTime = transTime;
        this.remark = remark;
        this.operatorId = operatorId;
    }

    public String getTransId() { return transId; }
    public void setTransId(String transId) { this.transId = transId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getTransType() { return transType; }
    public void setTransType(String transType) { this.transType = transType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Date getTransDate() { return transDate; }
    public void setTransDate(Date transDate) { this.transDate = transDate; }

    public Time getTransTime() { return transTime; }
    public void setTransTime(Time transTime) { this.transTime = transTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    @Override
    public String toString() {
        return String.format("交易[ID=%s, 账户=%s, 类型=%s, 金额=%.2f, 日期=%s, 时间=%s, 备注=%s]",
                transId, accountId, transType, amount, transDate, transTime, remark);
    }
}
