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

    private int customerProfileAccountID;
    private int customerProfileID;
    private String accountNumber;
    private Long MSISDN;
    private String expiryDate;
    private String currency = "";
    private String startTime = "";
    private String expiryTime = "";
    private long timeSpent;
    private double amountSpent = 0;
    private double amount = 0;
    private double amountBalance = 0;
    private int synchStatus = 4;
    private int sessionDataID = 0;
    private int profileStatus;

    public int getProfileStatus() {
        return profileStatus;
    }

    public void setProfileStatus(int profileStatus) {
        this.profileStatus = profileStatus;
    }

    public int getSynchStatus() {
        return synchStatus;
    }

    public void setSynchStatus(int synchStatus) {
        this.synchStatus = synchStatus;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public Long getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(Long MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String AvailableTime) {
        this.expiryDate = AvailableTime;
    }

    public String getStartTime() {
        return startTime.replaceAll("\\.\\d+", "");
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getExpiryTime() {
        return expiryTime.replaceAll("\\.\\d+", "");
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getCustomerProfileID() {
        return customerProfileID;
    }

    public void setCustomerProfileID(int customerProfileID) {
        this.customerProfileID = customerProfileID;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "AccountsData{" + "customerProfileAccountID=" + customerProfileAccountID + ", accountNumber=" + accountNumber + ", customerProfileID=" + customerProfileID + ", MSISDN=" + MSISDN + ", ExpiryDate=" + expiryDate + ", currency=" + currency + ", startTime=" + startTime + ",  expiryTime=" + expiryTime + '}';
    }

}
