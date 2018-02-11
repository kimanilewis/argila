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
    const ACTIVE = 1;
    const CREATED = 2;
    //Mpesa 
    const SUCCESSFUL_MPESA_REQUEST = 0;
    const FAILED_MPESA_REQUEST = -1;
    const COST_PER_UNIT = 100;

}
