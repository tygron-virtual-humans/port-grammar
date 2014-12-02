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
 * The GOAL parser grammar describes the grammar rules for the GOAL agent programming language elements.
 * 
 * Operators and punctuation symbols are directly referenced in the grammar to enhance readability.
 * References to <i>tokens</i> thus are implicit in the grammar. The token labels can be found in the
 * lexer grammar GOALLexer.
 */
parser grammar GOAL;

options{ tokenVocab=GOALLexer; }


// Modules

modules
	: (moduleImport | module)+ EOF
	;

moduleImport
	: '#import' MODULEFILE '.'
	;

module
	: moduleDef ('[' moduleOption (',' moduleOption)* ']')?
	  '{' knowledge? beliefs? goals? program? actionSpecs? '}'
	;

moduleDef
	: 'module' declaration
	| INIT 'module'
	| MAIN 'module'
	| EVENT 'module'
	;

moduleOption
	: key= 'exit' '=' value = ('always' | 'never' | 'nogoals' | 'noaction')
	| key = 'focus' '=' value = ('none' | 'new' | 'select' | 'filter')
	;

// Mental state sections

knowledge
	: 'knowledge' KR_BLOCK // allow empty knowledge section
	;

beliefs
	: 'beliefs' KR_BLOCK // allow empty beliefs section
	;

goals
	: 'goals' KR_BLOCK // allow empty goals section
	;
	
// Program section

// empty program section allowed because of IDE template
program
	: 'program' ('[' ruleEvaluationOrder ']')? '{' macroDef* programRule* '}'
	;

ruleEvaluationOrder
	: 'order' '=' value = ('linear' | 'linearall' | 'random' | 'randomall' | 'adaptive')
	;

macroDef
	: '#define' declarationOrCallWithTerms mentalStateCondition '.'
	;

programRule
	: IF mentalStateCondition 'then' ( actions '.' | nestedRules )
	| FORALL mentalStateCondition 'do' ( actions '.' | nestedRules )
	| LISTALL VAR '<-' mentalStateCondition 'do' ( actions '.' | nestedRules )
	;

mentalStateCondition
	: basicCondition
	| basicCondition ',' mentalStateCondition
	;

// can't use left recursion in combination with import grammar (ANTLR throws in that case)
basicCondition
	: mentalAtom
	| NOT '(' mentalAtom ')'
	| TRUE
	| declarationOrCallWithTerms	// macro
	;

mentalAtom
	: (selector '.')? mentalOperator PARLIST
	;

mentalOperator
	: op = 'bel'
	| op = 'goal'
	| op = 'a-goal'
	| op = 'goal-a'
	;

actions
	: action ('+' action)*
	;

action
	: (selector '.')? actionOperator PARLIST									
	| op = 'exit-module'
	| op = 'log'										
	| op = 'init'												
	| op = 'main'												
	| op = 'event'
	| declarationOrCallWithTerms				
	;

actionOperator
	: op = 'adopt'
	| op = 'drop'
	| op = 'insert'
	| op = 'delete'
	| op = 'send'
	| op = 'sendonce'
	| op = 'print'
	| op = 'log'
	;

selector
	: PARLIST
	| op = 'all'
	| op = 'allother'
	| op = 'self'
	| op = 'some'
	| op = 'someother'
	| op = 'this'
	;

nestedRules
	: '{' programRule+ '}'
	;

// Action specification

actionSpecs
	: 'actionspec' '{' actionSpec+ '}'
	;

actionSpec
	: declarationOrCallWithTerms (INTERNAL | EXTERNAL)? '{' precondition postcondition '}'
	;

precondition
	: 'pre' KR_BLOCK // allow empty precondition
	;

postcondition
	: 'post' KR_BLOCK // allow empty postcondition
	;

declaration
	: ID PARLIST?
	;
	
declarationOrCallWithTerms
	: ID PARLIST?
	;
