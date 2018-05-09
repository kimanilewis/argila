<?php

/**
 * @auther <kimanilewi@gmail.com>
 */

namespace Argila\ArgilaCoreAPI\Utilities;

Use Argila\ArgilaCoreAPI\Utilities\SyncLogger as logger;
Use Argila\ArgilaCoreAPI\Utilities\Database as dbConn;
use Argila\ArgilaCoreAPI\Config\Config as Config;
use Argila\ArgilaCoreAPI\Config\StatusCodes;
//use Argila\ArgilaCoreAPI\Models\sms_requests as sms;
use Ubench as benchmark;

class CoreUtils
{
    /*
     * log class.
     *
     */

    private $log;

    /**
     * db connection instance.
     */
    private $dbConn;

    /**
     * benchmark class.
     */
    private $benchmark;

    function __construct() {
        $this->log = new logger();
        $this->benchmark = new benchmark();
        $this->dbConn = new dbConn();
    }

    /**
     *
     * @param type $serviceCode
     * @return type
     */
    public function getService($serviceCode) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('s_services')
                ->where('serviceCode')->is($serviceCode)
                ->select(array(
                    'serviceID',
                    'serviceName',
                    'serviceCode',
                    'ownerClientID'))
                ->all();
            $this->log->sequel(Config::debug, $serviceCode,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) == 1) {
                $this->log->info(Config::info, $serviceCode,
                    "Query Sussefully executed. Results : "
                    . $this->log->printArray($result));
                $response = $result[0];
                $this->log->debug(Config::debug, $serviceCode,
                    "Result from check service using service Code query: "
                    . $this->log->printArray($result));
            } else {
                $this->log->info(Config::debug, $serviceCode,
                    "Unable to identify matching service : Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {

            $this->log->error(Config::error, -1,
                "Mysql Error while fetching service: " . $e->getMessage());
        } catch (Exception $e) {

            $this->log->error(Config::error, -1,
                "Error while fetching service details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     *
     * @param type $serviceCode
     * @return type
     */
    public function getServiceSetting($serviceID, $payerID) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('s_serviceSettings')
                ->where('serviceID')->is($serviceID)
                ->andWhere('payerClientID')->is($payerID)
                ->select(array(
                    'serviceSettingID',
                    'payerClientID'))
                ->all();
            $this->log->sequel(Config::debug, $serviceID,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) == 1) {
                $this->log->info(Config::info, $serviceID,
                    "Query Sussefully executed. Results : "
                    . $this->log->printArray($result));
                $response = $result[0];
                $this->log->debug(Config::debug, $serviceID,
                    "Result from check service using service Code query: "
                    . $this->log->printArray($result));
            } else {
                $this->log->info(Config::debug, $serviceID,
                    "Unable to identify matching service : Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {

            $this->log->error(Config::error, -1,
                "Mysql Error while fetching service: " . $e->getMessage());
        } catch (Exception $e) {

            $this->log->error(Config::error, -1,
                "Error while fetching service details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     *
     * @param type $currencyCode
     * @return type
     */
    public function getCurrency($currencyCode) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('currencies')->where('ISOCode')->is($currencyCode)
                ->select(array(
                    'ISOCode',
                    'currencyName',
                    'currencyID'))
                ->all();
            $this->log->sequel(Config::debug, $currencyCode,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) == 1) {
                $this->log->info(Config::info, $currencyCode,
                    "Query Sussefully executed. Results : "
                    . $this->log->printArray($result));
                $response = $result[0];
                $this->log->debug(Config::debug, $currencyCode,
                    "Result from check currency using currency code query: "
                    . $this->log->printArray($result));
            } else {
                $this->log->info(Config::debug, $currencyCode,
                    "Unable to identify matching currency : Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {

            $this->log->error(Config::error, -1,
                "Mysql Error while fetching currency details: " . $e->getMessage());
        } catch (Exception $e) {

            $this->log->error(Config::error, -1,
                "Error while fetching currency details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     * @return string
     */
    public function encryptData($payload) {
        $this->log->info(Config::info, -1, "Encrypting payload...");
        return hash("sha256", $payload);   //sha1(md5(rand(uniqid() . $payload, true)));
    }

    /**
     *
     * @param type $clientCode
     * @return type ARRAY
     */
    public function checkActiveSession($accountNumber) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('customer_accounts')
                ->where('accountNumber')->is($accountNumber)
                ->select(array(
                    'customerProfileAccountID',
                    'customerProfileID',
                    'expiryDate',
                    'MSISDN',
                    'customerName',
                    'startTime',
                    'processingStatus',
                    'expiryTime'))
                ->all();
            $this->log->sequel(Config::debug, $accountNumber,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $accountNumber,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records.");
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $accountNumber,
                    "Unable to identify a duplicate request: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     *
     * @param type $clientCode
     * @return type ARRAY
     */
    public function getLocation($venue_id) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('locations')
                ->where('venue_id')->is($venue_id)
                ->select(array(
                    'locationName',
                    'locationID',
                    'advert',
                    'active'))
                ->all();
            $this->log->sequel(Config::debug, $venue_id,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $venue_id,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records. Values" . $this->log->printArray($result));
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $userName,
                    "Unable to identify a duplicate request: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     *
     * @param type $accountNumber
     * @return type ARRAY
     */
    public function checkAccountProfile($accountNumber) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('customer_accounts')
                ->join('customerProfiles',
                    function($join) {
                    $join->on('customer_accounts.customerProfileID',
                        'customerProfiles.customerProfileID');
                })->where('accountNumber')->is($accountNumber)
                ->andWhere('customer_accounts.active')->is(Config::ACTIVE)
                ->select(array(
                    'accountNumber',
                    'customerName',
                    'customerProfileAccountID',
                    'balanceCarriedForward',
                    'expiryDate',
                    'MSISDN',
                    'processingStatus',
                    'customerProfiles.customerProfileID',
                    'customer_accounts.active'))
                ->all();

            $this->log->sequel(Config::debug, $accountNumber,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $accountNumber,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records.");
                $response = $result[0];
               } else {
                $this->log->info(Config::debug, $accountNumber,
                    "Record not found request: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    /**
     *
     * @param type $serviceName
     * @return type ARRAY
     */
    public function checkCardDetails($accountNumber) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->from('card_accounts')
                ->join('locations',
                    function($join) {
                    $join->on('card_accounts.locationID', 'locations.locationID');
                })
                ->where('accountNumber')->is($accountNumber)
                ->andWhere('locations.active')->is(Config::ACTIVE)
                ->andWhere('card_accounts.active')->is(Config::CREATED)
                ->select(array(
                    'card_id',
                    'accountNumber',
                    'card_accounts.locationID',
                    'locations.locationName',
                    'card_accounts.active'))
                ->all();
            $this->log->sequel(Config::debug, $accountNumber,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $accountNumber,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records.");
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $accountNumber,
                    "Unable to identify card details: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return json_decode(json_encode($response), True);
    }

    public function generateBusinessPin() {
        return rand(10000000, 99999999);
    }

    public static function formatMSISDN($MSISDN, $internationalMode = FALSE) {

        // default the country-dial code
        $countryDialCode = Config::COUNTRY_DIAL_CODE;
        try {
            //Sanitize the MSISDN
            $formatedMSISDN = preg_replace("/[^0-9\s]/", "", $MSISDN);

            //Get rid of the leading 0
            if ((substr($formatedMSISDN, 0, 1) == "0") && (strlen($formatedMSISDN)
                == 10)) {
                $formatedMSISDN = substr_replace($formatedMSISDN, "", 0, 1);
            }

            // If the # is less than the countries #
            if (strlen($formatedMSISDN) <= 9 && strlen($formatedMSISDN) > 0) {
                $formatedMSISDN = $countryDialCode . $formatedMSISDN;
                // If it is in international mode we apppend a  +
                if ($internationalMode) {
                    $formatedMSISDN = "+" . $formatedMSISDN;
                }
            }
        } catch (Exception $exc) {
            $flogParams = array(
                'MSISDN' => $formatedMSISDN);
            CoreUtils::flog(2, $flogParams,
                "Error formating the MSISDN  \n" . $exc->getMessage(),
                "\n" . __CLASS__, __FUNCTION__, __LINE__);
        }

        return trim($formatedMSISDN);
    }

    /**
     * Update request status
     * @param type $primaryKeyID
     * @param type $primaryIDName
     * @param type $tableName
     * @param Array $params Params to be updated
     * @return type
     */
    function updateQuery($primaryKeyID, $primaryIDName, $tableName, $params) {
        $response = array();
        $this->log->info(Config::info, $primaryKeyID,
            "About to update record : "
            . " Parameters: " . $this->log->printArray($params));
        try {
            $db = $this->dbConn->DBConn();
            //Results for update query
            $result = $db->update($tableName)
                ->where($primaryIDName)->is($primaryKeyID)
                ->set($params);
            $this->log->sequel(Config::debug, $primaryKeyID,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            if (count($result) > 0) {
                $this->log->info(Config::info, $primaryKeyID,
                    "Query Sussefully executed. Results : " . $result);
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $primaryKeyID,
                    "Unable to update request: Results " . $result);
            }
        } catch (SQLException $e) {

            $this->log->error(Config::error, -1,
                "Mysql Error while updating request details: "
                . $e->getMessage());
        } catch (Exception $e) {

            $this->log->error(Config::error, -1,
                "Error while updating request details: "
                . $e->getMessage());
        }
        return $response;
    }

    ////make a post to the users table
    public static function post($url, $fields) {
        $fields_string = null;
        $ch = curl_init();
        //set the url, number of POST vars, POST data
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_NOSIGNAL, 1);
        curl_setopt($ch, CURLOPT_POST, count($fields));
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        //execute post
        $result = curl_exec($ch);
        //close connection
        curl_close($ch);
        return $result;
    }

    /**
     *
     * @param type $statusDesc
     * @param type $msisdn
     * @param type $message
     * @param type $template
     * @return type ARRAY
     */
    public function logSMS($statusDesc, $msisdn, $message, $template) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->insert(array(
                    'smsID' => null,
                    'templateID' => $template,
                    'destination' => $msisdn,
                    'message' => $message,
//                    'status' => StatusCodes::ACTIVE,
                    'statusDesription' => $statusDesc,
                    'dateCreated' => date(Config::DATE_TIME_FORMAT),
                ))
                ->into('sms_requests');
            $this->log->sequel(Config::debug, $msisdn,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $msisdn,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records. Values: " . $this->log->printArray($result));
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $msisdn,
                    "No duplicate user: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return $response;
    }

    public function logSession($customerProfileAccountID, $locationID, $statusDesc) {
        try {
            $db = $this->dbConn->DBConn();
            //Results for a user
            $result = $db->insert(array(
                    'sessionDataID' => null,
                    'customerProfileAccountID' => $customerProfileAccountID,
                    'LocationID' => $locationID,
                    'syncStatus' => StatusCodes::ACTIVE,
//                    'status' => StatusCodes::ACTIVE,
                    'syncStatusDescription' => $statusDesc,
                    'dateCreated' => date(Config::DATE_TIME_FORMAT),
                ))
                ->into('session_data');
            $this->log->sequel(Config::debug, $customerProfileAccountID,
                "Execute Query : "
                . $this->log->printArray($db->getLog()));
            $response = array();
            if (count($result) > 0) {
                $this->log->info(Config::info, $customerProfileAccountID,
                    "Query Sussefully executed. Found " . count($result)
                    . " Records. Values: " . $this->log->printArray($result));
                $response = $result[0];
            } else {
                $this->log->info(Config::debug, $customerProfileAccountID,
                    "No duplicate user: Results "
                    . $this->log->printArray($result));
            }
        } catch (SQLException $e) {
            $this->log->error(Config::error, -1,
                "Mysql Error while fetching request details: "
                . $e->getMessage());
        } catch (Exception $e) {
            $this->log->error(Config::error, -1,
                "Error while fetching request details: " . $e->getMessage());
        }
        return $response;
    }

    function formatMessage($message, $params = array()) {

        $this->log->sequel(Config::debug, 0,
            "message params : "
            . $this->log->printArray($params));
        foreach ($params as $param => $value) {
            $param = strtoupper($param);
            $message = str_replace($param, $value, $message);
        }

        return $message;
    }

    /*
     * Create a random string
     * @param $length the length of the string to create
     * @return $str the string
     */

    function randomCode($length) {
        $str = "";
        $characters = array_merge(range('A', 'Z'), range('a', 'z'),
            range('0', '9'));
        $max = count($characters) - 1;
        for ($i = 0; $i < $length; $i++) {
            $rand = mt_rand(0, $max);
            $str .= $characters[$rand];
        }
        return $str;
    }

}
