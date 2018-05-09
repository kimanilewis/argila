<?php

Namespace Argila\ArgilaCoreAPI\Config;

/**
 * Sync status constants. Contains the statuses in which a record
 * can be in during processing.
 * PHP VERSION 5.3.6
 * @category  API
 * @package   Sync API
 * @author    Lewis Kimani   <kimanilewi@gmail.com>
 * @copyright 2017 Cellulant Ltd
 * @license   Proprietory License
 * @link      http://www.cellulant.com
 */
class StatusCodes
{

    const STATUS_UNKNOWN = 103; //not matching
    const GENERAL_EXCEPTION_OCCURRED = 104; //not matching
    const INACTIVE_CLIENT = 105;
    const INACTIVE_SERVICE = 106;
    const CUSTOMER_MSISDN_MISSING = 109;
    const INVALID_CUSTOMER_MSISDN = 110;
    const INVALID_CURRENCY_CODE = 115;
    const SERVICE_ID_NOT_SPECIFIED = 166;
    const SERVICE_CODE_PROVIDED_INVALID = 167;
    const GENERIC_SUCCESS_STATUS_CODE = 173;
    const GENERIC_FAILURE_STATUS_CODE = 174;
    const CLIENT_AUTHENTICATED_SUCCESSFULLY = 131;
    const CLIENT_AUTHENTICATION_FAILED = 132;
    //STATUS
    const ACTIVE = 1;
    const CREATED = 2;
    //REQUEST STATUSES

    const REQUEST_NOT_IDENTIFIED = 276;
    const MISSING_CURRENCY_CODE = 280;
    const EXPIRED_CARD_PROVIDED = 287;
    const INVALID_EMAIL_PROVIDED = 288;
    //CDE system known status codes
    const FAILED_VALIDATION = 9;
    const UNPROCESSED_REQUEST = 0;
    const PROCESSED_REQUEST = 1;
    const REQUEST_INPROCESS = 2;
    const DUPLICATE_REQUEST = 159;

}
