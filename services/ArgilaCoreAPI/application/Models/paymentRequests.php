<?php

namespace Argila\ArgilaCoreAPI\Models;

class paymentRequests extends BaseModel
{

    /**
     * @var string
     */
    protected $table = "payment_requests";

    /**
     * @var string
     */
    protected $primaryKey = "paymentRequestID";

    public $model = array(
        'paymentRequestID' => '',
         'MSISDN' => '',
        'payerTransactionID' => '',
        'amount' => '',
        'payerClient' => '',
        'paymentDateReceived' =>''
    );

}
