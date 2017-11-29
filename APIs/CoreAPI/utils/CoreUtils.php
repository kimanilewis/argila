<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of CoreUtils
 *
 * @author Lewis Kimani <lewis.kimani@cellulant.com>
 */
class CoreUtils
{

    /**
     * Log Function using log4php library .
     * @param int $logLevel
     * @param string $uniqueID 
     * @param string $stringtolog
     * @param string $fileName
     * @param string $function
     * @param int $lineNo
     * @param string $logger 
     * 
     * @example $stringtolog = CoreUtils::processLogArray(array("networkid"=>"1","message"=>"New safaricom USSD request","msisdn"=>$MSISDN,"accessPoint"=>$DATA));
      CoreUtils::flog4php(4,$stringtolog , __FILE__, __FUNCTION__, __LINE__, "safussdinterfaceinfo", $logproperties);
     */
    public static function flog4php($logLevel, $uniqueID = NULL, $arrayparams = null, $fileName
    = NULL, $function = NULL, $lineNo = NULL, $logger = NULL, $propertiespath) {

        $stringtolog = self::processLogArray($arrayparams);

        Logger::configure($propertiespath);
        $log4phplogger = Logger::getLogger($logger);
        //[date time | log level | file | function | unique ID(e.g MSISDN) | log text ]

        $loggedstring = "$fileName|$function|$uniqueID| $stringtolog";
        switch ($logLevel) {
            case 1: //critical
                $log4phplogger->fatal($loggedstring);
                break;
            case 2://fatal
                $log4phplogger->fatal($loggedstring);

                break;
            case 3://error
                $log4phplogger->error($loggedstring);

                break;
            case 4://info
                $log4phplogger->info($loggedstring);

                break;
            case 5://sequel
                $log4phplogger->debug($loggedstring);

                break;
            case 6://trace
                $log4phplogger->trace($loggedstring);

                break;
            case 7://debug
                $log4phplogger->debug($loggedstring);

                break;
            case 8://custom
                $log4phplogger->info($loggedstring);

                break;
            case 9://undefined
                // $log4phplogger->fatal($loggedstring);

                break;
            case 10: //warn
                $log4phplogger->warn($loggedstring);
                break;

            default; //undefined
        }
    }

    /**
     * Formulates common hub channels payload to log within a given request.
     * String to be returned will be concatenated for final log as show below
     * [date time | log level | file | function | unique ID | <<STRING RETURNED>> ]
     * 
     * @example method call - processLogArray(array("channelRequestID"=>32323,"networkid"=>1,"msisdn"=>254721159049..etc));
     * @param type $arrayparams
     */
    public static function processLogArray($arrayparams) {

        $logstringtoreturn = "";
        $paramCount = count($arrayparams);

        if (!empty($arrayparams)) {
            $counter = 0;
            foreach ($arrayparams as $key => $value) {
                $counter ++;

                $logstringtoreturn.= $key . ":" . $value . ($counter < $paramCount ? "," : "");
            }
        }

        return $logstringtoreturn;
    }

    public function select($table, $tableID, $id, $condition = '', $param = null) {
        $mysql = new MySQL(
            Config::HOST, Config::USER, Config::PASSWORD, Config::DATABASE,
            Config::PORT
        );
        $query = "SELECT * FROM  " . "" . $table . " " . " "
            . "WHERE " . $tableID . " = ? $condition ";
        if ($param != NULL) {
            $queryResult = DatabaseUtilities::executeQuery($mysql->mysqli,
                    $query, array('is', $id, $param));
        } else {
            $queryResult = DatabaseUtilities::executeQuery($mysql->mysqli,
                    $query, array('i', $id));
        }

        return $queryResult;
    }

   

}

?>