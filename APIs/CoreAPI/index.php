<?php

/**
 * Core API
 * This file holds the profiling class with redirect to functions for profile processing.
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
 * Class will execute the function dynamically;
 * */
date_default_timezone_set('Africa/Nairobi');

require 'utils/RequireUtils.php';

class API extends REST
{

    private $log;
    private $coreRequests;

    public function __construct() {
        $this->log = new BeepLogger();
        $this->coreRequests = new CoreRequests();
    }

    /*
     * Public method for core requests. It processes the request.
     * This method dynmically call the method based on the query string
     *
     */

    public function processApi() {
        $post = file_get_contents('php://input');

        $this->log->debugLog(
            Config::INFO, 0,
            "file_get_contents..... " . print_r($post, true)
            . "| \n and _POST had  " . print_r($_POST, TRUE)
        );
        $headers = apache_request_headers();
        $this->log->debugLog(
            Config::INFO, 0,
            "HEADERS as received::: | " . print_r($headers, true)
        );
        if (isset($headers['Authorization']) &&
            ($headers['Authorization']) == Config::AUTHORIZATION_KEY) {
            $this->log->debugLog(
                Config::INFO, 0, "Pass key verified successfully "
            );
        } else {
            $this->log->debugLog(
                Config::INFO, 0, "unable to verify Pass key"
            );
            $resp = $this->coreRequests->failedResponse("Authorization key could not be verified");
            $this->response($resp, 404);
            exit();
        }
        if (isset($post) && !empty($post)) {
            $params = json_decode($post, TRUE);
        } elseif (isset($_POST) && !empty($_POST)) {
            $params = $_POST;
        } else {
            $resp = $this->coreRequests->failedResponse("Unrecognised request."
                . " Only POST is accepted");
            $this->response($resp, 404);
            exit();
        }
        $this->log->debugLog(
            Config::INFO, 0,
            "Function to invoke is:::: | accountBraodcast" .
            "| \n and params are " . print_r($params, TRUE)
        );


        /* check if the function exists.
         * if exist, redirect to that fuction and send params as well
         */

        /**
         * core requests class file
         */
        if (method_exists($this->coreRequests, 'accountBraodcast')) {
            $this->log->debugLog(
                Config::INFO, 0,
                "About to invoke coreRequests handler :::: | " . $functionName . "|"
                . " \n and params are " . print_r($params, TRUE)
            );
            $this->coreRequests->accountBraodcast($params);
        } else {
            /*
             *  If the method not exist with in this class,
             * response would be "Function Not Found".
             */
            $resp = $this->coreRequests->failedResponse("Method Not Allowed");
            $this->response($resp, 404);
        }
    }

}

// Initiate API

$api = new API;
$api->processApi();
?>
