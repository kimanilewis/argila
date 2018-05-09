<?php
namespace Argila\ArgilaCoreAPI\Utilities;
use InvalidArgumentException;

class Encryption
{
    /**
     * Initialisation vector string.
     *
     * @var string
     */
    private $iv;
    /**
     * Secret key string.
     *
     * @var string
     */
    private $key;

    /**
     * Constructor.
     *
     * @param string $initialisationVector the initialisation vector
     * @param string $secretKey the secret key
     */
    public function __construct($initialisationVector, $secretKey)
    {
        if ($initialisationVector === null) {
            throw new InvalidArgumentException(
                "Initialisation vector must not be null"
            );
        } elseif (strlen($initialisationVector) != 16) {
            throw new InvalidArgumentException(
                "Initialisation vector must be 16 characters in length"
            );
        } else {
            $this->iv = $initialisationVector;
        }

        if ($secretKey === null) {
            throw new InvalidArgumentException("Secret key must not be null");
        } elseif (strlen($secretKey) != 16) {
            throw new InvalidArgumentException(
                "Secret key must be 16 characters in length"
            );
        } else {
            $this->key = $secretKey;
        }
    }

    /**
     * Encrypts the given text.
     *
     * @param string $str the text to encrypt
     *
     * @return string the encrypted string
     */
    public function encrypt($str)
    {
        if ($str === null || strlen($str) == 0) {
            throw new InvalidArgumentException("Empty string");
        }

        $str = $this->pkcs5Pad($str);

        $iv = $this->iv;

        $td = mcrypt_module_open("rijndael-128", " ", "cbc", $iv);

        mcrypt_generic_init($td, $this->key, $iv);
        $encrypted = mcrypt_generic($td, utf8_decode($str));

        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);

        return bin2hex($encrypted);
    }

    /**
     * Decrypts the given text.
     *
     * @param string $code the text to encrypt
     *
     * @return string the decrypted string
     */
    public function decrypt($code)
    {
        if ($code === null || strlen($code) == 0) {
            throw new InvalidArgumentException("Empty string");
        }

        $code = $this->hex2bin($code);
        $iv = $this->iv;

        $td = mcrypt_module_open("rijndael-128", " ", "cbc", $iv);

        mcrypt_generic_init($td, $this->key, $iv);
        $decrypted = mdecrypt_generic($td, $code);

        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);

        $str = $this->pkcs5Unpad(utf8_encode(trim($decrypted)));

        return $str;
    }

    /**
     * Converts a hex string to a binary data string.
     *
     * @param string $hexdata the hex string
     *
     * @return the binary data string
     */
    protected function hex2bin($hexdata)
    {
        if ($hexdata === null || strlen($hexdata) == 0) {
            throw new InvalidArgumentException("Empty string");
        }

        $bindata = '';

        for ($i = 0; $i < strlen($hexdata); $i += 2) {
            $bindata .= chr(hexdec(substr($hexdata, $i, 2)));
        }

        return $bindata;
    }

    /**
     * Pads the given text to PKCS5 padding.
     *
     * @param string $text the string to pad
     *
     * @return string the padded string
     */
    private function pkcs5Pad($text)
    {
        $blocksize = 16;
        $pad = $blocksize - (strlen($text) % $blocksize);
        return $text . str_repeat(chr($pad), $pad);
    }

    /**
     * Unpads the text from PKCS5 padding.
     *
     * @param string $text the string to unpad
     *
     * @return mixed the unpadded string or false on error
     */
    private function pkcs5Unpad($text)
    {
        $pad = ord($text{strlen($text) - 1});

        if ($pad > strlen($text)) {
            return $text;
        }

        if (strspn($text, chr($pad), strlen($text) - $pad) != $pad) {
            return $text;
        }

        return substr($text, 0, -1 * $pad);
    }
}