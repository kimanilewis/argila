<?php

//Responses

class Responses {
    // CRUD Unknown merchants responses
    const SUCCESS_UNKNOWNMERCHANT_CREATED = 'Successfully created an unknown Merchant.';
    const SUCCESS_UNKNOWNMERCHANT_UPDATED = 'Successfully updated an unknown Merchant.';
    const SUCCESS_UNKNOWNMERCHANT_DELETED = 'Successfully deleted an unknown Merchant.';
    const SUCCESS_UNKNOWNMERCHANT_FETCHED = 'Successfully fetched an unknown Merchant.';
    
    //Failure Responses
    const MISSING_PHONE_NUMBER_M = 'Missing phone number(MSISDN)';
    const MISSING_PROFILE_M = 'The MSISDN provided does not have a matching profile';
    const MISSING_UNKNOWNMERCHANTID_M = 'Missing an identifier to an unknown merchant';
    const MISSING_CLIENTCATEGORYTYPEID_M = 'Missing a category type of the unknown merchant';
    const INVALID_PHONE_NUMBER_M = 'Invalid phone number';
    
 
    
}

?>