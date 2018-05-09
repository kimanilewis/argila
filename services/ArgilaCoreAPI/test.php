<?php

//$apiUrl = "http://68.168.102.159/argilaCore/index.php/mpesa_request";
//$apiUrl = "http://68.168.102.159/argilaCore/index.php/pos";
//$apiUrl = "http://10.8.0.2/argilaCore/index.php/pos";
$apiUrl = "http://localhost/argilaCore/index.php/pos";

$password = "2018-04-11 20:55:22";

 $posRequest='eyJjcmVkZW50aWFscyI6eyJUb2tlbiI6ImJpZ3NxdWFyIiwibG9jYXRpb25faWQiOiIxMjNRV0UifSwicGF5bG9hZCI6eyJhY2NvdW50TnVtYmVyIjoiOTIwQjY1MTMiLCJiYXR0ZXJ5TGV2ZWwiOiIwMCIsInNvdXJjZSI6InBvcyJ9fQ==';
//$posRequest = '{"credentials":{"Token":"bigsquare998874","location_id":"1234ABCB"},'
//        . '"payload":{"accountNumber":"12345AAA","batteryLevel":"1030","source":"pos"}}';  //394687UJ
$userRequest = array(
    'source' => 'pos',
    'batteryLevel' => '10',
    'accountNumber' => '123WE',
);

$mpesaRequest = array(
    'xmlns:ns1' => 'http://cps.huawei.com/cpsinterface/c2bpayment',
    'TransAmount' => '10.00',
    'TransID' => 'LH36RF88BD',
    'TransType' => 'Pay Bill',
    'BillRefNumber' => '12345AAA',
    'BusinessShortCode' => 765035,
    'TransTime' => '20170803144147',
    'MSISDN' => '254718583299',
    'KYCInfo' => '[{ '
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

$response = post($apiUrl, $posRequest);

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
    curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
//    curl_setopt($ch, CURLOPT_POSTFIELDS, (json_encode($fields)));
    $result = curl_exec($ch);
    curl_close($ch);
    return $result;
}
