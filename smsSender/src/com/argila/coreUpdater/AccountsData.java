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

    private String message;
    private int smsID;
    private String msisdn;

//    @Override
//    public String toString() {
//        return "AccountsData{" + "customerProfileAccountID=" + customerProfileAccountID + ", accountNumber=" + accountNumber + ", sessionDataID=" + sessionDataID + '}';
//    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSmsID() {
        return smsID;
    }

    public void setSmsID(int smsID) {
        this.smsID = smsID;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @Override
    public String toString() {
        return "AccountsData{" + "message=" + message
                + ", smsID=" + smsID + ", msisdn=" + msisdn + '}';
    }

}
