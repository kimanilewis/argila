<?php

Namespace Argila\ArgilaCoreAPI;

use Klein\Klein as Router;
use Argila\ArgilaCoreAPI\Controllers\RoutesController as route;

$base = dirname($_SERVER['PHP_SELF']);
if (ltrim($base, '/')) {
    $_SERVER['REQUEST_URI'] = substr($_SERVER['REQUEST_URI'], strlen($base));
}
require_once __DIR__ . '/vendor/autoload.php';
$rqProcessor = new Router();

$rqProcessor->respond(array('POST', 'PUT'), '/pos',
    function($request, $response, $service) {

    /////All the headers that are Actually
    $request->headers;

    $API = new route();
    $API->run($request, $response);
});
//Mpesa end point
$rqProcessor->respond(array('POST', 'PUT'), '/mpesa_request',
    function($request, $response, $service) {

    /////All the headers that are Actually
    $request->headers;
    $API = new route();
    $API->run($request, $response);
});
//Mpesa end point
$rqProcessor->respond(array('POST', 'PUT'), '/mpesa_validate',
    function($request, $response, $service) {

    /////All the headers that are Actually
    $request->headers;
    $API = new route();
    $API->run($request, $response);
});
$rqProcessor->dispatch();

