<?php

/**
 * Its a class for benchmarking the execution time for any given function or statement
 * its initialised by creating an object variable of it.
 * @author Oscar Oluoch <oscar.oluoch@cellulant.com>
 */
class BenchMark
{

    public $limit;
    public $log;

    function __construct() {
        $this->limit = array();
        $this->log = new BeepLogger();
    }

    /**
     * The start function used to start the timing process
     */
    public function start($method, $uniqueid = -1) {
        $this->limit[$method][$uniqueid]['start'] = microtime(TRUE);
    }

    /**
     * It marks the end of the benchmark
     */
    public function end($method, $uniqueid) {
        $this->limit[$method][$uniqueid]['end'] = microtime(TRUE);
    }

    /**
     * Determines the time taken
     * @return String
     * @throws Exception if start time is not defined
     */
    public function elapsed_time($method, $uniqueid) {
        if (!isset($this->limit[$method][$uniqueid]['start']) || empty($this->limit[$method][$uniqueid]['start'])) {
            throw new Exception("The start time is not set correctly");
        } else {
            $this->end($method, $uniqueid);
            return sprintf("%.4f",
                $this->limit [$method][$uniqueid]['end'] - $this->limit[$method][$uniqueid]['start']);
        }
    }

    /**
     * Logs the turn around time in a TAT_INFO file in the logs folder
     * @param String $message
     * @param integer $uniqueid by default -1
     */
    public function logFunctionTAT($method, $uniqueid = -1) {
        try {
            $this->log->infoLog(
                Config::TAT_INFO, $uniqueid,
                $method . " - FUNCTION RUN TIME: " . $this->elapsed_time($method,
                    $uniqueid)
                . " seconds | elapsed memory: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                Config::ERROR, $uniqueid,
                $method . " - START time ERROR : " . $ex->getMessage()
            );
        }
        unset($this->limit[$method][$uniqueid]);
    }

    /**
     * Logs the turn around time in a TAT_INFO file in the logs folder
     * @param String $message
     * @param integer $uniqueid by default -1
     */
    public function logTotalTAT($method, $uniqueid = -1) {
        try {
            $this->log->infoLog(
                Config::TAT_INFO, $uniqueid,
                $method . " - TOTAL RUN TIME: " . $this->elapsed_time($method,
                    $uniqueid)
                . " seconds | elapsed memory: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                Config::ERROR, $uniqueid,
                $method . " - Start time ERROR : " . $ex->getMessage()
            );
        }
        unset($this->limit[$method][$uniqueid]);
    }

    /**
     * Logs the turn around time in a TAT_INFO file in the logs folder
     * @param String $message
     * @param integer $uniqueid by default -1
     */
    public function logInvocationTAT($method, $uniqueid = -1) {
        try {
            $this->log->infoLog(
                Config::TAT_INFO, $uniqueid,
                $method . " - TOTAL INVOCATION TIME: " . $this->elapsed_time($method,
                    $uniqueid)
                . " seconds | elapsed memory: " . $this->memory_usage()
            );
        } catch (Exception $ex) {
            $this->log->errorLog(
                Config::ERROR, $uniqueid,
                $method . " - Start time ERROR : " . $ex->getMessage()
            );
        }
        unset($this->limit[$method][$uniqueid]);
    }

    /**
     * Determines the total memory used in the process.
     * @return String
     */
    public function memory_usage() {
        return round(memory_get_usage() / 1024, 2) . 'KB'; //return the amount of memory taken to complete the task
    }

}
