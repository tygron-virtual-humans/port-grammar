package languageTools.program.test;

import languageTools.program.agent.actions.UserSpecAction;

public class TestAction {
	private final UserSpecAction action;
	private final boolean positive;

	public TestAction(UserSpecAction action, boolean positive) {
		this.action = action;
		this.positive = positive;
	}

	public UserSpecAction getAction() {
		return this.action;
	}

	public boolean isPositive() {
		return this.positive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result + (this.positive ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TestAction other = (TestAction) obj;
		if (this.action == null) {
			if (other.getAction() != null) {
				return false;
			}
		} else if (!this.action.equals(other.getAction())) {
			return false;
		}
		if (this.positive != other.isPositive()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!this.positive) {
			builder.append("not(");
		}
		builder.append("do(").append(this.action).append(")");
		if (!this.positive) {
			builder.append(")");
		}
		return builder.toString();
	}
}
