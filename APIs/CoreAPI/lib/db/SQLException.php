<?php

/**
 * MySQL SQLException.
 *
 * @copyright Brian Ngure <brian@pixie.co.ke>
 * @author Brian Ngure <brian@pixie.co.ke>
 * @package MySQL_Database_Utilities
 * @license GNU Lesser General Public License
 * @since Version 1.0
 */
class SQLException extends Exception
{

    /**
     * Constructor.
     *
     * @param string $message the error message
     * @param int $code the error code
     * @param Exception $previous the previous exception
     */
    public function __construct($message, $code = 0, Exception $previous = null) {
        // Make sure everything is assigned properly
        parent::__construct($message, $code);
    }

}
