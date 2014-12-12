/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 * 
 * The lexer grammar for the agent programming language GOAL lists all tokens used in the parser grammar
 * for the programming language. It is also used by the parser grammar for tests (test2g files).
 */
lexer grammar GOALLexer;


@members {
	boolean stayInDefault = false;
}

tokens{ HIDDEN }

// Modules
IMPORT			: '#import';
MODULE			: 'module';
MODULEFILE		: '"' ~[ \t\f\r\n?%*:|"<>]+ '.mod2g"';
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
LISTALL			: 'listall'		-> pushMode(VAR_PARAMETERS);
LTRARROW		: '->';
RTLARROW		: '<-';

// Mental state operators
BELIEF_OP		: 'bel'			{ stayInDefault = false; };
GOAL_OP			: 'goal'		{ stayInDefault = false; }; // GOAL_OP because we cannot have same token as parser grammar name.
AGOAL_OP		: 'a-goal'		{ stayInDefault = false; };
GOALA_OP		: 'goal-a'		{ stayInDefault = false; };
NOT				: 'not'			{ stayInDefault = true; };
TRUE			: 'true';

// Built-in actions
ADOPT			: 'adopt'		{ stayInDefault = false; };
DROP			: 'drop'		{ stayInDefault = false; };
INSERT			: 'insert'		{ stayInDefault = false; };
DELETE			: 'delete'		{ stayInDefault = false; };
LOG				: 'log'			{ stayInDefault = false; };
PRINT			: 'print'		{ stayInDefault = false; };
SENDONCE		: 'sendonce'	{ stayInDefault = false; };
SEND			: 'send'		{ stayInDefault = false; };

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

// Test tokens
UNITTEST		: 'masTest';
MASFILE			: '"' ~[ \t\f\r\n?%*:|"<>]+ '.mas2g"';
MAS				: 'mas';
TIMEOUT			: 'timeout';
IN				: 'in';
EVALUATE		: 'evaluate';
ASSERT			: 'assert';
ATSTART			: 'atstart';
// ALWAYS:			'always';
EVENTUALLY		: 'eventually';
ATEND			: 'atend';
UNTIL		    : 'until';
WHILE			: 'while';

StringLiteral
	: '"' ('\\"' | ~[\r\n"])* '"'
	;
UnterminatedStringLiteral
	: '"' ('\\"' | ~[\r\n"])*
	;
	
SingleQuotedStringLiteral
	: '\'' ('\\"' | ~[\r\n\'])* '\''
	;
UnterminatedSingelQuotedStringLiteral
	: '\'' ('\\"' | ~[\r\n\'])*
	;
	
//  Plus, minus, equals, and punctuation tokens.
EQUALS	: '=';
MINUS	: '-';
PLUS	: '+';
DOT		: '.';
COMMA	: ',';
LBR		: '(';
RBR		: ')';
CLBR	: '{';
CRBR	: '}';
SLBR	: '[';
SRBR	: ']';
COLON	: ':';

// GOAL identifiers
ID		: ALPHA (ALPHA | DIGIT | SCORE)*
		;

fragment ALPHA	: [a-zA-Z];
fragment SCORE	: '_';
fragment DIGIT	: [0-9];

// Floating point number (used in test2g file).
FLOAT	: (PLUS | MINUS)? (DIGIT+ (DOT DIGIT+)?) | (DOT DIGIT+)
		;

// Parameter list of KR terms (anything between brackets, except for bracket following 'not' operator)
PARLIST
  :  { stayInDefault == false }? '(' (  ~('(' | ')') | PARLIST )* ')'
  ;

// Comments
LINE_COMMENT	: '%' ~[\r\n]* '\r'? '\n'	-> channel(HIDDEN);
BLOCK_COMMENT	: '/*' .*? '*/'				-> channel(HIDDEN);
// White space
WS				: [ \t\f\r\n]+				-> channel(HIDDEN);

// Knowledge representation code
mode KRBLOCK;
KR_BLOCK
  : '{' (  ~('{' | '}') | KR_BLOCK )* '}' -> popMode
  ;
KR_BLOCK_WS
  : WS -> type(WS), channel(HIDDEN)
  ;
  
// Lexer mode to recognize variable parameters, to allow for all kinds of styles of variable names;
// E.g., PDDL uses '?' to indicate start of variable.
mode VAR_PARAMETERS;
VAR		: (ALPHA | DIGIT | PUNCTUATION)+
		;
VAR_PARAMETERS_RTLARROW	: RTLARROW -> type(RTLARROW), popMode;
VAR_PARAMETERS_RBR		: RBR -> type(RBR), popMode;
VAR_PARAMETERS_COMMA	: COMMA -> type(COMMA);
VAR_PARAMETERS_WS 		: WS -> type(WS), channel(HIDDEN);
// Something is wrong if we find anything else, but let's make sure we get out of this mode then again to avoid
// getting errors that say the rest of the input cannot be tokenized.
ERROR					: . -> popMode;

fragment PUNCTUATION: [=-+.:?!''~*$%#@^&_\\/];