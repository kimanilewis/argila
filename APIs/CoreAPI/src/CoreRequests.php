<?php

/**
 * Core API
 * This file holds CoreRequests class. Its responsible for logging all requests from 
 *  CORE to await processing 
 * 
 *  PHP VERSION 5.3.6
 * 
 * @category  API
 * @package   Argila Technologies
 * @author    Lewis Kimani <kimanilewi@gmail.com>
 * @copyright 2016 Argila Technologies Ltd
 * @license   Proprietory License
 * @link      http://www.argilatech.com
 * 
 */
class CoreRequests
{

    /**
     * database PDO class inatance
     * 
     */
    private $dbutils;

    /**
     * logs class inatance
     * 
     */
    private $log;

    /**
     * response class instance
     * 
     */
    private $response;
    private $crud;
    private $coreUtils;
    private $responseArray = array('SUCCESS' => TRUE,
        'STATCODE' => 1,
        'REASON' => 'Query processed successfully..',
        'ACCOUNT_NUMBER' => null);

    function __construct() {
        //$this->dbutils = new DbUtils();
        $this->log = new BeepLogger();
        $this->response = new REST();
        $this->coreUtils = new CoreUtils();
    }

    /**
     * 
     * @param type $params
     * @param type $requestMethod
     */
    public function accountBraodcast($params) {

        /**
         *  check is required params exists and set them.
         *  accountNumber is mandatory
         * */
        try {
            $mysql = new MySQL(
                Config::HOST, Config::USER, Config::PASSWORD, Config::DATABASE,
                Config::PORT
            );
            if (isset($params['accountNumber']) && !empty($params['accountNumber'])) {
                $accountNumber = $params['accountNumber'];
                $query = "SELECT accountNumber "
                    . " FROM customerProfiles WHERE accountNumber = ?";

                $bindParams = array('s', $accountNumber);
                $queryResult = DatabaseUtilities::executeQuery(
                        $mysql->mysqli, $query, $bindParams
                );
                $this->log->info(
                    Config::INFO, $accountNumber,
                    "About to check if account exist :::" .
                    print_r($queryResult, TRUE)
                );
                if (isset($queryResult) &&
                    !empty($queryResult)) {
                    $this->log->debug(
                        Config::INFO, $accountNumber,
                        "account not found ::: Calling create function ..." .
                        print_r($queryResult, TRUE));
                    $this->createAccount($params);
                }
                else {
                    $this->log->debug(
                        Config::INFO, $accountNumber,
                        "Account was found ::: Calling update function ..." .
                        print_r($queryResult, TRUE));
                    $this->createAccount($params);
                }
            }
            else {
                throw new Exception("Missing required param: accountNumber");
            }
        } catch (Exception $e) {
            return $this->response->response(
                    $this->failedResponse($e->getMessage()), 200);
        }
    }

    /**
     * stores card  sent from extenal application in hub
     * @param type $param
     */
    public function createAccount($params) {
        try {
            /**
             *  check is required params exists and set them.
             *  accountNumber is mandatory
             * */
            $mysql = new MySQL(
                Config::HOST, Config::USER, Config::PASSWORD, Config::DATABASE,
                Config::PORT
            );
            $payloadParams = array();
            $keys = '';
            $param = '';
            $values = '';
            if (isset($params['accountNumber']) && !empty($params['accountNumber'])) {
                $accountNumber = $params['accountNumber'];
                $queryParams['accountNumber'] = $accountNumber;
                $param = 'accountNumber';
                $values = '?';
                $keys = 's';
            }
            else {
                throw new Exception("Missing required param: accountNumber");
            }
            if (isset($params['accountBalance']) && !empty($params['accountBalance'])) {
                $accountBalance = $params['accountBalance'];
                $queryParams['accountBalance'] = $accountBalance;
                $keys .='s';
                $param .= ', accountBalance';
                $values .= ',?';
            }
            else {
                throw new Exception("Missing required param: accountBalance");
            }

            if (isset($params['MSISDN']) && !empty($params['MSISDN'])) {
                $MSISDN = $params['MSISDN'];
                $queryParams['MSISDN'] = $MSISDN;
                $keys .='i';
                $param .= ', MSISDN ';
                $values .= ',? ';
            }
            if (isset($params['availableTime']) && !empty($params['availableTime'])) {
                $availableTime = $params['availableTime'];
                $queryParams['availableTime'] = $availableTime;
                $keys .='i';
                $param .= ', availableTime';
                $values .= ',?';
            }
            else {
                throw new Exception("Missing required param: availableTime");
            }

            if (isset($params['originIP']) && !empty($params['originIP'])) {
                $originIP = $params['originIP'];
                $queryParams['originIP'] = $originIP;
                $keys .='i';
                $param .= ',originIP';
                $values .= ',?';
            }
            if (isset($params['timeStamp']) && !empty($params['timeStamp'])) {
                $timeStamp = $params['timeStamp'];
                $queryParams['dateCreated'] = $timeStamp;
                $keys .='s';
                $param .= ',dateCreated';
                $values .= ',?';
            }
            else {
                throw new Exception("Missing required param: timeStamp");
            }


            if (isset($accountNumber) && !empty($accountNumber)) {
                /**
                 * The code below creates a  new record with the provided 
                 * params 
                 */
//                $date = date('Y-m-d H:i:s');
//                $queryParams['dateCreated'] = $date;
//                $keys .='s';

                $query = "INSERT INTO coreRequests "
                    . "($param)"
                    . " values ($values)";

                array_unshift($queryParams, $keys);
                $this->log->debugLog(
                    Config::INFO, $MSISDN,
                    "About to create a record..::: query is "
                    . "$query With these params" . print_r($queryParams, TRUE)
                );
                $ss = DatabaseUtilities::insert(
                        $mysql->mysqli, $query, $queryParams
                );
                $this->log->debugLog(
                    Config::INFO, $MSISDN,
                    "About to create a record..::: query is "
                    . "$query With these params" . $ss
                );
                $coreRequestID = DatabaseUtilities::getInsertId($mysql->mysqli);
                if ($coreRequestID) {

                    $response = $this->coreUtils->select('coreRequests',
                        'coreRequestID', $coreRequestID);

                    $this->log->debugLog(
                        Config::INFO, $MSISDN,
                        "Insert Query was a success with ID:: "
                        . $coreRequestID
                    );
                    return $this->response->response(
                            $this->responseArray($response), 200);
                }
            }
            else {
                $this->log->errorLog(
                    Config::INFO, $MSISDN,
                    "Unable to retrieve account Number from sent params: "
                    . print_r($params, TRUE)
                );
                throw new Exception("Account number not set or empty");
            }
        } catch (badRequestException $e) {
            return $this->response->response(
                    $this->failedResponse(
                        $e->getMessage()), 400);
        } catch (Exception $e) {
            return $this->response->response(
                    $this->failedResponse(
                        $e->getMessage()), 400);
        }
    }

    public function responseArray($data) {

        $this->responseArray['ACCOUNT_NUMBER'] = $data[0]["accountNumber"];
        $this->log->debugLog(
            Config::INFO, 0,
            "Inside responseArray function:  "
            . "response was :::"
            . print_r($this->responseArray, true)
        );
        return json_encode($this->responseArray);
    }

    /**
     * This function updates core requests
     * 
     * @param type $params
     * @param type $requestMethod
     * @return type
     * @throws badRequestException
     * @throws Exception
     */
    public function updateAccount($params) {
        try {
            /**
             *  check is required params exists and set them.
             *  accountNumber is mandatory
             * */
            $mysql = new MySQL(
                Config::HOST, Config::USER, Config::PASSWORD, Config::DATABASE,
                Config::PORT
            );
            $payloadParams = array();
            $keys = '';
            $param = '';
            $values = '';
            if (isset($params['accountNumber']) && !empty($params['accountNumber'])) {
                $accountNumber = $params['accountNumber'];
                $queryParams['accountNumber'] = $accountNumber;
                $param = 'accountNumber';
                $values = '?';
                $keys = 'i';
            }
            else {
                throw new Exception("Missing required param: accountNumber");
            }
            if (isset($params['accountBalance']) && !empty($params['accountBalance'])) {
                $accountBalance = $params['accountBalance'];
                $queryParams['accountBalance'] = $accountBalance;
                $keys .='i';
                $param .= ', accountBalance';
                $values .= ',?';
            }

            if (isset($params['MSISDN']) && !empty($params['MSISDN'])) {
                $MSISDN = $params['MSISDN'];
                $queryParams['MSISDN'] = $MSISDN;
                $keys .='i';
                $param .= ', MSISDN ';
                $values .= ',? ';
            }

            if (isset($params['originIP']) && !empty($params['originIP'])) {
                $originIP = $params['originIP'];
                $queryParams['originIP'] = $originIP;
                $keys .='i';
                $param .= ',originIP';
                $values .= ',?';
            }
            if (isset($params['timeStamp']) && !empty($params['timeStamp'])) {
                $timeStamp = $params['timeStamp'];
                $queryParams['dateCreated'] = $timeStamp;
                $keys .='s';
                $param .= ',dateCreated';
                $values .= ',?';
            }

            if (isset($accountNumber) && !empty($accountNumber) && isset($timeStamp)) {
                /**
                 * The code below creates a  new record with the provided 
                 * params 
                 */
                $query = "INSERT INTO coreRequests "
                    . "($param)"
                    . " values ($values)";
                $this->log->debugLog(
                    Config::INFO, $MSISDN,
                    "About to crate a record..::: query is "
                    . "$query With these params" . print_r($queryParams, TRUE)
                );
                DatabaseUtilities::insert(
                    $mysql->mysqli, $query, $queryParams
                );
                $coreRequestID = DatabaseUtilities::getInsertId($mysql->mysqli);
                if ($coreRequestID) {

                    $response = $this->crud->select('coreRequests',
                        'coreRequestID', $coreRequestID);

                    $this->log->debugLog(
                        Config::INFO, $MSISDN,
                        "Insert Query was a success with ID:: "
                        . $coreRequestID
                    );
                    return $this->response->response(
                            $this->responseArray($response), 200);
                }
            }
            else {
                $this->log->errorLog(
                    Config::INFO, $MSISDN,
                    "Unable to retrieve account Number from sent params: "
                    . print_r($params, TRUE)
                );
                throw new Exception("Account number not set or empty");
            }
        } catch (badRequestException $e) {
            return $this->response->response(
                    $this->failedResponse(
                        $e->getMessage()), 400);
        } catch (Exception $e) {
            return $this->response->response(
                    $this->failedResponse(
                        $e->getMessage()), 400);
        }
    }

    /**
     * 
     * @param type $error
     */
    public function failedResponse($error) {

        $this->failedResponse['SUCCESS'] = "FALSE";
        $this->failedResponse['STATCODE'] = 0;
        $this->failedResponse['REASON'] = $error;
        $this->failedResponse['ACCOUNT_NUMBER'] = "NULL";
        $this->log->debugLog(
            Config::INFO, 0,
            "Inside failedResponse function:  "
            . "Failed response. Request was :::"
            . print_r($this->failedResponse, true)
        );
        echo json_encode($this->failedResponse);
    }

}
