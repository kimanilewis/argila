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

    private int coreRequestID;
    private String accountNumber;
    private double accountBalance = 0;
    private Long MSISDN;
    private String availableTime;
    private int customerProfileID;

    public int getCustomerProfileID() {
        return customerProfileID;
    }

    public void setCustomerProfileID(int customerProfileID) {
        this.customerProfileID = customerProfileID;
    }

    public int getCoreRequestID() {
        return coreRequestID;
    }

    public void setCoreRequestID(int coreRequestID) {
        this.coreRequestID = coreRequestID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    public Long getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(Long MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
    private String dateCreated;

    @Override
    public String toString() {
        return "AccountsData{" + "coreRequestID=" + coreRequestID
                + ", accountNumber=" + accountNumber
                + ", accountBalance=" + accountBalance
                + ", MSISDN=" + MSISDN
                + ", dateCreated=" + dateCreated
                + ", AvailableTime=" + availableTime
                + '}';
    }

}
