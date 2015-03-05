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
 * The Test parser grammar describes the grammar rules for the tests for GOAL agent programs.
 */
 parser grammar Test;

@header{
/**
 * The parser grammar for tests for the agent programming language GOAL. The grammar imports the GOAL parser
 * grammar and uses the rules for <i>actions</i> and <i>mentalStateCondition</i>.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found in the
 * lexer grammar GOALLexer.
 */
}

options{ tokenVocab=GOALLexer; }

import GOAL;
		
// masFile is not optional but making it optional in the grammar results in nicer parse errors.
unitTest
	: 'masTest' '{' masFile? timeout? agentTests '}'
	;

masFile
	: 'mas' '=' MASFILE '.'
	;

timeout
	: 'timeout' '=' FLOAT '.'
	;

agentTests
	: agentTest*
	;

// ID must be name of agent in MAS file launch rule.
agentTest
	: ID '{' test* '}'
	;

// ID is name of a test that is executed for an agent.
test
	: ID '{' (testSection '.')* '}'
	;

testSection
	: doActions | assertTest | evaluateIn
	;

doActions
	: 'do' actions
	;

assertTest
	: 'assert' mentalStateCondition (':' (SingleQuotedStringLiteral | StringLiteral) )?
	;
	
doTest
	: ('do' PARLIST)
	| (NOT '(' 'do' PARLIST ')')
	;

evaluateIn
	: 'evaluate' '{' testCondition* '}' 'in' doActions testBoundary?
	;

testCondition
	: testConditionPart ('->' testConditionPart)* '.'
	;
testConditionPart
	: (ATSTART | ALWAYS | NEVER | EVENTUALLY | ATEND) testMentalStateCondition
	;
testBoundary
    : (UNTIL | WHILE) testMentalStateCondition
	;

testMentalStateCondition
	: (mentalStateCondition|doTest) (',' (mentalStateCondition|doTest))*
	;