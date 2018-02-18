<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Argila\ArgilaCoreAPI\Models;

class sms_requests extends BaseModel
{

    protected $table = "sms_requests";
    protected $primaryKey = "sms_id";

    /**
     * @return \Opis\Database\ORM\Relation\BelongsTo
     */
    public function cardAccounts() {
        return $this->belongsTo('sms_templates', 'sms_template_id');
    }

    public $model = array(
        'smsID' => '',
        'templateID' => '',
        'active' => '',
    );

}
