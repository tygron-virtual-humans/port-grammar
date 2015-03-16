package languageTools.program.test;

import java.util.LinkedList;
import java.util.List;

import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;

public class TestMentalStateCondition {
	private final MentalStateCondition conditions;
	private final List<TestAction> actions;

	public TestMentalStateCondition(List<MentalStateCondition> conditions,
			List<TestAction> actions) {
		List<MentalFormula> formulas = new LinkedList<>();
		for (final MentalStateCondition condition : conditions) {
			formulas.addAll(condition.getSubFormulas());
		}
		this.conditions = new MentalStateCondition(formulas);
		this.actions = actions;
	}

	public MentalStateCondition getConditions() {
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
		if (first) {
			builder.append(this.conditions.toString());
			first = false;
		} else {
			builder.append(", ").append(this.conditions.toString());
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
