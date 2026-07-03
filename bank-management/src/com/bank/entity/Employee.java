package com.bank.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工实体类
 */
public class Employee {
    private String employeeId;
    private String employeeName;
    private String position;
    private String department;
    private String phone;
    private Date hireDate;
    private String branch;

    public Employee() {}

    public Employee(String employeeId, String employeeName, String position,
                    String department, String phone, Date hireDate, String branch) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.position = position;
        this.department = department;
        this.phone = phone;
        this.hireDate = hireDate;
        this.branch = branch;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    @Override
    public String toString() {
        return String.format("员工[ID=%s, 姓名=%s, 职位=%s, 部门=%s, 电话=%s, 入职=%s, 支行=%s]",
                employeeId, employeeName, position, department, phone, hireDate, branch);
    }
}
