<?php

/**
 * @author Lewis Kimani <kimanilewi@gmail.com>
 */

namespace Argila\ArgilaCoreAPI\Controllers;

use Argila\ArgilaCoreAPI\Utilities\SyncLogger as logger;
use Argila\ArgilaCoreAPI\Utilities\Encryption;
use Argila\ArgilaCoreAPI\Config\Config;
use Argila\ArgilaCoreAPI\Config\StatusCodes as StatusCodes;
use Argila\ArgilaCoreAPI\Utilities\Authenticator as Authenticator;
use Exception;

class RoutesController
{

    /**
     * @var logger
     * log classs instance
     */
    private $log;

    /**
     * @var
     * post data
     */
    private $data;

    /**
     * @var
     */
    private $response;

    function __construct() {
        $this->log = new logger();
    }

    /**
     *
     * @param $request
     * @param $response
     * runs the application
     */
    public function run($request, $response) {
        $requestData = $request->body();
        $route = $request->params()[0];
        $route = str_replace("/", "", $route);
        $this->data =  json_decode($requestData, true);
        $this->log->info(Config::info, -1,
            "Received $route request  with parameters :"
            . $this->log->printArray( $this->data ));
        $this->data['route'] = $route;
        $this->response = $response;
        $this->log->info(Config::info, -1,
            "Server Request : "
            . $this->log->printArray($_SERVER));
        $resultCredentials = $this->authenticate($this->data['credentials']);
        if ($resultCredentials["authStatusCode"] ===
            StatusCodes::CLIENT_AUTHENTICATION_FAILED){
            $this->response = array(
                'authStatus' => $resultCredentials,
                'results' => "");
            $this->log->info(Config::info, -1,
                "Authentication failed request. Response: "
                . $this->log->printArray($resultCredentials));
        } else{
            $result = $this->route($this->data);
            $this->log->info(Config::info, -1,
                " Controller request. Response: "
                . $this->log->printArray($result));
            $this->response = $result;
        }

        header('Content-Type: application/json');
        echo json_encode($this->response);
    }

    /**
     * @param $request
     * @TODO figure out how the decryption will work
     * @TODO figure out how the authentication will work
     */
    public function decryptRequest($request) {
        $encryption = new Encryption();
        $encryption->decrypt($request);
    }

    /**
     * @param $credentials
     * @TODO figure out how the decryption will work
     * @TODO figure out how the authentication will work
     */
    public function authenticate($credentials) {

        $authentication = new Authenticator();
        $authResponse = $authentication->authenticate($credentials);
        $this->log->debug(Config::debug, -1,
            "Result from Authenctication: "
            . $this->log->printArray($authResponse));
        $aStat = $authResponse['statusInfo']['authStatusCode'];
        if ($aStat == null || $aStat != StatusCodes::CLIENT_AUTHENTICATED_SUCCESSFULLY
        ) {
//            $response1['authStatus'] = $authResponse['statusInfo'];
//            $response1["results"] = array();

            $response1 = Config::FAILED_RESPONSE;
            return json_encode($response1);
        }
        $authInfor = $authResponse['statusInfo'];
        return $authInfor;
    }

    /**
     * @param $function
     * @param $request
     * @throws Exception
     */
    public function route($request) {

        $this->log->debug(Config::debug, -1,
            "Incoming Result  "
            . $this->log->printArray($request));
        $result = array();
        switch (strtolower($request['route'])) {

            case 'pos':
                $SyncRequest = new PosController();
                $result = $SyncRequest->processPosRequest($request);
                break;
            case 'mpesa_request':
                $servicesRequest = new MpesaController();
                $result = $servicesRequest->processMpesaRequest($request);
                break;
            default :
                $result['Status'] = "The function Specified is Invalid";
                break;
        }

        $this->log->debug(Config::debug, -1,
            "Response Result...........  "
            . $this->log->printArray($result));
        return $result;
    }

}
