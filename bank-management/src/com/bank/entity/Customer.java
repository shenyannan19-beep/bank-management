package com.bank.entity;

import java.util.Date;

/**
 * 客户实体类
 */
public class Customer {
    private String customerId;
    private String customerName;
    private String idCard;
    private String phone;
    private String city;
    private String address;
    private Date openDate;

    public Customer() {}

    public Customer(String customerId, String customerName, String idCard,
                    String phone, String city, String address, Date openDate) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.idCard = idCard;
        this.phone = phone;
        this.city = city;
        this.address = address;
        this.openDate = openDate;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Date getOpenDate() { return openDate; }
    public void setOpenDate(Date openDate) { this.openDate = openDate; }

    @Override
    public String toString() {
        return String.format("客户[ID=%s, 姓名=%s, 身份证=%s, 电话=%s, 城市=%s, 地址=%s, 开户日期=%s]",
                customerId, customerName, idCard, phone, city, address, openDate);
    }
}
