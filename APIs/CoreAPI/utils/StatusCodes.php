<?php

/** File : statusCodes.php
 * @author: Lewis Kimani <lewis.kimani@cellulant.com>
 * 
 * This file formats response and adds the set statusCode Message on data return.
 */

class REST
{

    public $_allow = array();
    public $_content_type = "application/json";
    public $_request = array();
    private $_code = 200;

    /**
     * Formats the return response
     * @param type $data
     * @param type $status
     */
    public function response($data, $status) {
        $this->_code = ($status) ? $status : 200;
        $this->set_headers();
        echo $data;
    }

    /**
     * All available statuses tio be used in the API
     * @return type 
     */
    private function get_status_message() {
        $status = array(
            100 => 'Continue',
            101 => 'Switching Protocols',
            200 => 'OK',
            201 => 'Created',
            202 => 'Accepted',
            203 => 'Non-Authoritative Information',
            204 => 'Missing param(s)', //No Content  
            205 => 'Reset Content',
            206 => 'Partial Content',
            302 => 'Found',
            304 => 'Not Modified',
            307 => 'Temporary Redirect',
            400 => 'Bad Request',
            401 => 'Unauthorized',
            403 => 'Forbidden',
            404 => 'Function Not Found',
            406 => 'Not Acceptable',
            409 => 'Conflict',
            417 => 'Failed ', //Expectation Failed
            500 => 'Internal Server Error',
            501 => 'Not Implemented',
            502 => 'Bad Gateway',
            505 => 'HTTP Version Not Supported');
        return ($status[$this->_code]) ? $status[$this->_code] : $status[500];
    }

    /**
     * Sets return HTTP Headers
     */
    private function set_headers() {
        header("HTTP/1.1 " . $this->_code . " " . $this->get_status_message());
        header("Content-Type:" . $this->_content_type);
    }

}

/**
 * The following classes are used to handle various exceptions 
 *  encountered within the API functions.
 */
class badRequestException Extends Exception
{
    
}

?>