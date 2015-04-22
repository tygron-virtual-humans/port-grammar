package languageTools.errors.mas;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum MASWarning implements ValidatorWarningType {
	/**
	 * Environment section should have reference to environment interface file.
	 */
	ENVIRONMENT_NO_REFERENCE,
	/**
	 * Same key should not be used more than once.
	 */
	INIT_DUPLICATE_KEY,
	/**
	 * Same key should not be used more than once.
	 */
	AGENTFILE_DUPLICATE_KEY,
	/**
	 * Same name for agent should not be used more than once (naming the same
	 * file twice is OK though!).
	 */
	AGENTFILES_DUPLICATE_NAME,
	/**
	 * Agentfiles section should not be empty.
	 */
	AGENTFILES_NO_AGENTS,
	/**
	 * Agent files should be used in launch rules.
	 */
	AGENTFILE_UNUSED,
	/**
	 * Wild cards should not be used in unconditional launch rules.
	 */
	LAUNCH_INVALID_WILDCARD,
	/**
	 * Launchpolicy section should not be empty.
	 */
	LAUNCH_NO_RULES,
	/**
	 * When environment is specified, launchpolicy section should have
	 * conditional rules to connect agents to it.
	 */
	LAUNCH_NO_CONDITIONAL_RULES,
	/**
	 * References to agent files in launch rules need to be present in
	 * agentfiles section.
	 */
	AGENTFILE_NONEXISTANT_REFERENCE,
	/**
	 * Any type of entity constraint should be specified only once in a launch
	 * rule.
	 */
	CONSTRAINT_DUPLICATE,
	/**
	 * Only one environment/agentfiles/launchpolicy section is allowed
	 */
	SECTION_DUPLICATE;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.MASWarningMessages");

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
