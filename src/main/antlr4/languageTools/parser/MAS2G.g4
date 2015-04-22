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
 * The grammar for MAS files for the GOAL agent programming language.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found at the
 * end of the grammar.
 */
grammar MAS2G;

tokens{ HIDDEN }


mas
	: (environment | agentFiles | launchPolicy)*
      EOF
	;

// Environment section

environment
	: 'environment' '{'
	  'env' '=' string '.'
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
	| string
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
	: string ('[' agentFilePar (',' agentFilePar)* ']')? '.'
	;

agentFilePar
	: key = ('name' | 'language') '=' ID
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
	: (((STAR | ID) ':' ID) | ID) multiplier?
	;
	
multiplier
	: '[' INT ']'
	;
         
conditionalRule
	: 'when' entityDescription '@env' 'do' basicRule
	;

entityDescription
	: 'entity'
	| '[' entityConstraint (',' entityConstraint)* ']'
	;

entityConstraint
	: ( key = ('name' | 'type') '=' ID)
	| ( (key = 'max') '=' INT)
	;
	
string
	: (StringLiteral ('+' StringLiteral)*)
	| (SingleQuotedStringLiteral ('+' SingleQuotedStringLiteral)*)
	;

// LEXER

ID
	: (ALPHA | SCORE) (ALPHA | DIGIT | SCORE)*
	;

fragment ALPHA: [a-zA-Z];
fragment SCORE: '_';

STAR : '*' ;

// Floating point number (differs from that in GOALLexer as integers are not floats here).
FLOAT
	: ('+' | '-')? (DIGIT+ '.' DIGIT*)
	| ('.' DIGIT+)
	;
	
INT
	: ('+' | '-')? DIGIT+
	;

fragment DIGIT: [0-9];

fragment EscapedQuote: '\\"';
StringLiteral
	: '"' (EscapedQuote | ~[\r\n"])* '"'
	;

fragment EscapedSingleQuote: '\\\'';
SingleQuotedStringLiteral
	: '\'' (EscapedSingleQuote | ~[\r\n\'])* '\''
	;

AGENTFILENAME
	: ('"' ~[ \t\f\r\n?%*:|"<>]+ '.goal"')
	| ('\'' ~[ \t\f\r\n?%*:|\'<>]+ '.goal\'')
	;

// White space and comments.
LINE_COMMENT
	: '%' ~[\r\n]* '\r'? '\n' -> channel(HIDDEN)
	;

BLOCK_COMMENT
	: '/*' .*? '*/' -> channel(HIDDEN)
	;

WS
	: [ \t\f\r\n]+ -> skip
	;