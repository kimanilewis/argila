/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cellulant.CoreBroadcastProcessor;

import java.io.Serializable;

/**
 *
 * @author daniel
 */
public class BroadcastData implements Serializable {

    private String invoiceNumber;
    private int beepTransactionID;
    private int rewardRecipientID;

    private String payerTransactionID;
    private String amount;
    private String MSISDN;
    private String accountNumber;

    private int serviceID;

    private String currencyCode;
    private String apiFunctionName;

    private String payerClientCode;
    private String sslCertificatePath;
    private String serviceCode;
    private String narration;
    private String paymentMode;
    private String dateCreated;
    private int numberOfSends;
    private String lastSend;
    private int overallStatus;
    /**
     * Time when the status should be next sent.
     */
    private String nextSend;
    /**
     * Time when the status was last sent.
     */
    private String firstSend;

    public int getRewardRecipientID() {
        return rewardRecipientID;
    }

    public void setRewardRecipientID(int rewardRecipientID) {
        this.rewardRecipientID = rewardRecipientID;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public int getBeepTransactionID() {
        return beepTransactionID;
    }

    public void setBeepTransactionID(int beepTransactionID) {
        this.beepTransactionID = beepTransactionID;
    }

    public String getPayerTransactionID() {
        return payerTransactionID;
    }

    public void setPayerTransactionID(String payerTransactionID) {
        this.payerTransactionID = payerTransactionID;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getServiceID() {
        return serviceID;
    }

    public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getApiFunctionName() {
        return apiFunctionName;
    }

    public void setApiFunctionName(String apiFunctionName) {
        this.apiFunctionName = apiFunctionName;
    }

    public String getPayerClientCode() {
        return payerClientCode;
    }

    public void setPayerClientCode(String payerClientCode) {
        this.payerClientCode = payerClientCode;
    }

    public String getSslCertificatePath() {
        return sslCertificatePath;
    }

    public void setSslCertificatePath(String sslCertificatePath) {
        this.sslCertificatePath = sslCertificatePath;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getNextSend() {
        return nextSend;
    }

    public void setNextSend(String nextSend) {
        this.nextSend = nextSend;
    }

    public String getFirstSend() {
        return firstSend;
    }

    public void setFirstSend(String firstSend) {
        this.firstSend = firstSend;
    }

    public int getNumberOfSends() {
        return numberOfSends;
    }

    public void setNumberOfSends(int numberOfSends) {
        this.numberOfSends = numberOfSends;
    }

    public String getLastSend() {
        return lastSend;
    }

    public void setLastSend(String lastSend) {
        this.lastSend = lastSend;
    }

    public int getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(int overallStatus) {
        this.overallStatus = overallStatus;
    }

}
