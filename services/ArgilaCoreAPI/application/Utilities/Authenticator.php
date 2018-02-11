<?php

namespace Argila\ArgilaCoreAPI\Utilities;

Use Argila\ArgilaCoreAPI\Utilities\SyncLogger as Logger;
Use Argila\ArgilaCoreAPI\Utilities\PasswordHash as PasswordHash;
Use Argila\ArgilaCoreAPI\Utilities\Database as dbConn;
use Argila\ArgilaCoreAPI\Config\Config as Config;
use Argila\ArgilaCoreAPI\Config\StatusCodes as StatusCodes;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Authenticate
 *
 * @author 
 */
class Authenticator
{

    /**
     * Log class instance.
     *
     * @var object
     */
    private $log;

    /**
     * db connection instance.
     *
     * @var object
     */
    private $dbConn;

    /**
     * Class constuctor.
     */
    public function __construct() {
        $this->log = new Logger();
        $this->dbConn = new dbConn();
    }

    /**
     * This function is used as the main authenticator with all the procedures
     * required. The function returns the following status codes and
     * descriptions.
     * <ul>
     * <li>131 | Authentication Successful => All parameters are ok and the user
     * is active</li>
     * <li>132 | Authentication failed => User cannot access API, only UI</li>
     * <li>132 | Authentication failed => User is inactive or user password is
     * inactive</li>
     * <li>132 | Authentication failed => Username does not exist</li>
     * <li>132 | Authentication failed => Internal server error</li>
     * <li>132 | Authentication failed => Credential payload is null or does not
     * exist</li>
     * <li>163 | Username not provided => The username is not provided in the
     * payload</li>
     * <li>164 | Password not provided => The password is not provided in the
     * payload</li>
     * <li>105 | INACTIVE_CLIENT => The client is inactive</li>
     * </ul>
     *
     * @param array $credentials array carrying the username and password
     *
     * @return mixed Array containing the authentication status and additional
     *               information.
     */
    public function authenticate($credentials) {
        try {
            $logCredentials = $credentials;
            $logCredentials['token'] = "****************";
            $this->log->debug(Config::debug, -1,
                "Credentials used for authentication:"
                . $this->log->printArray($credentials));
            $active = 1;
            // Verify to see that all the credentials params are presient
            if ($credentials != null) {
                // Username of invoking user
                $location = $credentials['location_id'];
                // The password of invoking user
                $token = $credentials['token'];
                //The client code of the client being represented
                $clientCode = isset($credentials['clientCode']) ?
                    $credentials['clientCode'] : "";
                if ($location != null && $token != null) {
                    $authStatus = array();
                    try {
                        $response = array();
                        //pasword hasher
                        $hasher = new PasswordHash(8, false);
                        /*
                         * connect to mysql database and check if a user of the
                         *  provided credentials exists
                         */
                        $db = $this->dbConn->DBConn();
                        //Results for a user
                        $result = $db->from('locations')->where('LocationID')->is($location)
                            ->select(array('token', 'locationName',
                                'active'))
                            ->all();
                        $this->log->sequel(Config::debug, -1,
                            "Execute Query : "
                            . $this->log->printArray($db->getLog()));

                        if (count($result) == 1) {
                            $this->log->info(Config::info, -1,
                                "Query Sussefully executed. Results : "
                                . $this->log->printArray($result));
                            $result = $result[0];
                            $this->log->debug(Config::debug, -1,
                                "Result from check user query: "
                                . $this->log->printArray($result));

                            if ($result->active != $active) {
                                $response['statusInfo'] = array(
                                    "authStatusCode" => StatusCodes::CLIENT_AUTHENTICATION_FAILED,
                                    "authStatusDescription" => "User with "
                                    . "username '" . $location . "' is inactive");
                                $this->log->error(Config::error, -1,
                                    "Access denied -> User with username '"
                                    . $location . "' is inactive");


                                return $response;
                            }

                            if ($result->token != $token) {
                                $response['statusInfo'] = array(
                                    "authStatusCode" => StatusCodes::CLIENT_AUTHENTICATION_FAILED,
                                    "authStatusDescription" => "User with "
                                    . "location '" . $location
                                    . "'  invalid token provided ");


                                $this->log->error(Config::error, -1,
                                    "Access denied -> User with username '"
                                    . $location . "' has provided invalid token");


                                return $response;
                            }if ($result->token != $token) {

                                $response['clientCode'] = $result->clientCode;
                                $response['clientID'] = $result->clientID;
                                $response['userID'] = $result->userID;

                                $authStatus['authStatusCode'] = StatusCodes::CLIENT_AUTHENTICATED_SUCCESSFULLY;
                                $authStatus['authStatusDescription'] = "Authentication was successful.";
                                $response['statusInfo'] = $authStatus;



                                $this->log->info(Config::info, -1,
                                    "User " . $credentials['username']
                                    . " with userID: " . $response['userID']
                                    . " has been authenticated. User belongs to: "
                                    . $result->clientCode);
                            }

                            } else {
                            $response['statusInfo'] = array("authStatusCode" => StatusCodes::CLIENT_AUTHENTICATION_FAILED,
                                "authStatusDescription" => "User with the provided "
                                . "username does not exist");

                            $this->log->error(Config::error, -1,
                                "User " . $credentials['token']
                                . " Does not exist in the system. Returning: "
                                . $this->log->printArray($response['statusInfo']));
                        }
                    } catch (SQLException $e) {

                        $this->log->error(Config::error, -1,
                            "Mysql Error while authenticating: " . $e->getMessage());

                        $response['statusInfo'] = array("authStatusCode" => StatusCodes::CLIENT_AUTHENTICATION_FAILED,
                            "authStatusDescription" => "Internal server Error. " . "Failed authentication");
                    } catch (Exception $e) {

                        $this->log->error(Config::error, -1,
                            "Error while authenticating: " . $e->getMessage());

                        $response['statusInfo'] = array("authStatusCode" => StatusCodes::CLIENT_AUTHENTICATION_FAILED,
                            "authStatusDescription" => "Internal server Error. " . "Failed authentication");
                    }


                    return $response;
                } else {
                    $this->log->error(Config::error, -1,
                        "Username or password not supplied. " . "Authentication not possible.");

                    /*
                     * Authentication cannot be performed so exit function with
                     * appropriate status code.
                     */
                    if ($location == null || empty($location)) {
                        $authStatus['authStatusCode'] = StatusCodes::CLIENT_AUTHENTICATION_FAILED;
                        $authStatus['authStatusDescription'] = "Username not "
                            . "supplied. Authentication not possible, exiting.";
                    } elseif ($token == null || empty($token)) {
                        $authStatus['authStatusCode'] = StatusCodes::CLIENT_AUTHENTICATION_FAILED;
                        $authStatus['authStatusDescription'] = "Password not "
                            . "supplied. Authentication not possible.";
                    }

                    // Formulating the response
                    $response["statusInfo"] = $authStatus;

                    $this->log->error(Config::error, -1,
                        "Authentication not possible."
                        . $this->log->printArray($response["statusInfo"]));


                    return $response;
                }
                return $response;
            } else {
                $authStatus['authStatusCode'] = StatusCodes::CLIENT_AUTHENTICATION_FAILED;
                $authStatus['authStatusDescription'] = "Credentials payload "
                    . "section is missing or null. Authentication not possible";

                // Formulate the response
                $response["statusInfo"] = $authStatus;

                $this->log->error(Config::error, -1,
                    "Credentials payload missing or null. "
                    . "Authentication not possible. Response: "
                    . $this->log->printArray($response["statusInfo"]));
                return $response;
            }
        } catch (SQLException $e) {

            $this->log->error(Config::error, -1,
                "Error while authenticating: " . $e->getMessage());

            $response['statusInfo'] = array("authStatusCode" => StatusCodes::GENERIC_FAILURE_STATUS_CODE,
                "authStatusDescription" => "Internal server Error. " . "Failed authentication");
        } catch (Exception $e) {

            $this->log->error(Config::error, -1,
                "Error while authenticating: " . $e->getMessage());

            $response['statusInfo'] = array("authStatusCode" => StatusCodes::GENERIC_FAILURE_STATUS_CODE,
                "authStatusDescription" => "Internal server Error. " . "Failed authentication");
        }
        $this->log->error(Config::error, -1,
            "Authentication failed authStatus to be returned: " . $this->log->printArray($response["statusInfo"]));
        return $response;
    }

}
