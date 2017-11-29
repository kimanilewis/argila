<?php

/**
 * This file holds the Authenticator class used to authenticate the API Users.
 *
 * PHP VERSION 5.3.6
 *
 * @category  Core
 * @package   Authenticator
 * @author    Daniel Mbugua <daniel.mbugua@cellulant.com>
 * @copyright 2013 Cellulant Ltd
 * @license   Proprietory License
 * @link      http://www.cellulant.com
 */

/**
 * This class holds the functions used to authenticate the API Users.
 *
 * @category  ProfilingAPI
 * @package   Authenticator
 * @author    Lewis Kimani<lewis.kimani@cellulant.com>
 * @copyright 2016 Cellulant Ltd
 * @license   Proprietory License
 * @version   Release:3.1.0  // changed to work with profilling API
 * @link      http://www.cellulant.com
 */
class Authenticator
{

    /**
     * Log class instance.
     *
     * @var object
     */
    private $log;
    private $response;
    private $dbutils;
    private $crud;
    private $hasher;

    /**
     * Class constuctor.
     */
    public function __construct() {
        $this->log = new BeepLogger();
        $this->dbutils = new DbUtils();
        $this->crud = new profilingCrudLibrary();
        $this->hasher = new PasswordHash(8, false);
        $this->response = new REST();
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

        $logCredentials = $credentials;
        $logCredentials['password'] = "****************";
        $this->log->debugLog(Config::DEBUG, -1,
            "Credentials used for authentication:"
            . $this->log->printArray($logCredentials));
        $passwdStatusID = 1;
        $canAccessUI = 0;
        $active = 1;
        $response = array();
        // Verify to see that all the credentials params are presient
        if (!empty($credentials['password']) && !empty($credentials['username'])  ) {
            // Username of invoking user
            $username = $credentials['username'];
            // The password of invoking user
            $password = $credentials['password'];
            //The client code of the client being represented
            $clientCode = isset($credentials['clientCode']) ?
                $credentials['clientCode'] : "";

            if ($username != null && $password != null) {
                
                $response['STATUS'] = FALSE;

                /*
                 * Try to use stronger but system-specific hashes, with
                 * a possible fallback to the weaker portable hashes.
                 */
//                $hasher = new PasswordHash(8, false);
                $query = "SELECT userName, password, c.clientID, "
                    . "c.clientCode, userID, canAccessUI, "
                    . "passwordStatusID, u.active "
                    . "FROM users u INNER JOIN clients c "
                    . "ON c.clientID = u.clientID WHERE username = ? ";

                $params = array($username);
                $QueryResult = $this->dbutils->query($query, FALSE, $params);
                $this->log->infoLog(
                    Config::INFO, 0,
                    "Inside Authenticator function:  "
                    . "Failed since request method was not post."
                    . " $query Request was :::" . print_r($QueryResult, true)
                );
                if (count($QueryResult['DATA']['RESULTS']) == 1)    {
                    $this->log->infoLog(
                        Config::INFO, 0,
                        "Inside Authenticator function:  "
                        . "user exists  "
                        . " Request was :::" . print_r($QueryResult, true)
                    );
                    $result = $QueryResult['DATA']['RESULTS'];
                    if ($result[0]['canAccessUI'] != $canAccessUI) {

                        $this->log->errorLog(Config::ERROR, -1,
                            "Access denied -> User with username '"
                            . $username . "' can only access the User "
                            . "Interface");

                        $response['REASON'] = "USER_AUTHENTICATION_FAILED: None api user";
                        return $response;
                    }

                    if ($result[0]['active'] != $active) {
                        $this->log->errorLog(Config::ERROR, -1,
                            "Access denied -> User with username '"
                            . $username . "' is inactive");

                        $response['REASON'] = "USER_AUTHENTICATION_FAILED: inactive user";
                        return $response;
                    }

                    if ($result[0]['passwordStatusID'] != $passwdStatusID) {
                        $this->log->errorLog(Config::ERROR, -1,
                            "Access denied -> User with username '"
                            . $username . "' has an inactive password");

                        $response['REASON'] = "USER_AUTHENTICATION_FAILED: inactive password";
                        return $response;
                    }

                    //Fetch the stored password from the DB
                    $storedPass = $result[0]['password'];
                    $check = $this->hasher->CheckPassword($password, $storedPass);

                    if ($check) {
                        $response['STATUS'] = TRUE;
                        $response['clientCode'] = $result[0]['clientCode'];
                        $response['clientID'] = $result[0]['clientID'];
                        $response['userID'] = $result[0]['userID'];

                        $this->log->infoLog(Config::INFO, -1,
                            "User " . $credentials['username']
                            . " with userID: " . $response['userID']
                            . " has been authenticated. User belongs to: "
                            . $result[0]['clientCode']);
                        return $response;
                    }
                    else {
                        $response['STATUS'] = FALSE;
                        $response['REASON'] = "User " . $credentials['username']
                            . " has failed authentication. ";
                        $this->log->errorLog(Config::ERROR, -1,
                            "User " . $credentials['username']
                            . " has failed authentication. ");
                    }
                    return $response;
                }
                else {
                    $response['STATUS'] = FALSE;
                    $response['REASON'] = "User " . $credentials['username']
                        . " Does not exist in the system.";
                    $this->log->errorLog(Config::ERROR, -1,
                        "User " . $credentials['username']
                        . " Does not exist in the system. Returning: "
                        . $this->log->printArray($response['statusInfo']));
                    return $response;
                }
            }


            return $response;
        }
        else {
            $response['STATUS'] = FALSE;
            $this->log->errorLog(
                Config::ERROR, 0,
                "Username or password not supplied. " . "Authentication not possible." . print_r($auth , TRUE)
            );
            $this->log->errorLog(Config::ERROR, -1,
                "Username or password not supplied. " . "Authentication not possible.");

            /*
             * Authentication cannot be performed so exit function with
             * appropriate status code.
             */
            if ($username == null || empty($username)) {
                $response['REASON'] = "Username not "
                    . "supplied. Authentication not possible, exiting.";
            }
            elseif ($password == null || empty($password)) {
                $response['REASON'] = "Password not "
                    . "supplied. Authentication not possible.";
            }

            $this->log->errorLog(Config::ERROR, -1,
                "Authentication not possible."
                . $this->log->printArray($response["REASON"]));


            return $response;
        }
    }

}
