import ast, os

def envOrDefault(env_name, host, port, endpoint):
    return {
        **ast.literal_eval(os.getenv(env_name, "{'host': '" + host + "', 'port': '" + port + "'}")),
        'uri': PATH_PREFIX + endpoint
    }

PATH_PREFIX = os.getenv('MC_PATH_PREFIX', '/api/v1')
PARSER = envOrDefault('MC_PARSER', 'parser', '80', '/calculate')
ADD = envOrDefault('MC_ADD', 'add', '80', '/add')
SUB = envOrDefault('MC_SUB', 'sub', '80', '/sub')
DIV = envOrDefault('MC_DIV', 'div', '80', '/div')
MULT = envOrDefault('MC_MULT', 'mult', '80', '/mult')
NEG = envOrDefault('MC_NEG', 'neg', '80', '/neg')
POW = envOrDefault('MC_POW', 'pow', '80', '/pow')
MOD = envOrDefault('MC_MOD', 'mod', '80', '/mod')
SERVICES = [ADD, SUB, DIV, MULT, NEG, POW, MOD, PARSER]

for svc in SERVICES:
    print(svc)