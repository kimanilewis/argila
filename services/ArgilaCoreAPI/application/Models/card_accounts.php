<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Argila\ArgilaCoreAPI\Models;

class card_accounts extends BaseModel
{

    protected $table = "card_accounts";
    protected $primaryKey = "card_id";

    /**
     * @return \Opis\Database\ORM\Relation\BelongsTo
     */
    public function cardAccounts() {
        return $this->belongsTo('locations', 'locationID');
    }

    public $model = array(
        'card_id' => '',
        'accountNumber' => '',
        'active' => '',
    );

}
