<?php

namespace Argila\ArgilaCoreAPI\Models;

class Mpesa extends BaseModel
{

    protected $table = "s_services";
    protected $primaryKey = "serviceID";

    /**
     * @return \Opis\Database\ORM\Relation\BelongsTo
     */
    public function client() {
        return $this->belongsTo('Client', 'clientID');
    }

    public $model = array(
        'serviceName' => '',
        'active' => '',
    );

}
