*** Settings *** 				
Library 	Collections 			
Library 	RequestsLibrary 			

*** Variables ***
${PATH_PREFIX}= 	/api/v1
&{PARSER}= 	host=parser 	port=8080 	uri=${PATH_PREFIX}/calculate
&{ADD}= 	host=add 	port=3000 	uri=${PATH_PREFIX}/add
&{SUB}= 	host=sub 	port=8000 	uri=${PATH_PREFIX}/sub
&{DIV}= 	host=div 	port=5000 	uri=${PATH_PREFIX}/div
&{MULT}= 	host=mult 	port=8080 	uri=${PATH_PREFIX}/mult
&{NEG}= 	host=neg 	port=80 	uri=${PATH_PREFIX}/neg
&{POW}= 	host=pow 	port=3000 	uri=${PATH_PREFIX}/pow
&{MOD}= 	host=mod 	port=8080 	uri=${PATH_PREFIX}/mod
@{SERVICES}= 	&{ADD} 	&{SUB} 	&{DIV} 	&{MULT} 	&{NEG} 	&{POW} 	&{MOD}

*** Test Cases *** 				
Healthcheck all services
	:FOR 	${svc} 	IN 	@{SERVICES}
	\	Run Keyword And Continue On Failure 	Check Status 	http://${svc['host']}:${svc['port']}

Test add service
	${operands}= 	Create List 	${1} 	${2}
	Post Ops 	${ADD} 	${operands} 	${3}

Test sub service
	${operands}= 	Create List 	${3} 	${2}
	Post Ops 	${SUB} 	${operands} 	${1}

Test mult service
	${operands}= 	Create List 	${3} 	${2}
	Post Ops 	${MULT} 	${operands} 	${6}

Test div service
	${operands}= 	Create List 	${6} 	${2}
	Post Ops 	${DIV} 	${operands} 	${3}
	${operands}= 	Create List 	${6} 	${0}
	Post Invalid  ${DIV} 	${operands}

Test neg service
	${operands}= 	Create List 	${1}
	Post Ops 	${NEG} 	${operands} 	${-1}

Test pow service
	${operands}= 	Create List 	${2} 	${0}
	Post Ops 	${POW} 	${operands} 	${1}
	${operands}= 	Create List 	${2} 	${3}
	Post Ops 	${POW} 	${operands} 	${8}
	${operands}= 	Create List 	${2} 	${-1}
	Post Invalid 	${POW} 	${operands}

Test mod service
	${operands}= 	Create List 	${8} 	${2}
	Post Ops 	${MOD} 	${operands} 	${0}
	${operands}= 	Create List 	${8} 	${3}
	Post Ops 	${MOD} 	${operands} 	${2}
	${operands}= 	Create List 	${8} 	${0}
	Post Invalid 	${MOD} 	${operands}

Test parser service
	Post Expr 	${PARSER} 	1 	${1}
	Post Expr 	${PARSER} 	--4*(3-2+1)^5%6/2 	${1}

*** Keywords ***
Check Status
	[Arguments] 	${url}
	Create Session 	svc 	${url}
	${resp}= 	Get Request 	svc 	${PATH_PREFIX}/status
	Should Be Equal As Strings	${resp.status_code}	200 	
	Dictionary Should Contain Item 	${resp.json()} 	data	OK

Post Expr
	[Arguments] 	${svc} 	${expression} 	${expected}
	${data}= 	Create Dictionary 	input=${expression}
	Post 	${svc} 	${data} 	${expected}

Post Ops
	[Arguments] 	${svc} 	${operands} 	${expected}
	${data}= 	Create Dictionary 	operands=${operands}
	Post 	${svc} 	${data} 	${expected}

Post
	[Arguments] 	${svc} 	${data} 	${expected}
	Create Session 	svc 	http://${svc['host']}:${svc['port']}
	${headers}= 	Create Dictionary 	Content-Type=application/json
	${resp}= 	Post Request 	svc 	${svc['uri']} 	data=${data} 	headers=${headers}
	Should Be Equal As Strings	${resp.status_code}	200
	Dictionary Should Contain Item 	${resp.json()} 	result	${expected}

Post Invalid
	[Arguments] 	${svc} 	${operands}
	Create Session 	svc 	http://${svc['host']}:${svc['port']}
	${data}= 	Create Dictionary 	operands=${operands}
	${headers}= 	Create Dictionary 	Content-Type=application/json
	${resp}= 	Post Request 	svc 	${svc['uri']} 	data=${data} 	headers=${headers}
	Should Be Equal As Strings	${resp.status_code}	400