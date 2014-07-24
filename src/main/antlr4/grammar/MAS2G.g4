grammar MAS2G;

mas
	: environment?
	  agentFiles
	  launchPolicy
      EOF
	;

// Environment section

environment
	: 'environment' '{'
	  'env' '=' DOUBLESTRING '.'
	  ('init' '=' '[' initKeyValue (',' initKeyValue)* ']' '.')?
	  '}'
	;

initKeyValue
	: ID '=' initExpr
	;

initExpr
	: constant
	| function
	| list
	;

constant
	: ID 
	| INT
	| FLOAT 
	| SINGLESTRING 
	| DOUBLESTRING
	;

function
	: ID '(' initExpr (',' initExpr)* ')'
	;

list
	: '[' initExpr (',' initExpr)* ']'
	;

// Agent Files section

agentFiles
	: 'agentfiles' '{' agentFile* '}' // empty agent files section allowed because of IDE template
	;

agentFile
	: DOUBLESTRING ('[' agentFilePar (',' agentFilePar)* ']')? '.'
	;

agentFilePar
	: ('name' | 'language') '=' ID
	;

// Launch Policy section

launchPolicy
	: 'launchpolicy' '{' launchRule* '}' // empty launch policy section allowed because of IDE template
	;

launchRule
	: basicRule
	| conditionalRule
	;

basicRule
	: 'launch' launchRuleComponent (',' launchRuleComponent)* '.'
	;

launchRuleComponent
	: ((('*' | ID) AGENTFILENAME) | ID) ('[' INT ']')?
	;
         
conditionalRule
	: 'when' entityDescription '@env' 'do' basicRule
	;

entityDescription
	: 'entity'
	| '[' entityConstraint (',' entityConstraint)* ']'
	;

entityConstraint
	: (('name' | 'type') '=' ID)
	| ('max' '=' INT)
	;

// LEXER

AGENTFILENAME
	:  ':'[ \t]*~[ \t\f\r\n?%*:|"<>.,]+
	;

ID
	: (ALPHA | SCORE) (ALPHA | DIGIT | SCORE)*
	;

fragment ALPHA: [a-zA-Z];
fragment SCORE: '_';

FLOAT
	: ('+' | '-')? (DIGITS '.' DIGITS*)
	| ('.' DIGITS)
	;
	
INT
	: ('+' | '-')? DIGITS
	;

fragment DIGITS:DIGIT+;
fragment DIGIT: [0-9];

SINGLESTRING
	: ('\'' ('\\\'' | .)*? '\'')
	;

DOUBLESTRING
	: ('"' ('\\"' | .)*? '"')
	;

// White space and comments.
LINE_COMMENT
	: '%' ~[\r\n]* '\r'? '\n' -> channel(HIDDEN)
	;

BLOCK_COMMENT
	: '/*' .*? '*/' -> channel(HIDDEN)
	;

WS
	: [ \t\f\r\n]+ -> channel(HIDDEN)
	;