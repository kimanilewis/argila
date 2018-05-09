<?php
namespace Argila\ArgilaCoreAPI\Models;
use Argila\ArgilaCoreAPI\Config\Config;
use Opis\Database\Model;
use Opis\Database\Connection;

class BaseModel extends Model
{

    /**
     * @var
     * connection instance
     */
    protected static $connection;

    /**
     * @return mixed
     * lets make sure we have a DB connection
     */
    public static function getConnection()
    {
        if(static::$connection === null)
        {
            static::$connection =  new Connection('mysql:host='.Config::DbHost.';dbname='.Config::DbName,
                 Config::DbUser,
                 Config::DbPass);
        }
        return static::$connection;
    }


}