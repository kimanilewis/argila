<?php

/**
 * Class Helpers
 * @package Argila\ArgilaCoreAPI\Utilities
 * contains helper functions
 * reusable system wide
 * PHP VERSION 5.6
 * @author    Lewis Kimani   <kimanilewi@gmail.com>
 * @copyright 2017 Cellulant Ltd
 * @license   Proprietory License
 * @link      http://www.cellulant.com
 */

namespace Argila\ArgilaCoreAPI\Utilities;

use Argila\ArgilaCoreAPI\Config\StatusCodes;
use Respect\Validation\Validator as v;
use Argila\ArgilaCoreAPI\Utilities\SyncLogger as logger;
use Argila\ArgilaCoreAPI\Config\Config;

class Helpers
{

    /**
     * * log class
     * */
    private $log;

    function __construct() {
        $this->log = new logger();
    }

    /**
     * @param array $data
     * @return stdClass
     */
    public static function arrayToObj($data = array()) {
        try {
            if (!is_array($data) || !(empty($data))) {
                throw new \Exception("Null variable provided,no data passed for conversion");
            }
            $package = new \stdClass();
            foreach ($data as $index => $val) {
                $package->$index = $val;
            }
            return $package;
        } catch (\Exception $e) {
            
        }
    }

    /**
     * 
     * @param type $payload
     * @param type $rules
     * @return boolean
     * @throws \Exception
     */
    public function validateParams($payload, $rules) {

        $ruleCount = count($rules);
        if ($ruleCount == 0) {
            return TRUE;
        }
        $requiredFields = $rules['required'];

        $this->log->info(Config::info, -1,
            " reveives payload  "
            . $this->log->printArray($rules['required']));
        foreach ($payload as $key => $value) {
            $index = array_search($key, $requiredFields);
            unset($requiredFields[$index]); //remove the checked element

            if (isset($rules['required']) && in_array($key, $rules['required'])) {
                if (!v::notEmpty()->validate($value)) {
                    throw new \Exception(
                    'Required parameter ['
                    . $key . '] is empty.'
                    , StatusCodes::INVALID_DATA_TYPE
                    );
                }
            }

            if (isset($rules['int']) && in_array($key, $rules['int'])) {
                $indexI = array_search($key, $rules['int']);

                unset($rules['int'][$indexI]);
                $value = (int) $value;
                if (!v::int()->validate($value)) {
                    throw new \Exception(
                    'Wrong data type, ['
                    . $rules['int'][$key] . '] must be an integer.'
                    , StatusCodes::INVALID_DATA_TYPE
                    );
                }
            }
            if (isset($rules['float']) && in_array($key, $rules['float'])) {
                $value = (float) $value;
                if (!v::float()->validate($value)) {
                    throw new \Exception(
                    'Wrong data type, ['
                    . $key . '] must be a decimal.'
                    , StatusCodes::INVALID_DATA_TYPE
                    );
                }
            }
            if (isset($rules['date']) && in_array($key, $rules['date'])) {
                if (!v::date()->validate($value)) {
                    throw new \Exception(
                    'Wrong data type, ['
                    . $key . '] must be a valid date.'
                    . ' Format: [ yyyy-mm-dd ]'
                    , StatusCodes::INVALID_DATA_TYPE
                    );
                }
            }
            $indexS = array_search($key, $rules['string']);

            unset($rules['string'][$indexS]);
            if (isset($rules['string']) && in_array($key, $rules['string'])) {
                if (!v::string()->validate($value)) {
                    throw new \Exception(
                    'Wrong data type, ['
                    . $value . '] must be a string.'
                    , StatusCodes::INVALID_DATA_TYPE
                    );
                }
            }
        }

        $items = count($requiredFields);
        if ($items > 0) {
            $list = '';
            foreach ($requiredFields as $value) {
                $list .= $value . ',';
            }
            $list = trim($list, " \t\n\r\0\x0B");
            $list = rtrim($list, ',');
            throw new \Exception('Required parameters [' . $list . '] not provided.',
            301);
        }
        return TRUE;
    }

}
