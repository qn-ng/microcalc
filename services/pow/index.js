const express = require('express');
const router = express.Router()
const app = express();
const morgan = require('morgan');
const debug = require('debug')('app');
const bodyParser = require('body-parser');
const axios = require('axios');

const APP_VERSION = 'v1';
const APP_PORT = process.env.APP_PORT || 3000;
const APP_BASEPATH = `/api/${APP_VERSION}`;
const APP_SERVICE = `name: pow, version: ${APP_VERSION}`;
const MULT_ENDPOINT = process.env.MULT_ENDPOINT || 'http://mult/api';
const AXIOS_DISABLE_PROXY = process.env.AXIOS_DISABLE_PROXY !== undefined;

const TRACING_HEADERS = [
    "x-request-id",
    "x-b3-traceid",
    "x-b3-spanid",
    "x-b3-parentspanid",
    "x-b3-sampled",
    "x-b3-flags",
    "x-ot-span-context"
];

router
    .get('/status', (_, res) => res.status(200).json({ data: 'OK' }))
    .post('/pow', async (req, res) => {
        if (!req.body
            || !req.body.operands
            || !Array.isArray(req.body.operands)
            || req.body.operands.length !== 2
            || req.body.operands[1] < 0) {
            return res.status(400).json({ error: 'Invalid input', service: APP_SERVICE });
        }

        const headers = TRACING_HEADERS
            .map(header => [header, req.header(header)])
            .filter(p => p[1] !== undefined);
        const axiosOpts = Object.assign(
            {},
            { headers: headers },
            AXIOS_DISABLE_PROXY ? { proxy: false } : {}
        );

        let [op1, op2] = req.body.operands;
        debug('Received', op1, op2);

        op1 = parseInt(op1);
        op2 = parseInt(op2);

        const ok = (result) => res.status(200).json({
            result: result,
            operands: [op1, op2],
            service: APP_SERVICE
        });
        const origins = [];

        try {
            const doMult = async (l, r) => {
                debug('doMult', l, r);
                const { data } = await axios.post(
                    MULT_ENDPOINT,
                    { operands: [l, r] },
                    axiosOpts);
                origins.push(data);
                return data.result;
            };

            const doPow = async (base, exp) => {
                debug('doPow', base, exp);

                if (exp === 0)
                    return 1;
                if (exp === 1)
                    return base;

                if (exp % 2 === 1) {
                    return await doMult(base, await doPow(base, exp - 1));
                }

                const sqr = await doMult(base, base);
                return await doPow(sqr, exp / 2);
            };

            return ok(await doPow(op1, op2));
        } catch (err) {
            debug('Error handled', err.response ? err.response : err);
            if (err.response && err.response.status === 400) {
                return res.status(400).json({ error: 'Invalid input', service: APP_SERVICE });
            }
            return res.status(500).json({ error: 'Something wrong', service: APP_SERVICE });
        }
    });

app.use(morgan('combined'))
app.use(bodyParser.json());
app.use(APP_BASEPATH, router);
app.use((_, res) => {
    res.status(404).end();
});

app.listen(APP_PORT, () => debug(`service launched at ${APP_PORT}`));