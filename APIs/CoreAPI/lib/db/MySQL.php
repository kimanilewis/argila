<?php

/**
 * MySQL connection wrapper that uses the RAII idiom to manage connections.
 */
class MySQL
{

    /**
     * The MySQLi object.
     *
     * @var object the MySQLi object
     */
    private $mysqli;

    /**
     * Creates a MySQL database connection object.
     *
     * @param string $host the database host
     * @param string $user the database user name
     * @param string $pass the database password
     * @param string $db the database name
     * @param string $port the database port
     */
    public function __construct($host, $user, $pass, $db, $port) {
        $this->mysqli = new mysqli($host, $user, $pass, $db, $port);
    }

    /**
     * Gets the MySQL connection object.
     *
     * @param string $name the name of the MySQL object.
     *
     * @return object the MySQL object
     */
    public function __get($name) {
        return $this->$name;
    }

    /**
     * Closes the MySQL connection.
     */
    public function __destruct() {
        $this->mysqli->close();
    }

}
