<?php

require_once "BenchMarkConfig.php";
require_once "logger/AppLogger.php";

/**
 * Its a class for benchmarking the execution time for any given function or
 * statement its initialised by creating an object variable of it.
 *
 * It also logs this information in log files that are accessed by monitoring
 * application for information analysis
 *
 * PHP VERSION 5.3.6
 *
 * @category  CPG
 * @package   BenchMark
 * @author Oscar Oluoch <oscar.oluoch@cellulant.com>
 * Improvements from original done by:
 * @author Daniel Mbugua <daniel.mbugua@cellulant.com
 * @copyright 2016 Cellulant Ltd
 * @license   Proprietory License
 * @link      http://www.cellulant.com
 */
class BenchMark
{

    /**
     * This stored the parameter of the validation
     *
     * @var array
     */
    private $limit;

    /**
     * The logging object
     *
     * @var array
     */
    private $log;

    /**
     * The sessionID of the invocation
     *
     * @var type
     */
    private $sessionID;

    /**
     *
     */
    private $levelNames = array(
        self::OVERALL_LEVEL => "OVERALL",
        self::FUNCTION_LEVEL => "FUNCTION",
        self::DATABASE_LEVEL => "DATABASE",
        self::THIRDPARTY_LEVEL => "THIRDPARTY",
        5 => "UNDEFINED"
    );

    /**
     * Constant valie for total TAT time level
     */
    const OVERALL_LEVEL = 1;

    /**
     * Constant valie for function TAT time level
     */
    const FUNCTION_LEVEL = 2;

    /**
     * Constant valie for query TAT time level
     */
    const DATABASE_LEVEL = 3;

    /**
     * Constant valie for external invocation TAT time level
     */
    const THIRDPARTY_LEVEL = 4;

    /**
     * This is the session identifier for the invocation
     *
     * @param type $sessionID The session identifier of the invocation
     */
    function __construct($sessionID) {
        $this->limit = array();
        $this->sessionID = $sessionID;
        $this->log = new AppLogger($this->sessionID);
    }

    /**
     * The start function used to start the timing process.
     *
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    public function start($level, $method, $uniqueid = -1) {
        $this->limit[$this->sessionID][$method][$level][$uniqueid]['start'] = microtime(TRUE);
    }

    /**
     * It marks the end of the benchmark.
     *
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    private function end($level, $method, $uniqueid) {
        $this->limit[$this->sessionID][$method][$level][$uniqueid]['end'] = microtime(TRUE);
    }

    /**
     * Determines the time taken for the process.
     *
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     *
     * @return type
     *
     * @throws Exception
     */
    private function elapsed_time($level, $method, $uniqueid) {
        if (!isset($this->limit[$this->sessionID][$method][$level][$uniqueid]['start'])
            || empty($this->limit[$this->sessionID][$method][$level][$uniqueid]['start'])) {
            throw new Exception("The start time is not set correctly");
        } else {
            $this->end($level, $method, $uniqueid);
            return sprintf("%.4f",
                $this->limit [$this->sessionID][$method][$level][$uniqueid]['end']
                - $this->limit[$this->sessionID][$method][$level][$uniqueid]['start']);
        }
    }

    /**
     * Logs the turn around time in a TAT_INFO file in the logs folder on
     * different levels
     *
     * @param type $level The TAT tracking level
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    public function logTAT($level, $method, $uniqueid = -1) {

        switch ($level) {
            CASE 1:
                $this->logOverallTAT($level, $method, $uniqueid);
                break;
            CASE 2:
                $this->logFunctionTAT($level, $method, $uniqueid);
                break;
            CASE 3:
                $this->logDatabaseTAT($level, $method, $uniqueid);
                break;
            CASE 4:
                $this->logThirdPartyInvocationTAT($level, $method, $uniqueid);
                break;
            DEFAULT:
                $this->log->errorLog(
                    BenchMarkConfig::TAT_ERROR, $uniqueid,
                    "| UNDEFINED | $method | TAT Log level not set "
                );
        }
    }

    /**
     * Logs the turn around time in a TAT_INFO file in the logs folder
     *
     * @param type $level The TAT tracking level
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    private function logFunctionTAT($level, $method, $uniqueid) {
        try {
            $this->log->infoLog(
                BenchMarkConfig::TAT_INFO, $uniqueid,
                $this->levelNames[$level] . " | $method | FUNCTION RUN TIME: "
                . $this->elapsed_time($level, $method, $uniqueid)
                . " SECONDS | ELAPSED MEMORY: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                BenchMarkConfig::TAT_ERROR, $uniqueid,
                $this->levelNames[$level] . " | $method | EXCEPTION CAUGHT: "
                . $ex->getMessage()
            );
        }
        unset($this->limit[$this->sessionID][$method][$level][$uniqueid]);
    }

    /**
     * Logs the total turn around time at entry point in a TAT_INFO file in
     * the logs folder
     *
     * @param type $level The TAT tracking level
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    private function logOverallTAT($level, $method, $uniqueid) {
        try {
            $this->log->infoLog(
                BenchMarkConfig::TAT_INFO, $uniqueid,
                $this->levelNames[$level] . " | $method | OVERALL RUN TIME: "
                . $this->elapsed_time($level, $method, $uniqueid)
                . " SECONDS | ELAPSED MEMORY: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                BenchMarkConfig::TAT_ERROR, $uniqueid,
                $this->levelNames[$level] . " | $method | EXCEPTION CAUGHT: "
                . $ex->getMessage()
            );
        }
        unset($this->limit[$this->sessionID][$method][$level][$uniqueid]);
    }

    /**
     * Logs the external inocation turn around time in a TAT_INFO file in the
     * logs folder
     *
     * @param type $level The TAT tracking level
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    private function logThirdPartyInvocationTAT($level, $method, $uniqueid) {
        try {
            $this->log->infoLog(
                BenchMarkConfig::TAT_INFO, $uniqueid,
                $this->levelNames[$level] . " | $method | "
                . "THIRDPARTY INVOCATION RUN TIME: "
                . $this->elapsed_time($level, $method, $uniqueid)
                . " SECONDS | ELAPSED MEMORY: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                BenchMarkConfig::TAT_ERROR, $uniqueid,
                $this->levelNames[$level] . " | $method | EXCEPTION CAUGHT: "
                . $ex->getMessage()
            );
        }
        unset($this->limit[$this->sessionID][$method][$level][$uniqueid]);
    }

    /**
     * Logs the query turn around time in a TAT_INFO file in the logs folder
     *
     * @param type $level The TAT tracking level
     * @param string $method This is the method where invocation is taking place
     * @param string $uniqueid This is the unique identifier of the transaction
     * being processed
     */
    private function logDatabaseTAT($level, $method, $uniqueid) {
        try {
            $this->log->infoLog(
                BenchMarkConfig::TAT_INFO, $uniqueid,
                $this->levelNames[$level] . " | $method | QUERY RUN TIME: "
                . $this->elapsed_time($level, $method, $uniqueid)
                . " SECONDS | ELAPSED MEMORY: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                BenchMarkConfig::TAT_ERROR, $uniqueid,
                $this->levelNames[$level] . " | $method | EXCEPTION CAUGHT : "
                . $ex->getMessage()
            );
        }
        unset($this->limit[$this->sessionID][$method][$level][$uniqueid]);
    }

    /**
     * Determines the total memory used in the process.
     *
     * @return String the memory usage
     */
    private function memory_usage() {
        //return the amount of memory taken to complete the task
        return round(memory_get_usage() / 1024, 2) . 'KB';
    }

}
