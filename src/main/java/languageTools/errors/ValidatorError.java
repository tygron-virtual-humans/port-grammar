package languageTools.errors;

import krTools.parser.SourceInfo;

public class ValidatorError extends Message {

	public interface ValidatorErrorType extends ValidatorMessageType {
	}

	public ValidatorError(ValidatorErrorType type, SourceInfo source, String... args) {
		super(type, source, args);
	}

	@Override
	public ValidatorErrorType getType() {
		return (ValidatorErrorType) this.type;
	}

	@Override
	public String toString() {
		if (getSource() != null) {
			return "Validation error at " + getSource() + ": "
					+ this.toShortString();
		} else {
			return "Validation error: " + this.toShortString();
		}
	}
}