package languageTools.errors.mas;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum MASError implements ValidatorErrorType {
	/**
	 * Should be able to locate agent file.
	 */
	AGENTFILE_COULDNOT_FIND,
	/**
	 * An agent file should have extension 'goal'.
	 */
	AGENTFILE_OTHER_EXTENSION,
	/**
	 * Should be able to locate environment file.
	 */
	ENVIRONMENT_COULDNOT_FIND,
	/**
	 * Environment interface files should be jar files.
	 */
	ENVIRONMENT_NOTAJAR,
	/**
	 * Initialization parameter in environment section should be valid.
	 */
	INIT_UNRECOGNIZED_PARAMETER,
	/**
	 * KR Interface (name) should be supported.
	 */
	KRINTERFACE_NOT_SUPPORTED;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.MASErrorMessages");

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
