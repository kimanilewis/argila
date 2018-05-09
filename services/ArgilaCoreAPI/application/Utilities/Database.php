<?php
Namespace Argila\ArgilaCoreAPI\Utilities;
use Argila\ArgilaCoreAPI\Config\Config;
Use Opis\Database\Database as ODatabase;
Use Opis\Database\Connection;
use PDO;

/**
 ** instantiate database connection uses opis library
 ** (http://www.opis.io/database)
 ** @author lewis Kimani <kimanilewi@gmail.com>
 **/
class Database{
     
     public function DBConn()
     {
          $conn = Connection::create('mysql:host=' . Config::DbHost . ';dbname='.Config::DbName, Config::DbUser, Config::DbPass)
              ->option(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_OBJ)
              ->option(PDO::ATTR_STRINGIFY_FETCHES, false)
              ->persistent()
              ->logQueries();
        $dbConn = new ODatabase($conn);
        return $dbConn;
     }

}


