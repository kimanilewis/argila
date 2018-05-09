<?php

namespace Argila\ArgilaCoreAPI\Models;

class customerProfiles extends BaseModel
{

    /**
     * @var string
     */
    protected $table = "customerProfiles";

    /**
     * @var string
     */
    protected $primaryKey = "customerProfileID";

    /**
     * @return \Opis\Database\ORM\Relation\BelongsTo
     */
    public function customer_profiles() {
        //  return $this->belongsTo('customer_accounts', 'customerProfileID');
    }

    public $model = array(
        'customerProfileID' => '',
        'MSISDN' => '',
        'customerName' => '',
        'amount' => '',
        'amountCarriedForward' => ''
    );

}
