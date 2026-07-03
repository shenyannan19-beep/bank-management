package com.bank.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 贷款实体类
 */
public class Loan {
    private String loanId;
    private String customerId;
    private String loanType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private int termMonths;
    private String status;
    private Date applyDate;
    private String approverId;

    public Loan() {}

    public Loan(String loanId, String customerId, String loanType, BigDecimal amount,
                BigDecimal interestRate, int termMonths, String status,
                Date applyDate, String approverId) {
        this.loanId = loanId;
        this.customerId = customerId;
        this.loanType = loanType;
        this.amount = amount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.status = status;
        this.applyDate = applyDate;
        this.approverId = approverId;
    }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getApplyDate() { return applyDate; }
    public void setApplyDate(Date applyDate) { this.applyDate = applyDate; }

    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }

    @Override
    public String toString() {
        return String.format("贷款[ID=%s, 客户ID=%s, 类型=%s, 金额=%.2f, 利率=%.2f%%, 期限=%d月, 状态=%s, 申请日期=%s]",
                loanId, customerId, loanType, amount, interestRate, termMonths, status, applyDate);
    }
}
