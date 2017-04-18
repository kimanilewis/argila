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
    private String availableTime;
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

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "AccountsData{" + "customerProfileAccountID=" + customerProfileAccountID + ", accountNumber=" + accountNumber + ", customerProfileID=" + customerProfileID + ", AvailableTime=" + availableTime + '}';
    }

}
