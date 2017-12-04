/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import java.io.Serializable;

/**
 *
 * @author lewie
 */
public class AccountsData implements Serializable {

    private int customerProfileAccountID = 0;
    private int customerProfileID;
    private String accountNumber;
    private String expiryDate;
    private int expiryTime;
    private String locationID;

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public int getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(int expiryTime) {
        this.expiryTime = expiryTime;
    }
    private double amount = 0;

    public int getCustomerProfileAccountID() {
        return customerProfileAccountID;
    }

    public void setCustomerProfileAccountID(int customerProfileAccountID) {
        this.customerProfileAccountID = customerProfileAccountID;
    }

    public int getCustomerProfileID() {
        return customerProfileID;
    }

    public void setCustomerProfileID(int customerProfileID) {
        this.customerProfileID = customerProfileID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "AccountsData{"
                + "customerProfileAccountID=" + customerProfileAccountID
                + ", customerProfileID=" + customerProfileID
                + ", accountNumber=" + accountNumber
                + ", expiryDate=" + expiryDate
                + ", expiryTime=" + expiryTime + ", amount=" + amount
                + '}';
    }

}
