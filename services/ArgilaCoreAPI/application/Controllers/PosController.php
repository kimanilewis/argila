<?php

/**
 * @author Lewis Kimani <kimanilewi@gmail.com>
 */

namespace Argila\ArgilaCoreAPI\Controllers;

use Argila\ArgilaCoreAPI\Config\StatusCodes;
use Argila\ArgilaCoreAPI\Models\User as Request;
use Argila\ArgilaCoreAPI\Utilities\SyncLogger as logger;
use Symfony\Component\Config\Definition\Exception\Exception;
use Ubench as benchmark;
use Argila\ArgilaCoreAPI\Config\Config;
use Argila\ArgilaCoreAPI\Config\Validation;
use Argila\ArgilaCoreAPI\Utilities\Helpers;
use Argila\ArgilaCoreAPI\Utilities\CoreUtils as CoreUtils;

/**
 * Pos Controller
 */
class PosController
{

    /**
     * * log class
     * */
    private $log;

    /**
     * benchmark class.
     */
    private $benchmark;
    private $coreUtils;
    private $helpers;
    private $clientID;

    function __construct() {
        $this->log = new logger();
        $this->benchmark = new benchmark();
        $this->coreUtils = new CoreUtils();
        $this->helpers = new helpers();
    }

    function processPosRequest($request) {

        $this->benchmark->start();
        $results = array();
        $this->log->info(Config::info, -1,
            "Received users request "
            . $this->log->printArray($request));
        /**
         * *********************************************************************
         * Get request function and forward request for processing
         */
        $this->log->debug(Config::debug, -1,
            "Received PoS data...."
            . $this->log->printArray($request));
        /**
         * *********************************************************************
         * Check if session exists
         */
        return Config::MAX_TIME;
        $accountData = $this->coreUtils->checkActiveSession($request['accountNumber']);
        if (new DateTime() < new DateTime($accountData['expiryDate']) && $accountData['processingStatus']
            != Config::ACTIVE){
            // expiry date is in future ..start a session.
            return Config::MAX_TIME;
         } else{
            //card expired
         }
    }

    function createPoSRequest($request) {

        $this->benchmark->start();
        $results = array();
        $this->log->debug(Config::debug, -1,
            "Proceeding to initiate  "
            . "a users request "
            . $this->log->printArray($request));
        $user = new Request();
        $validator = new Validation();
        $userID = 0;
        $emailAddress = '';
        $token = '';
        /**
         * *********************************************************************
         * validate incoming datatypes, and verify required params are available
         */
        $rules = $validator->rules['users'];
        $requestData = $request;
        $this->log->debug(Config::debug, -1,
            "Received users data...."
            . $this->log->printArray($request));
        $statusDescription = "invalid parameters: ";
        $clientName = '';
        try {
            if ($this->helpers->validateParams($requestData, $rules)) {
                $this->log->debug(Config::debug, -1,
                    "Ressponse from datatype validation....."
                    . $this->log->printArray($rules));
                /**
                 * *************************************************************
                 * validate mandatory parameter for clients model
                 */
                /*
                 * *************************************************************
                 * check for mandatory clientCode
                 */
                $clientCode = $requestData['clientCode'];
                $validateClientCode = $this->coreUtils->checkClientCode($clientCode);
                if (count($validateClientCode) > 0) {
                    $clientID = $validateClientCode['clientID'];
                    $user->clientID = $clientID;
                    $clientName = $validateClientCode['clientName'];
                } else {
                    $this->log->info(Config::info, $clientCode,
                        "Client with client Code $clientCode does not exist. "
                        . $this->log->printArray($validateClientCode));
                    $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
                    $statusDescription .= "Client with clientCode "
                        . " $clientCode does not exists.";
                    $result['statusDescription'] = $statusDescription;
                    return $result;
                }
                /*
                 * *************************************************************
                 * check for mandatory username
                 */
                $username = $requestData['userName'];
                $validateUsername = $this->coreUtils->checkUser($username);
                if (count($validateUsername) > 0) {
                    $user->userName = $username;
                    $clientID = $validateUsername['clientID'];
                    $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
                    $statusDescription .= "User with the username $username exists.";
                    $result['statusDescription'] = $statusDescription;
                    return $result;
                } else {
                    $user->userName = $username;
                    $this->log->info(Config::info, $username,
                        "User with the username $username does not exist. "
                        . "Proceeding to create the user.  "
                        . " user details are: "
                        . $this->log->printArray($validateUsername));
                }

                /*
                 * **************************************************************
                 * validate mandatory parameter currency code
                 */
                $MSISDN = $requestData['MSISDN'];

                $telephoneNo = $this->coreUtils->formatMSISDN($MSISDN);
                if ($telephoneNo > 0) {
                    $this->log->info(Config::info, $username,
                        "Telephone No validated successfully: "
                        . " telephoneNo  is: "
                        . $this->log->printArray($telephoneNo));
                    $user->MSISDN = $telephoneNo;
                } else {
                    $user->MSISDN = $telephoneNo;
                    $result['statusCode'] = StatusCodes::INVALID_CUSTOMER_MSISDN;
                    $statusDescription .= "CurrencyCode($username),";
                    $result['statusDescription'] = $statusDescription;
                    return $result;
                }
                /**
                 * *************************************************************
                 * Validate provided email address
                 */
                $emailAddress = isset($requestData['emailAddress']) ?
                    $requestData['emailAddress'] : "";

                if ($emailAddress != "") {
                    if (!filter_var($emailAddress, FILTER_VALIDATE_EMAIL)) {
                        $result['statusCode'] = StatusCodes::INVALID_EMAIL_PROVIDED;
                        $statusDescription .= "emailAddress"
                            . "($emailAddress),";
                        $result['statusDescription'] = $statusDescription;
                        return $result;
                    } else {
                        $user->emailAddress = $emailAddress;
                    }
                }
                /**
                 * *************************************************************
                 * Validate provided email address
                 */
                $token = isset($requestData['token']) ?
                    $requestData['token'] : "";

                if ($token != "") {
                    $this->log->info(Config::info, $username,
                        "Token found. Proceeding with validation: ");
                } else {
                    $this->log->error(Config::error, $username,
                        "No token provided or is invalid: ");
                    $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
                    $statusDescription .= "Missing token"
                        . "($token),";
                    $result['statusDescription'] = $statusDescription;
                    return $result;
                }
                $password = isset($requestData['userPassword']) ?
                    $requestData['userPassword'] : "";

                if ($password != "") {
                    $user->password = $password;
                } else {
                    $this->log->error(Config::error, $username,
                        "No passwors provided or is invalid: ");
                    $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
                    $statusDescription .= "password"
                        . "($password),";
                    $result['statusDescription'] = $statusDescription;
                    return $result;
                }
                /**
                 * Beloe lines sets up the default parameters of
                 *  before creating the model
                 */
                $user->passwordStatusID = Config::DEFAULT_PASSWORD_STATUS;
                $user->datePasswordChanged = date(Config::DATE_TIME_FORMAT);
                $user->dateActivated = date(Config::DATE_TIME_FORMAT);
                $user->dateCreated = date(Config::DATE_TIME_FORMAT);
                $user->active = StatusCodes::CREATED;
                $user->updatedBy = StatusCodes::PROCESSED_REQUEST;
                $user->createdBy = StatusCodes::PROCESSED_REQUEST;
            }
            /**
             * *****************************************************************
             * PARAMS FAILED LEVEL ONE DATATYPE VALIDATION.
             * *****************************************************************
             * This means either there requires parameters which are empty
             *  or not provided. It could also be datatypes required did not
             *  match with the incoming data.
             */ else {
                $this->log->debug(Config::debug, -1,
                    "Failed datatype validation: "
                    . $this->log->printArray($rules));

                $result['statusCode'] = StatusCodes::INVALID_REQUSET_DATA;
                $statusDescription .= "User details failed validation";
                $result['statusDescription'] = $statusDescription;
                return $result;
            }
        } catch (\Exception $ex) {
            $this->log->error(Config::error, -1,
                "An Error Occurred" . $ex->getMessage());
            $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
            $result['statusDescription'] = $ex->getMessage();
            $result['userID'] = -1;
        }

        /*
         * *********************************************************************
         *  POST VALIDATION PROCESS
         * *********************************************************************
         * If there exist validation errors log the request and return else
         * log request and forward the same request to queue
         */


        //Count the number of invalid payment variables.
        if (count($results) > 1) {
            $this->log->debug(Config::debug, 0,
                "User data validation failed. Following errors were found:"
                . $this->log->printArray($results));
            $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
            $result['statusDescription'] = "Request validation failed";
            $result['clientCode'] = $clientCode;
            return $result;
        } else {
            try {
                $this->log->info(Config::info, 0,
                    "About to save the user request to the database ");
                $user->save();
                $userID = $user->userID;
                $this->log->info(Config::info, 0,
                    "Created user with user ID..." . $userID);
                $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                $result['statusDescription'] = "User "
                    . "was created successfully";
                $result['userID'] = $userID;
            } catch (\PDOException $ex) {
                $this->log->error(Config::error, -1,
                    "An Exception Occured  " . $ex->getMessage());
                $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                $result[
                    'statusDescription'] = "A general Exception Occured "
                    . "Contact Cellulant Support";
                $result['userID'] = -1;
                return $result;
            }
        }
        /**
         *
         * @param type $request
         * @return array
         */
        if ($userID > 0) {
            try {
                $this->log->info(Config::info, 0, "About to add a user group.. ");
                $userGroup = $this->coreUtils->createUserGroup($userID);
                if ($userGroup > 0) {
                    $parameters = array(
                        'userName' => $username,
                        'emailAddress' => $emailAddress,
                        'password' => $password,
                        'requestSourceuserID' => $userID,
                        'groupName' => Config::MERCHANT_ADMIN_GROUP_NAME,
                        'applicationName' => Config::CHECKOUT_APPLIACATION_NAME,
                        'clientCode' => $clientCode
                    );
                    $url = Config::CASUrl;
                    $postdata = CoreUtils::post($url, json_encode($parameters));
                    $casResponse = json_decode($postdata, true);
                    $this->log->error(Config::error, -1,
                        "Response from Cas: " . $this->log->printArray($casResponse));

                    if ($casResponse['SUCCESS'] == 'TRUE') {
                        $this->log->error(Config::error, -1,
                            "User successfully synced to cas. About to send email");
                        $messageParams = array();
                        $messageParams['USERNAME'] = $username;
                        $messageParams['CLIENTNAME'] = $clientName;
                        $messageParams['CLIENTCODE'] = $clientCode;
                        $messageParams['TOKEN'] = $token;
                        $messageParams['CPG_URL'] = Config::CPGURL;
                        $messageParams['PORTAL_URL'] = Config::PORTAL_URL;
                        $logEmailStatus = $this->coreUtils->logEmail($userID,
                            $emailAddress, json_encode($messageParams));
                        if ($logEmailStatus > 0) {
                            $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                            $result['statusDescription'] = "Users email logged successfully";
                            $this->log->debug(Config::debug, -1,
                                "Response: " . $this->log->printArray($logEmailStatus));
                        } else {
                            $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                            $result['statusDescription'] = "User successfully created, "
                                . "but failed to log email";
                            $this->log->error(Config::error, -1,
                                "Response: " . $this->log->printArray($request));
                        }
                    } else {
                        $postDataLower = strtolower($postdata);
                        if ($casResponse['SUCCESS'] == 'FALSE' && strpos($postDataLower,
                                "duplicate entry") === false) {
                            $this->log->error(Config::error, -1,
                                "Syncing user to cas failed  ");
                            $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                            $result['statusDescription'] = "A general Exception Occured "
                                . "Contact Cellulant Support";
                            $result['userID'] = -1;
                        } else if ($casResponse['SUCCESS'] == 'FALSE' && strpos($postDataLower,
                                "duplicate entry") !== false) {
                            $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                            $result['statusDescription'] = "User was already on cas, "
                                . "no email will be logged";
                            $user->active = StatusCodes::ACTIVE;
                            $user->save();
                            $this->log->error(Config::error, -1,
                                "Response: " . $this->log->printArray($request));
                        }
                    }
                    return $result;
                }
            } catch (\PDOException $ex) {
                $this->log->error(Config::error, -1,
                    "An Exception Occured  " . $ex->getMessage());
                $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                $result['statusDescription'] = "A general Exception Occured "
                    . "Contact Cellulant Support";
                $result['userID'] = -1;
                return $result;
            }
        } else {
            $this->log->error(Config::error, -1,
                "User data validation failed. Following errors were found:"
                . $this->log->printArray($results));
            $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
            $result['statusDescription'] = "Request validation failed";
            $result['clientCode'] = $clientCode;
            return $result;
        }
        return $result;
    }

    /**
     *
     * @param type $request
     * @return type
     */
    function updateUserRequest($request) {
        $this->benchmark->start();
        $results = array();
        $this->log->info(Config::info, -1,
            "Proceeding to initiate  "
            . "a user update request ");
        $user = new Request();
        $userID = 0;
        /**
         * *********************************************************************
         * validate incoming datatypes, and verify required params are available
         */
        $requestData = $request;
        $this->log->debug(Config::debug, -1,
            "Received user update data."
            . $this->log->printArray($request));
        $statusDescription = "invalid parameters: ";
        $emailAddress = '';
        try {
            /*
             * *************************************************************
             * check for mandatory clientCode and retrieve client details.
             */
            $clientCode = $requestData['clientCode'];
            $validateClient = $this->coreUtils->checkClientCode($clientCode);
            if (count($validateClient) > 0) {
                $this->log->info(Config::info, $clientCode,
                    "Client Code exists:  "
                    . " client details are: "
                    . $this->log->printArray($validateClient));
                $user->clientID = $validateClient['clientID'];
            } else {
                $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                $statusDescription .= "Client not identified for clientCode: $clientCode";
                $result['statusDescription'] = $statusDescription;

                return $request;
            }
            /*
             * *************************************************************
             * check for mandatory username
             */
            $username = $requestData['userName'];
            $validateUsername = $this->coreUtils->checkUser($username);
            if (count($validateUsername) > 0) {
                $this->log->debug(Config::debug, $username,
                    "User with the username $username found. "
                    . $this->log->printArray($validateUsername));
                $user->userName = $username;
                $emailAddress = $validateUsername['emailAddress'];
                $userID = $validateUsername['userID'];
            } else {
                $this->log->debug(Config::debug, $username,
                    "User with the username $username does not exist. "
                    . $this->log->printArray($validateUsername));
                $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
                $statusDescription .= "User with the username $username "
                    . " does not exists.";
                $result['statusDescription'] = $statusDescription;
                return $result;
            }
            if ($requestData['active'] && $requestData['active'] > 0) {
                $user->active = $requestData['active'];
            }
            foreach ($requestData as $requestKey => $value) {
                if (array_key_exists($requestKey, $user->model)) {
                    $updateParams[$requestKey] = $value;
                }
            }
            if (isset($requestData['userPassword']) && !empty($requestData['userPassword'])) {
                $password = $requestData['userPassword'];
                $updateParams['password'] = $password;
            }
            $updateResult = $this->coreUtils->updateQuery($userID, 'userID',
                'users', $updateParams);

            if (!empty($password)) {
                $casUpdate = $this->updateUserCas($username, $emailAddress,
                    $password);
                if ($casUpdate) {
                    $this->log->info(Config::info, -1,
                        "user record updated successfully on CAS: ");
                } else {
                    $this->log->error(Config::error, -1,
                        "Failed to save update user on CAS: ");
                    $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                    $statusDescription .= "Failed to save update user on CAS";
                    $result['statusDescription'] = $statusDescription;
                    $result['userID'] = -1;
                    return $result;
                }
            }

            if ($updateResult == 0 || $updateResult > 1) {
                $this->log->info(Config::info, -1,
                    "service record updated successfully: ");
                $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                $statusDescription .= "Record updated successfully";
                $result['statusDescription'] = $statusDescription;
                $result['userID'] = $userID;
                return $result;
            } else {
                $this->log->error(Config::error, -1,
                    "Failed to save update request: ");
                $result['statusCode'] = StatusCodes::INVALID_REQUSET_DATA;
                $statusDescription .= "Failed to save update request";
                $result['statusDescription'] = $statusDescription;
                $result['userID'] = -1;
            }
        } catch (\Exception $ex) {
            $this->log->error(Config::error, -1,
                "An Error Occurred" . $ex->getMessage());
            $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
            $result['statusDescription'] = $ex->getMessage();
            $result['userID'] = -1;
        }
        return $result;
    }

    /**
     *
     * @param type $username
     * @param type $emailAddress
     * @param type $password
     * @return boolean
     */
    function updateUserCas($username, $emailAddress, $password) {
        $url = Config::CAS_SET_PASSWORD;
        $parameters = array(
            'userName' => $username,
            'emailAddress' => $emailAddress,
            'requestOrigin' => Config::CAS_REQUEST_ORIGIN,
            'newPassword' => $password);
        $this->log->debug(Config::debug, $username,
            "Params to update CAS " . $this->log->printArray($parameters));
        $updatePassword = CoreUtils::post($url, json_encode($parameters));
        $this->log->debug(Config::debug, $username,
            "Response from Cas: " . $this->log->printArray($updatePassword));
        if ($updatePassword['SUCCESS'] == 'FALSE') {
            $this->log->error(Config::error, -1,
                "Syncing user password to cas failed  ");
            return FALSE;
        } else {
            $this->log->info(Config::info, -1,
                "Password successfully updated on cas ");
            return TRUE;
        }
    }

}
