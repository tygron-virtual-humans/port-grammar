lexer grammar GOALLexer;

@members {
	// counters for matching KR code fragments
	int bmatch=0, smatch=0;
}

// Modules
IMPORT			: '#import';
MODULEFILE		: '"' ~[ \t\f\r\n?%*:|"<>]+ '.mod2g"';
MODULE			: 'module';
INIT			: 'init';
MAIN			: 'main';
EVENT			: 'event';
FOCUS			: 'focus';
NONE			: 'none';
NEW				: 'new';
FILTER			: 'filter';
SELECT			: 'select';
EXITMODULE		: 'exit-module'; // built-in action but up here because of next token
EXIT			: 'exit';
ALWAYS			: 'always';
NEVER			: 'never';
NOGOALS			: 'nogoals';
NOACTION		: 'noaction';

// Mental state sections
KNOWLEDGE		: 'knowledge'	-> pushMode(KRBLOCK);
BELIEFS			: 'beliefs'		-> pushMode(KRBLOCK);
GOALS			: 'goals'		-> pushMode(KRBLOCK);

// Program section
PROGRAM			: 'program';
ORDER			: 'order';
LINEARALL		: 'linearall';
LINEAR			: 'linear';
RANDOMALL		: 'randomall';
RANDOM			: 'random';
ADAPTIVE		: 'adaptive';
DEFINE			: '#define';

// Rule tokens
IF				: 'if';
THEN			: 'then';
FORALL			: 'forall';
DO				: 'do';
LISTALL			: 'listall';
RTLARROW		: '<-';
LTRARROW		: '->';

// Mental state operators
BELIEF			: 'bel'			-> pushMode(KRSTATEMENT);
GOAL			: 'goal'		-> pushMode(KRSTATEMENT);
AGOAL			: 'a-goal'		-> pushMode(KRSTATEMENT);
GOALA			: 'goal-a'		-> pushMode(KRSTATEMENT);
NOT				: 'not';
TRUE			: 'true';

// Built-in actions
ADOPT			: 'adopt'		-> pushMode(KRSTATEMENT);
DROP			: 'drop'		-> pushMode(KRSTATEMENT);
INSERT			: 'insert'		-> pushMode(KRSTATEMENT);
DELETE			: 'delete'		-> pushMode(KRSTATEMENT);
LOG				: 'log'			-> pushMode(KRSTATEMENT);
PRINT			: 'print'		-> pushMode(KRSTATEMENT);
SENDONCE		: 'sendonce'	-> pushMode(KRSTATEMENT);
SEND			: 'send'		-> pushMode(KRSTATEMENT);

// Selector expressions
ALL				: 'all';
ALLOTHER		: 'allother';
SELF			: 'self';
SOME			: 'some';
SOMEOTHER		: 'someother';
THIS			: 'this';

// Action specification tokens
ACTIONSPEC		: 'actionspec';
EXTERNAL		: '@env';
INTERNAL		: '@int';
PRE				: 'pre'			-> pushMode(KRBLOCK);
POST			: 'post'		-> pushMode(KRBLOCK);

// IDs
ID				: (ALPHA | SCORE) (ALPHA | DIGIT | SCORE)* 
				 	{ int IDi=1; // 'hack' for KR parameters
				   		while(true){
				  	 		final char next = (char)_input.LA(IDi);
				  	 		if (!java.lang.Character.isWhitespace(next)) {
				  		 		if(next=='(') pushMode(KRSTATEMENT);
				  		 		break;
				  	 		}
				  	 		IDi++;
				 		}
				 	};

fragment ALPHA	: [a-zA-Z];
fragment SCORE	: '_';
fragment DIGITS	: DIGIT+;
fragment DIGIT	: [0-9];

//  Plus, minus, equals, and punctuation tokens.
EQUALS			: '=';
MINUS			: '-';
PLUS			: '+';
DOT				: '.';
COMMA			: ',';
LBR				: '(';
RBR				: ')';
CLBR			: '{';
CRBR			: '}';
SLBR			: '[';
SRBR			: ']';

// Comments and white space
LINE_COMMENT	: '%' ~[\r\n]* '\r'? '\n'	-> channel(HIDDEN);
BLOCK_COMMENT	: '/*' .*? '*/'				-> channel(HIDDEN);
WS				: [ \t\f\r\n]+				-> channel(HIDDEN);

// Knowledge representation code
mode KRBLOCK;
KR_CLBR			: WS? CLBR
				 	{ setType(CLBR);
				   	  bmatch++;
				   	  if (bmatch>1) more();
				 	};
KR_CRBR			: CRBR WS? 
				 	{ setType(CRBR);
				   	  bmatch--;
					  if (bmatch==0) popMode();
					  else more();
				 	};
KR_BLOCK		: .;

mode KRSTATEMENT;
KR_LBR			: WS? LBR 
					{ setType(LBR);
					  smatch++;
					  if (smatch>1) more();
					};
KR_RBR			: RBR WS?
					{ setType(RBR);
					  smatch--;
					  if (smatch==0) popMode();
					  else more();
					};
KR_STATEMENT	: .;