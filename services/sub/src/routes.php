<?php

use Slim\Http\Request;
use Slim\Http\Response;

// Routes

const APP_VERSION = 'v1';
const APP_SERVICE = 'name: sub, version: ' . APP_VERSION;

$app->group('/api/'.APP_VERSION, function () {
    $this->get('/status', function (Request $request, Response $response, array $args) {
        return $response->withStatus(200)->withJson(['data' => 'OK']);
    });
    $this->post('/sub', function (Request $request, Response $response, array $args) {
        $body = $request->getParsedBody();
        if (is_null($body) || !is_array($body['operands']) || count($body['operands']) !== 2) {
            return $response->withStatus(400)->withJson([
                'error' => 'Invalid input',
                'service' => APP_SERVICE
            ]);
        }

        $ops = $body['operands'];
        $this->logger->debug('Received', $ops);

        $op1 = intval($ops[0]);
        $op2 = intval($ops[1]);

        return $response->withStatus(200)->withJson([
            'result' => $op1 - $op2,
            'operands' => [$op1, $op2],
            'service' => APP_SERVICE
        ]);
    });
});
