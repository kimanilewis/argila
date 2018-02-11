<?php

Namespace Argila\ArgilaCoreAPI\Config;

/**
 * * validation rules 
 * * loads all the validation rules
 * * @author kimanilewi@gmail.com
 * * (should be in a model ideally )
 * */
class Validation
{

    public $rules = array(
        'mpesa_request' => array(
            'required' => array(
                'BillRefNumber',
                'TransID',
                'TransAmount',
                'TransTime',
                'KYCInfo',
                'MSISDN'
            ),
            'string' => array(
                'BillRefNumber',
                'TransType',
                'KYCInfo',
            ),
            'double' => array(
                'TransAmount'
            )
        ),
        'pos' => array(
            'required' => array(
                'accountNumber',
                'location_id',
            ),
            'int' => array(
                'batteryLevel'
            ),
        )
    );

}
