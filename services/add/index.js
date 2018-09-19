const express = require('express');
const router = express.Router()
const app = express();
const morgan = require('morgan');
const debug = require('debug')('app');
const bodyParser = require('body-parser');

const APP_VERSION = 'v1';
const APP_PORT = process.env.APP_PORT || 3000;
const APP_BASEPATH = `/api/${APP_VERSION}`;
const APP_SERVICE = `name: add, version: ${APP_VERSION}`;

router
    .get('/status', (_, res) => res.status(200).json({ data: 'OK' }))
    .post('/add', (req, res) => {
        if (!req.body
            || !req.body.operands
            || !Array.isArray(req.body.operands)
            || req.body.operands.length !== 2) {
            return res.status(400).json({ error: 'Invalid input', service: APP_SERVICE });
        }

        let [op1, op2] = req.body.operands;
        debug('Received', op1, op2);

        op1 = parseInt(op1);
        op2 = parseInt(op2);

        return res.status(200).json({
            result: op1 + op2,
            operands: [op1, op2],
            service: APP_SERVICE
        });
    });

app.use(morgan('combined'))
app.use(bodyParser.json());
app.use(APP_BASEPATH, router);
app.use((_, res) => {
    res.status(404).end();
});

app.listen(APP_PORT, () => debug(`service launched at ${APP_PORT}`));