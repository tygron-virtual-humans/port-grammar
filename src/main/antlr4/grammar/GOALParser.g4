parser grammar GOALParser;

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
	: 'module' function
	| 'init' 'module'
	| 'main' 'module'
	| 'event' 'module'
	;

moduleOption
	: 'exit' '=' ('always' | 'never' | 'nogoals' | 'noaction')
	| 'focus' '=' ('none' | 'new' | 'select' | 'filter')
	;

// Mental state sections

knowledge
	: 'knowledge' '{' KR_BLOCK* '}' // allow empty knowledge section
	;

beliefs
	: 'beliefs' '{' KR_BLOCK* '}' // allow empty beliefs section
	;

goals
	: 'goals' '{' KR_BLOCK* '}' // allow empty goals section
	;

// Action specification

actionSpecs
	: 'actionspec' '{' actionSpec+ '}'
	;

actionSpec
	: function ('@int' | '@env')? '{' precondition postcondition '}'
	;

precondition
	: 'pre' '{' KR_BLOCK* '}' // allow empty precondition
	;

postcondition
	: 'post' '{' KR_BLOCK* '}' // allow empty postcondition
	;

// Program section

program
	: 'program' ('[' ruleEvaluationOrder ']')? '{' macroDef* programRule* '}' // empty program section allowed because of IDE template
	;

ruleEvaluationOrder
	: 'order' '=' ('linear' | 'linearall' | 'random' | 'randomall' | 'adaptive')
	;

macroDef
	: '#define' macro mentalStateCondition '.'
	;
	
macro
	: function
	;

programRule
	: 'if' mentalStateCondition 'then' ( actions | nestedRules )
	| 'forall' mentalStateCondition 'do' ( actions | nestedRules )
	| 'listall' ((ID '<-' mentalStateCondition) | (mentalStateCondition '->' ID)) 'do' ( actions | nestedRules )
	;

mentalStateCondition
	: mentalAtom
	| macro
	| mentalStateCondition ',' mentalStateCondition
	| 'not' '(' mentalStateCondition ')'
	| 'true'
	;

mentalAtom
	: (selector '.')? mentalOperator '(' KR_STATEMENT+ ')'
	;

mentalOperator
	: 'bel'
	| 'goal'
	| 'a-goal'
	| 'goal-a'
	;

actions
	: action ('+' action)* '.'
	;

action
	: (selector '.')? actionOperator '(' KR_STATEMENT+ ')'
	| function
	| EXITMODULE
	| INIT
	| MAIN
	| EVENT
	;

actionOperator
	: 'adopt'
	| 'drop'
	| 'insert'
	| 'delete'
	| 'send'
	| 'sendonce'
	| 'print'
	| 'log'
	;

selector
	: selectExpression
	| '[' selectExpression (',' selectExpression)? ']'
	;

selectExpression
	: ID
	| 'all'
	| 'allother'
	| 'self'
	| 'some'
	| 'someother'
	| 'this'
	;

nestedRules
	: '{' programRule+ '}'
	;
	
function
	: ID ('(' KR_STATEMENT+ ')')?
	;