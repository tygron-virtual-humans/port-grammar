package languageTools.program.test;

import java.util.ArrayList;
import java.util.List;

import languageTools.program.agent.msc.MentalStateCondition;

public class TestMentalStateCondition {
	private final List<MentalStateCondition> conditions;
	private final List<TestAction> actions;

	public TestMentalStateCondition(List<MentalStateCondition> conditions,
			List<TestAction> actions) {
		this.conditions = (conditions == null) ? new ArrayList<MentalStateCondition>(
				0) : conditions;
		this.actions = (actions == null) ? new ArrayList<TestAction>(0)
						: actions;
	}

	public List<MentalStateCondition> getConditions() {
		return this.conditions;
	}

	public List<TestAction> getActions() {
		return this.actions;
	}

	@Override
	public String toString() {
		boolean first = true;
		final StringBuilder builder = new StringBuilder();
		for (TestAction action : this.actions) {
			if (first) {
				builder.append(action.toString());
				first = false;
			} else {
				builder.append(", ").append(action.toString());
			}
		}
		for (MentalStateCondition condition : this.conditions) {
			if (first) {
				builder.append(condition.toString());
				first = false;
			} else {
				builder.append(", ").append(condition.toString());
			}
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.actions == null) ? 0 : this.actions.hashCode());
		result = prime * result
				+ ((this.conditions == null) ? 0 : this.conditions.hashCode());
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
		if (this.actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!this.actions.equals(other.actions)) {
			return false;
		}
		if (this.conditions == null) {
			if (other.conditions != null) {
				return false;
			}
		} else if (!this.conditions.equals(other.conditions)) {
			return false;
		}
		return true;
	}
}
