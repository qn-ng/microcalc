from flask import Flask, request
from flask_restful import Resource, Api, abort

APP_VERSION = 'v1'
APP_BASEPATH = '/api/' + APP_VERSION
APP_SERVICE = 'name: div, version: ' + APP_VERSION

app = Flask(__name__)
api = Api(app)

class GetStatus(Resource):
    def get(self):
        return {'data': 'OK'}

class PostDiv(Resource):
    def post(self):
        body = request.get_json()
        ops = body['operands'] if body is not None else []

        if (len(ops) != 2 or ops[1] == 0):
            return abort(400, error='Invalid Input', service=APP_SERVICE)

        op1,op2 = ops
        print('Received {} {}'.format(op1, op2))

        op1 = int(op1)
        op2 = int(op2)

        return {
            'result': int(op1/op2),
            'operands': [op1,op2],
            'service': APP_SERVICE
        }

api.add_resource(GetStatus, APP_BASEPATH + '/status')
api.add_resource(PostDiv, APP_BASEPATH + '/div')

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')