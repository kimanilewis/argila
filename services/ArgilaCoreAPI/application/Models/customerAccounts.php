<?php

namespace Argila\ArgilaCoreAPI\Models;

class customerAccounts extends BaseModel
{

    /**
     * @var string
     */
    protected $table = "customer_accounts";

    /**
     * @var string
     */
    protected $primaryKey = "customerProfileAccountID";

    /**
     * @return \Opis\Database\ORM\Relation\BelongsTo
     */
    public function customer_accounts() {
        //  return $this->belongsTo('customer_accounts', 'customerProfileID');
    }

    public $model = array(
        'customerProfileAccountID' => '',
        'customerProfileID' => '',
        'accountNumber' => '',
        'balanceCarriedForward' => '',
        'expiryDate' => ''
    );

}
