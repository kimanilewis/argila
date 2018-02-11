<?php

Namespace Argila\ArgilaCoreAPI\Controllers;

use Argila\ArgilaCoreAPI\Config\StatusCodes;
use Argila\ArgilaCoreAPI\Models\Mpesa as Request;
use Argila\ArgilaCoreAPI\Models\customerProfiles as customerProfile;
use Argila\ArgilaCoreAPI\Models\customerAccounts as customerAccounts;
use Argila\ArgilaCoreAPI\Models\paymentRequests as payment;
use Argila\ArgilaCoreAPI\Utilities\SyncLogger as logger;
use Argila\ArgilaCoreAPI\Config\Config;
use Argila\ArgilaCoreAPI\Config\Validation;
use Argila\ArgilaCoreAPI\Utilities\Helpers as helpers;
use Argila\ArgilaCoreAPI\Utilities\CoreUtils as CoreUtils;
use Ubench as benchmark;
use Exception;

/**
 * This is the main services controller. Performs all CRUD funtionalitites
 * @author Lewis Kimani <kimanilewi@gmail.com>
 */
class MpesaController
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
    private $balance;

    function __construct() {
        $this->log = new logger();
        $this->benchmark = new benchmark();
        $this->coreUtils = new CoreUtils();
        $this->helpers = new helpers();
    }

    function processMpesaRequest($request) {
        $this->benchmark->start();
        $this->log->info(Config::info, -1,
            "Received mpesa request "
            . $this->log->printArray($request));
        $results = array();
        $response['accept'] = -1;
        $this->log->info(Config::info, -1,
            "Proceeding to validate  "
            . " mpesa request ");
//        $customerProfile = new customerProfile();
        $validator = new Validation();
        /**
         * *********************************************************************
         * validate incoming datatypes, and verify required params are available
         */
        $rules = $validator->rules['mpesa_request'];
        $requestData = $request;
        $this->log->info(Config::info, -1,
            "Received mpesa data...."
            . $this->log->printArray($request));
        $statusDescription = "invalid parameters: ";
        try {

            if ($this->helpers->validateParams($requestData, $rules)) {
                $this->log->info(Config::info, -1,
                    "Request successfully validated."
                    . " Proceeding to validate account details."
                    . $this->log->printArray($rules));

                //log request 
                $this->logPaymetRequest($requestData);
                /**
                 * *************************************************************
                 * validate mandatory parameter for services model
                 */
                /*
                 * *************************************************************
                 * check for mandatory clientCode
                 */
                $accountNumber = $requestData['BillRefNumber'];
                $accountProfileDetails = $this->coreUtils->checkAccountProfile($accountNumber);
                if (count($accountProfileDetails) > 0) {
                    $this->log->info(Config::info, $accountNumber,
                        "account profile exists: About to update account "
                        . $this->log->printArray($accountProfileDetails));
                    $this->updateAccountDetails($request, $accountProfileDetails);

                } else {
                    $this->log->info(Config::info, $accountNumber,
                        "account profile does not exist:"
                        . "  About to check if its a valid card "
                        . $this->log->printArray($accountProfileDetails));

                    $cardDetails = $this->coreUtils->checkCardDetails($accountNumber);
                    if (count($cardDetails) > 0) {
                        $this->log->info(Config::info, $accountNumber,
                            " card details exists:  "
                            . $this->log->printArray($cardDetails));
                        $card_id = $cardDetails['card_id'];
                        //create customer account
                        $this->saveCustomerAccount($request, $cardDetails);
                        } else {
                        $this->log->info(Config::info, $accountNumber,
                            "account profile does not exist:"
                            . "  exiting... "
                            . $this->log->printArray($cardDetails));
                        return $response;
                        }
                }
                return $response;
                /*
                 * **************************************************************
                 * validate mandatory parameter service code
                 */
                $serviceCode = $requestData['serviceCode'];

                $validateCurrencyCode = $this->coreUtils->getService($serviceCode);
                if (count($validateCurrencyCode) > 0) {
                    $this->log->debug(Config::debug, $clientCode,
                        "Service Code exists: Proceeding to create one"
                        . " service details are: "
                        . $this->log->printArray($$accountProfileDetails));
                    $getRandon = $this->coreUtils->randomCode(6);
                    $serviceCode = substr($serviceCode, 0, 3) . $getRandon;
                    $this->log->debug(Config::debug, $serviceCode,
                        "Service Code updated to: " . $serviceCode);
                    $service->serviceCode = $serviceCode;
                    $result['serviceCode'] = $serviceCode;
                } else {
                    $this->log->info(Config::info, $serviceCode,
                        "service does not exist. Preceeding to create one: ");
                    $service->serviceCode = $serviceCode;
                }
                /*
                 * **************************************************************
                 * validate mandatory parameter currency code
                 */
                $currencyCode = $requestData['currencyCode'];

                $validateCurrencyCode = $this->coreUtils->getCurrency($currencyCode);
                if (count($validateCurrencyCode) > 0) {
                    $this->log->info(Config::info, $currencyCode,
                        "currencyCode validated successfully: "
                        . " currency details are: "
                        . $this->log->printArray($validateCurrencyCode));
                    $currencyID = $validateCurrencyCode['currencyID'];
                    $service->currencyID = $currencyID;
                } else {
                    $result['statusCode'] = StatusCodes::INVALID_CURRENCY_CODE;
                    $statusDescription .= "CurrencyCode($currencyCode),";
                    $result['statusDescription'] = $statusDescription;
                    array_push($results, $result);
                }

                $service->active = StatusCodes::ACTIVE;
                $service->dateCreated = date(Config::DATE_TIME_FORMAT);

            }
            /**
             * *****************************************************************
             * PARAMS FAILED LEVEL ONE DATATYPE VALIDATION.
             * *****************************************************************
             * This means either there requires parameters which are empty
             *  or not provided. It could also be datatypes required did not
             *  match with the incoming data.
             */ else {
                $this->log->info(Config::info, -1,
                    "Failed datatype validation: "
                    . $this->log->printArray($rules));

                $result['statusCode'] = StatusCodes::INVALID_REQUSET_DATA;
                $statusDescription .= "Request failed data validation";
                $result['statusDescription'] = $statusDescription;
                array_push($results, $result);
                $response['accept'] = Config::FAILED_MPESA_REQUEST;
                return $response;
            }
        } catch (\Exception $ex) {
            $this->log->error(Config::info, -1,
                "An Error Occurred: " . $ex->getMessage());
            $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
            $result['statusDescription'] = $ex->getMessage();
            $result['transactionID'] = -1;
            $this->log->error(Config::info, -1,
                "An Error Occurred: " . $ex->getMessage());
            $response['accept'] = Config::FAILED_MPESA_REQUEST;
            return $response;
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
            $this->log->info(Config::info, 0,
                "Client data validation failed. Following errors were found:"
                . $this->log->printArray($results));
            $result['statusCode'] = StatusCodes::FAILED_VALIDATION;
            $result['statusDescription'] = "Request validation failed";
            $result['clientCode'] = $clientCode;
            array_push($results, $result);

            $response['accept'] = Config::FAILED_MPESA_REQUEST;
            return $response;
        } else {
            try {
                $this->log->info(Config::info, 0,
                    "About to save the service request to the database ");
                $service->save();
                $result['hubServiceID'] = $service->serviceID;
                $this->log->info(Config::info, 0,
                    "Service created successfully. Result:"
                    . $this->log->printArray($results));
                $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                $result['statusDescription'] = "Service "
                    . "was created successfully";
                $response['accept'] = Config::SUCCESSFUL_MPESA_REQUEST;
                return $response;
            } catch (\PDOException $ex) {

                $this->log->error(Config::error, -1,
                    "An Exception Occured  " . $ex->getMessage());
                $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
                $result[
                    'statusDescription'] = "A general Exception Occured "
                    . "Contact Cellulant Support";
                $result['serviceID'] = -1;
                array_push($results, $result);
            }

            return $response;
        }
    }

    private function saveCustomerAccount($request, $cardDetails) {
        $this->benchmark->start();
        $results = array();
        $this->log->info(Config::info, -1,
            "Proceeding to initiate  "
            . "create account details "
            . $this->log->printArray($request));

        $customerProfile = new customerProfile();
        $customerProfile->MSISDN = $request['MSISDN'];
//        $customerProfile->customerName =$request['MSISDN'];
        $customerProfile->dateCreated = date(Config::DATE_TIME_FORMAT);
        $customerProfile->save();

        $billDetails = $this->calculateSubscription($request['TransAmount'], 0);
        $units = $billDetails['units'];
        $balance = $billDetails['balance'];
        $expiryDate = date(Config::DATE_TIME_FORMAT,
            strtotime(date(Config::DATE_TIME_FORMAT) . ' + ' . $units . ' months'));

        $profileID = $customerProfile->customerProfileID;
        $customerAccount = new customerAccounts();
        $customerAccount->customerProfileID = $profileID;
        $customerAccount->accountNumber = $request['BillRefNumber'];
        $customerAccount->balanceCarriedForward = $balance;
        $customerAccount->expiryDate = $expiryDate;
        $customerAccount->active = Config::ACTIVE;
        $customerAccount->dateCreated = date(Config::DATE_TIME_FORMAT);
        $this->log->info(Config::info, -1,
            "Proceeding to initiate  "
            . "Update account details "
            . $this->log->printArray($customerAccount));
        $customerAccount->save();
    }

    function updateAccountDetails($request, $accountDetails) {
        $this->benchmark->start();
        $results = array();
        $this->log->info(Config::info, -1,
            "Proceeding to initiate  "
            . "Update account details "
            . $this->log->printArray($request));
        try {
            //add amount to balance.
            $amount = $request['TransAmount'];
            $amountCarriedForward = $accountDetails['balanceCarriedForward'];
            $billDetails = $this->calculateSubscription($amount,
                $amountCarriedForward);
            $units = $billDetails['units'];
            $balance = $billDetails['balance'];
            $customerProfileAccountID = $accountDetails['customerProfileAccountID'];

            $expiryDate = $accountDetails['expiryDate'];
            $params['expiryDate'] = "DATE_ADD($expiryDate, INTERVAL $units " . ' ' . "  MONTH)";
            $params['balanceCarriedForward'] = $balance;

            $cutomerProfiles = customerAccounts::find($customerProfileAccountID);
            $newdDate = date(Config::DATE_TIME_FORMAT,
                strtotime($cutomerProfiles->expiryDate . ' + ' . $units . ' months'));

            $cutomerProfiles->expiryDate = $newdDate;
            $cutomerProfiles->balanceCarriedForward = $balance;
            $cutomerProfiles->save();
//            $cutomerProfile->save();
//            $updateResult = $this->coreUtils->updateQuery($customerProfileAccountID,
//                'customerProfileAccountID', 'customer_accounts', $params);
            if ($cutomerProfiles->save()){
                //Service  updated successfully .
                $this->log->info(Config::info, -1,
                    "customer account record updated successfully: ");
                $result['statusCode'] = StatusCodes::PROCESSED_REQUEST;
                $statusDescription .= "Record updated successfully";
                $result['statusDescription'] = $statusDescription;
                $result['accountNumber'] = $accountDetails['accountNumber'];
                return $result;
            } else {
                $this->log->info(Config::info, -1,
                    "Failed to update customer account "
                    . $this->log->printArray($params));

                $result['statusCode'] = StatusCodes::UNPROCESSED_REQUEST;
                $statusDescription .= "customer account record update failed";
                $result['statusDescription'] = $statusDescription;
                $result['accountNumber'] = $accountDetails['accountNumber'];
                array_push($results, $result);
            }
        } catch (\Exception $ex) {
            $this->log->error(Config::info, -1,
                "An Error Occurred" . $ex->getMessage());
            $result['statusCode'] = StatusCodes::GENERIC_FAILURE_STATUS_CODE;
            $result['statusDescription'] = $ex->getMessage();
            $result['hubServiceID'] = -1;
            array_push($results, $result);
        }
        $this->benchmark->end();
        return $result;
    }

    private function calculateSubscription($amountPaid, $balance) {
        $this->benchmark->start();
        $responce = array();

        $this->log->info(Config::info, -1,
            "Calculating subscription. "
            . "Amount paid: $amountPaid, available balance: $balance");
        //sum
        $totalAmount = $amountPaid + $balance;
        // calculate units

        $units = (int) floor($totalAmount / Config::COST_PER_UNIT);
        $newBalance = $totalAmount - ($units * Config::COST_PER_UNIT);
        $this->balance = $newBalance;
        $response['balance'] = $newBalance;
        $response['units'] = $units;
        $this->benchmark->end();

        return $response;
    }

    private function logPaymetRequest($param) {
        $payment = new payment();
        $payment->payerTransactionID = $param['TransID'];
        $payment->MSISDN = $param['MSISDN'];
        $payment->amountPaid = $param['TransAmount'];
        $payment->payerClient = 'mpesa';
        $payment->paymentInfo = trim($param['KYCInfo']);
        $payment->paymentDateReceived = date(Config::DATE_TIME_FORMAT);
        $payment->dateCreated = date(Config::DATE_TIME_FORMAT);
        $payment->save();
    }

}
