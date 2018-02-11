<?php

$apiUrl = "http://localhost/argilaCore/index.php/mpesa_request";
//$apiUrl = "http://192.168.254.242:9008/syncAPI_KE/index.php/api";

$username = "ecommerce";
$password = "!23qweASD";


$posRequest = '{"credentials":{"Token":"bigsquare998874","location_id":"ABCD1234"},"payload":{"accountNumber":"D1337B25","batteryLevel":"1030","source":"pos"}}';
$userRequest = array(
//    'username' => $username,
//    'password' => $password,
    'source' => 'pos',
    'batteryLevel' => '10',
    'accountNumber' => '123WE',
);

$mpesaRequest = array(
    'xmlns:ns1' => 'http://cps.huawei.com/cpsinterface/c2bpayment',
    'TransAmount' => '200.00',
    'TransID' => 'LH36RF88BD',
    'TransType' => 'Pay Bill',
    'BillRefNumber' => '12_ABC',
    'BusinessShortCode' => 765035,
    'TransTime' => '20170803144147',
    'MSISDN'=>'254718668308',
    'KYCInfo' =>'[{ '
    . '    "KYCValue": "John",  '
    . '    "KYCName": "[Personal Details][First Name]"    },    '
    . ' {      "KYCValue": "Doe",      '
    . '       "KYCName": "[Personal Details][Middle Name]" '
    . '  }, '
    . '   {      '
    . '       "KYCValue": "Doe",      '
    . '        "KYCName": "[Personal Details][Last Name]"   '
    . '    }  ]',
);

//$response = post($apiUrl,$paymentRequest);
//$response = post($apiUrl, $paymentRequestArray);
$response = post($apiUrl, $mpesaRequest);

print_r($response);

function post($url = "", $fields = array()) {

    global $username, $password;
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_NOSIGNAL, 1);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_USERPWD, $username . ":" . $password);
    curl_setopt($ch, CURLOPT_ENCODING, 'gzip,deflate');
    curl_setopt($ch, CURLOPT_POST, count($fields));
    curl_setopt($ch, CURLOPT_POSTFIELDS, (json_encode($fields)));
    $result = curl_exec($ch);
    curl_close($ch);
    return $result;
}
