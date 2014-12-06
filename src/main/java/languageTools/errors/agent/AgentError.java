package languageTools.errors.agent;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum AgentError implements ValidatorErrorType {
	/**
	 * Action parameters in an action specification should not contain
	 * duplicates.
	 */
	ACTIONSPEC_DUPLICATE_PARAMETER,
	/**
	 * The same action signature should not be defined twice (either as module
	 * or action).
	 */
	ACTION_LABEL_ALREADY_DEFINED,
	/**
	 * Free variables in action parameters should be bound by precondition or
	 * mental state condition in the rule the action is used in.
	 */
	ACTIONSPEC_UNBOUND_VARIABLE,
	/**
	 * A user-defined action that is used in a rule should be specified.
	 */
	ACTION_USED_NEVER_DEFINED,
	/**
	 * Goals in the goals section of a program should be closed.
	 */
	GOAL_UNINSTANTIATED_VARIABLE,
	/**
	 * A goal should be a query but it should also be possibl to convert it into
	 * an update.
	 */
	GOALSECTION_NOT_AN_UPDATE,
	/**
	 * Imported file should exist.
	 */
	IMPORT_MISSING_FILE,
	/**
	 * An expression in a knowledge representation language that is queried
	 * should be defined.
	 */
	KR_BELIEF_QUERIED_NEVER_DEFINED,
	/**
	 * An expression in a knowledge representation language that is queried
	 * should be defined.
	 */
	KR_GOAL_QUERIED_NEVER_DEFINED,
	/**
	 * A parameter must be a variable recognized by the KR interface language
	 * that is used.
	 */
	KR_SAYS_PARAMETER_INVALID,
	/**
	 * Macro should be defined only once.
	 */
	MACRO_DUPLICATE_NAME,
	/**
	 * A macro that is used should be specified.
	 */
	MACRO_NOT_DEFINED,
	/**
	 * Macro parameters should occur in definition.
	 */
	MACRO_PARAMETERS_NOT_IN_DEFINITION,
	/**
	 * Left and right brackets should match.
	 */
	MSC_BRACKET_DO_NOT_MATCH,
	/**
	 * Not operator should be applied to a mental atom.
	 */
	MSC_INVALID_NOT,
	/**
	 * Mental state condition should start with valid mental state operator.
	 */
	MSC_INVALID_OPERATOR,
	/**
	 * Modules, except for the init module, should have a program section.
	 */
	MODULE_MISSING_PROGRAM_SECTION,
	/**
	 * Module parameters should be variables.
	 */
	PARAMETER_NOT_A_VARIABLE,
	/**
	 * Program should have either main or event or both types of modules.
	 */
	PROGRAM_NO_MAIN_NOR_EVENT,
	/**
	 * Variables in postcondition of an action specification should be bound by
	 * variables that occur in the action parameters or the precondition.
	 */
	POSTCONDITION_UNBOUND_VARIABLE,
	/**
	 * Action or module parameters should not be an anonymous variable. (This is
	 * a KR specific constraint that is checked in the agent validator.)
	 */
	PROLOG_ANONYMOUS_VARIABLE,
	/**
	 * Listall parameter cannot be a Prolog anonymous variable.
	 */
	PROLOG_LISTALL_ANONYMOUS_VARIABLE,
	/**
	 * Mental literals of type a-goal and goal-a should not contain Prolog
	 * anonymous variables.
	 */
	PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE,
	/**
	 * The body of a rule should have one or more actions.
	 */
	RULE_MISSING_BODY,
	/**
	 * Rule condition should be a valid mental state condition.
	 */
	RULE_MISSING_CONDITION,
	/**
	 * Variables in the head (action calls) of a rule should be bound by the
	 * body (condition) of the rule.
	 */
	RULE_VARIABLE_NOT_BOUND,
	/**
	 * Variables used in a selector should be bound.
	 */
	SELECTOR_VAR_NOT_BOUND,
	/**
	 * Send and sendonce actions should have a selector.
	 */
	SEND_INVALID_SELECTOR;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.AgentErrorMessages");

	@Override
	public String toReadableString(String... args) {
		try {
			return String.format(BUNDLE.getString(name()), (Object[]) args);
		} catch (MissingResourceException e1) {
			if (args.length > 0) {
				return args[0];
			} else {
				return name();
			}
		} catch (MissingFormatArgumentException e2) {
			return BUNDLE.getString(name());
		}
	}
}
