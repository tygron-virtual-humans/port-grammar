package languageTools.program.test;

import languageTools.program.agent.msc.MentalStateCondition;

public class TestMentalStateCondition {
	private final MentalStateCondition condition;
	private final TestAction action;
	private final boolean actionFirst;

	public TestMentalStateCondition(MentalStateCondition condition,
			TestAction action) {
		this.condition = condition;
		this.action = action;
		this.actionFirst = (condition == null) ? true : false;
	}

	public TestMentalStateCondition(TestAction action,
			MentalStateCondition condition) {
		this.condition = condition;
		this.action = action;
		this.actionFirst = (action == null) ? false : true;
	}

	public MentalStateCondition getCondition() {
		return this.condition;
	}

	public TestAction getAction() {
		return this.action;
	}

	public boolean isActionFirst() {
		return this.actionFirst;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (this.actionFirst) {
			builder.append(this.action.toString());
			if (this.condition != null) {
				builder.append(", ").append(this.condition.toString());
			}
		} else if (this.condition != null) {
			builder.append(this.condition.toString());
			if (this.action != null) {
				builder.append(", ").append(this.action.toString());
			}
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result
				+ ((this.condition == null) ? 0 : this.condition.hashCode());
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
		TestMentalStateCondition other = (TestMentalStateCondition) obj;
		if (this.action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!this.action.equals(other.getAction())) {
			return false;
		}
		if (this.condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!this.condition.equals(other.getCondition())) {
			return false;
		}
		return true;
	}
}
