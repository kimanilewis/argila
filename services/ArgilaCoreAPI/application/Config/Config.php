<?php

Namespace Argila\ArgilaCoreAPI\Config;

/**
 * * Application configs 
 * * @author Lewis Kimani   <kimanilewi@gmail.com>
 * */
class Config
{

    ////
    const DbHost = 'localhost';
    const DbUser = "root";
    const DbPass = "lewie";
    const DbName = "argila";
    const API_DB = "API_CONNECTION_TO_DB";
    const error = '/var/log/applications/ke/argila/error.log';
    const info = '/var/log/applications/ke/argila/info.log';
    const debug = '/var/log/applications/ke/argila/debug.log';
    const tat = '/var/log/applications/ke/argila/tat.log';
    const sequel = '/var/log/applications/ke/argila/sql.log';

    /**
     * move this to amazon ad
     * Warning this is a test move all this to the cloud
     * @TODO implement HSM , CloudBased or a physical HSM
     */
    const initializationVector = "8228b9a98ca15318";
    const EncryptionKey = "3c6e0b8a9c15224a";
    const SUCCESSFUL_REQUEST = "Request has been completed successfully";
    const FAILED_REQUEST = "Failed to complete the request";
    const DATE_TIME_FORMAT = "Y-m-d H:i:s";

    /**
     * current date and time.
     */
    const TODAY = 'date("Y-m-d H:i:s")';
    const YEAR = 'date("Y")';
    const MONTH = 'date("m")';
    const FAILED_RESPONSE = 'SC0000Z';
    const SESSION_END = 'SC9999Z';
    const MAX_TIME = 'SC0045Z';
    const MAX_MINUTES = 45;
    const ACTIVE = 1;
    const CREATED = 2;
    //Mpesa 
    const SUCCESSFUL_MPESA_REQUEST = 0;
    const FAILED_MPESA_REQUEST = -1;
    const COST_PER_UNIT = 100;
    //sms
    const START_SESSION = "Dear CUSTOMERNAME, A session has been started with your card number - ACCOUNTNUMBER at LOCATIONNAME. MESSAGE ";
    const STOP_SESSION = "Dear CUSTOMERNAME, Your charging session has ended. Thank you for choosing #Tap&Charge.  MESSAGE ";
    const NEW_ACCOUNT = "Dear CUSTOMERNAME, Your card number ACCOUNTNUMBER has been activated. Thank you for choosing #Tap&Charge.";
    const TOP_UP = "Dear CUSTOMERNAME, Your #Tap&Charge subscription has been successfully renewed for card number -ACCOUNTNUMBER. Expiry date EXPIRYDATE ";
    const UNKNOWN_ACCOUNT = "Dear customer, Request failed. Card number entered doesn't exist. Please enter the card number specified on your card. For more info visit www.argilaTech.com";
    const UNDER_PAYMENT = "Dear CUSTOMERNAME, Your #Tap&Charge subscription has been successfully received for card number -ACCOUNTNUMBER. Kindly top up KES BALANCE .You account balance is KES BALANCECARRIEDFORWARD.";
    const MPESA_CALLBACK = "http://localhost/argilaCore/index.php/mpesa_request";
    const CHECKOUT_URL = "http://68.168.102.159/c2b/online";
    const CHECKOUT_STK_ = "http://10.172.71.110:20800/c2b/api/online";

//    const 
}
