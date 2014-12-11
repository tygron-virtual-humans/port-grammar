package languageTools.errors.test;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum TestError implements ValidatorErrorType {
	/* Main settings */
	MAS_MISSING, MAS_INVALID, AGENT_INVALID, TIMEOUT_INVALID,
	/* Test contents (1) */
	TEST_DUPLICATE, TEST_INVALID_SIZE, TEST_MISSING_AGENT, TEST_MISSING, TEST_MISSING_NAME,
	/* Test contents (2) */
	TEST_MISSING_ACTION, TEST_MISSING_TEST, TEST_INVALID_TEST, TEST_MISSING_MODULE,
	/* Test contents (3) */
	TEST_MISSING_OPERATOR, TEST_INVALID_ACTION, TEST_INVALID_QUERY;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.TestErrorMessages");

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
