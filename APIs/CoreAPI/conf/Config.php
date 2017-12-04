<?php

/**
 * Profiling API functions configuration file.
 * @category  Profile
 * @package   ProfilingAPI
 * @author    Lewis Kimani <lewis.kimani@cellulant.com>
 * 

 * Contains common configuration information that will be used accross all the
 * API files/classes.
 *
 * @copyright 2016 Cellulant Ltd
 */
class Config
{

    /**
     * Database host name or IP address.
     *
     * @var string
     */
    const DB_DSN = "mysql:dbname=argila;host=localhost";

    /**
     * Database user name.
     *
     * @var string
     */
    const HOST = "localhost";

    /**
     * Database user name.
     *
     * @var string
     */
    const USER = "root";

    /**
     * Database password.
     *
     * @var string
     */
    const PASSWORD = "lewie";

    /**
     * Database name.
     *
     * @var string
     */
    const DATABASE = "argila";

    /**
     * Database port in use.
     *
     * @var string
     */
    const PORT = "3306";
    
    const AUTHORIZATION_KEY="7dg20Vt2XEx5TYo1wjxV106kTROcriwM";

    /**
     * File location for info logs.
     *
     * @var string
     */
    const INFO = "/var/log/applications/Argila/CoreAPI/info.log";

    /**
     * File location for error logs.
     *
     * @var string
     */
    const ERROR = "/var/log/applications/Argila/CoreAPI/error.log";

    /**
     * File location for fatal logs.
     *
     * @var string
     */
    const FATAL = "/var/log/applications/ke/hub4/hubChannels/profilingAPI/fatal.log";

    /**
     * File location for debug logs.
     *
     * @var string
     */
    const DEBUG = "/var/log/applications/ke/hub4/hubChannels/profilingAPI/debug.log";
    const USSD_LOG_PROPERTIES = "/var/log/applications/ke/hub4/hubChannels/profilingAPI/profilingAPI_logs.properties";

    /**
     * Post method constant.
     */
    Const POST_METHOD = "POST";

    /**
     * Update PUT method Constant.
     */
    Const PUT_METHOD = "PUT";

    /**
     * DELETE method Constant.
     */
    Const DELETE_METHOD = "DELETE";

    /**
     * GET method Constant.
     */
    Const GET_METHOD = "GET";

}
