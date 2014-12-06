package languageTools.errors.agent;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum AgentWarning implements ValidatorWarningType {
	/**
	 * Post-condition of an action should not be empty.
	 */
	ACTIONSPEC_MISSING_POST,
	/**
	 * Pre-condition of an action should not be empty.
	 */
	ACTIONSPEC_MISSING_PRE,
	/**
	 * Action parameters should be used in pre- or post-condition.
	 */
	ACTIONSPEC_PARAMETER_NOT_USED,
	/**
	 * Specified actions should be used.
	 */
	ACTION_NEVER_USED,
	/**
	 * There should be a matching belief for every goal (to support automated
	 * removal of goal when achieved)
	 */
	GOAL_DOES_NOT_MATCH_BELIEF,
	/**
	 * It should be possible to perform an action.
	 */
	EXITMODULE_CANNOT_REACH,
	/**
	 * Knowledge and beliefs that have been defined or declared should be used.
	 */
	KR_KNOWLEDGE_OR_BELIEF_NEVER_USED,
	/**
	 * Goal that has been defined should be used.
	 */
	KR_GOAL_NEVER_USED,
	/**
	 * A macro that is defined should be used.
	 */
	MACRO_NEVER_USED,
	/**
	 * A module option should be used once.
	 */
	MODULE_DUPLICATE_OPTION,
	/**
	 * Only user-defined modules should have focus options.
	 */
	MODULE_ILLEGAL_FOCUS,
	/**
	 * Module options should be known.
	 */
	MODULE_UNKNOWN_OPTION,
	/**
	 * Exit option should only be used if it can have an effect.
	 */
	MODULE_USELESS_EXIT,
	/**
	 * Program section of module should have rules (except for init module).
	 */
	MODULE_EMPTY_PROGRAMSECTION,
	/**
	 * Modules should be used.
	 */
	MODULE_NEVER_USED;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.AgentWarningMessages");

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
