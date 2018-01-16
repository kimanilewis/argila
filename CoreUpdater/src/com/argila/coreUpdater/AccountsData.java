/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.coreUpdater;

import java.io.Serializable;

/**
 *
 * @author lewie
 */
public class AccountsData implements Serializable {

    private int customerProfileAccountID;
    private int sessionDataID;
    private String accountNumber;
    private long timeSpent;
    private double amountSpent = 0;
    private double amountBalance = 0;
    private String locationName;
    private String msisdn;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getSessionDataID() {
        return sessionDataID;
    }

    public void setSessionDataID(int sessionDataID) {
        this.sessionDataID = sessionDataID;
    }

    public double getAmountBalance() {
        return amountBalance;
    }

    public void setAmountBalance(double amountBalance) {
        this.amountBalance = amountBalance;
    }

    public double getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public int getCustomerProfileAccountID() {
        return customerProfileAccountID;
    }

    public void setCustomerProfileAccountID(int customerProfileAccountID) {
        this.customerProfileAccountID = customerProfileAccountID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "AccountsData{" + "customerProfileAccountID=" + customerProfileAccountID + ", accountNumber=" + accountNumber + ", sessionDataID=" + sessionDataID + '}';
    }

}
