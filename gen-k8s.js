const yaml = require('js-yaml');

const IMG_PREFIX = 'gcr.io/orange-api-214308/microcalc';
const APP_HOSTNAME = 'calc.ficium.fr';

const APPS = [
    { name: 'add', port: 3000 },
    { name: 'sub', port: 8000 },
    { name: 'mult', port: 8080 },
    { name: 'div', port: 5000 },
    {
        name: 'neg', port: 80, env: {
            ASPNETCORE_MULT_ENDPOINT: 'http://mult:8080/api/v1/mult'
        }
    },
    {
        name: 'pow', port: 3000, env: {
            MULT_ENDPOINT: 'http://mult:8080/api/v1/mult'
        }
    },
    {
        name: 'mod', port: 8080, env: {
            MC_MULT_ENDPOINT: 'http://mult:8080/api/v1/mult',
            MC_DIV_ENDPOINT: 'http://div:5000/api/v1/div',
            MC_SUB_ENDPOINT: 'http://sub:8000/api/v1/sub'
        }
    },
    {
        name: 'parser', port: 8080, env: {
            ADD_HOST: 'add',
            ADD_PORT: 3000,
            ADD_URI: '/api/v1/add',

            SUB_HOST: 'sub',
            SUB_PORT: 8000,
            SUB_URI: '/api/v1/sub',

            DIV_HOST: 'div',
            DIV_PORT: 5000,
            DIV_URI: '/api/v1/div',

            MULT_HOST: 'mult',
            MULT_PORT: 8080,
            MULT_URI: '/api/v1/mult',

            NEG_HOST: 'neg',
            NEG_PORT: 80,
            NEG_URI: '/api/v1/neg',

            POW_HOST: 'pow',
            POW_PORT: 3000,
            POW_URI: '/api/v1/pow',

            MOD_HOST: 'mod',
            MOD_PORT: 8080,
            MOD_URI: '/api/v1/mod'
        }

    }
];

const output = [];

const svcTpl = yaml.safeLoad(`
apiVersion: v1
kind: Service
metadata:
  name: app_name
  labels:
    app: microcalc
    microcalc: app_name
spec:
  ports:
  - port: app_port
    name: main-port
  selector:
    microcalc: app_name
`);

const deployTpl = yaml.safeLoad(`
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: app_name
  labels:
    app: microcalc
    microcalc: app_name
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app: microcalc
        microcalc: app_name
    spec:
      containers:
      - image: img_prefix-app_name
        # imagePullPolicy: IfNotPresent
        name: app_name
        ports:
        - containerPort: app_port
`);

APPS
    .map(app => {
        const svc = Object.assign({}, svcTpl);
        const deploy = Object.assign({}, deployTpl);

        svc.metadata.name = app.name;
        svc.metadata.labels.microcalc = app.name;
        svc.spec.ports[0].port = app.port;
        svc.spec.selector.microcalc = app.name

        deploy.metadata.name = app.name;
        deploy.metadata.labels.microcalc = app.name;
        deploy.spec.template.metadata.labels.microcalc = app.name;
        deploy.spec.template.spec.containers[0].image = IMG_PREFIX + '-' + app.name;
        deploy.spec.template.spec.containers[0].name = app.name;
        deploy.spec.template.spec.containers[0].ports[0].containerPort = app.port;

        if (app.env) {
            deploy.spec.template.spec.containers[0].env =
                Object.entries(app.env).map(kv => ({ name: kv[0], value: kv[1].toString() }));
        }

        return [yaml.safeDump(svc), yaml.safeDump(deploy)];
    })
    .forEach(doc => console.log(doc[0] + '\n---\n' + doc[1] + '\n---\n'));

const gatewayTpl = yaml.safeLoad(`
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: microcalc-gateway
  labels:
    app: microcalc
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - app_hostname
`);
gatewayTpl.spec.servers.forEach(svr => svr.hosts[0] = APP_HOSTNAME);

console.log(yaml.safeDump(gatewayTpl) + '\n---\n');

const vsvcTpl = yaml.safeLoad(`
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: microcalc
  labels:
    app: microcalc
spec:
  hosts:
  - app_hostname
  gateways:
  - microcalc-gateway
  http:
  - match:
    - uri:
        prefix: /
    route:
    - destination:
        host: parser
        port:
          number: 8080
`);
vsvcTpl.spec.hosts[0] = APP_HOSTNAME;

console.log(yaml.safeDump(vsvcTpl) + '\n---\n');