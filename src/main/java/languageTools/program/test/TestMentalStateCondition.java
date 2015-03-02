package languageTools.program.test;

import java.util.ArrayList;
import java.util.List;

import languageTools.program.agent.actions.UserSpecAction;
import languageTools.program.agent.msc.MentalStateCondition;

public class TestMentalStateCondition {
	private final List<MentalStateCondition> conditions;
	private final List<UserSpecAction> actions;

	public TestMentalStateCondition(List<MentalStateCondition> conditions,
			List<UserSpecAction> actions) {
		this.conditions = (conditions == null) ? new ArrayList<MentalStateCondition>(
				0) : conditions;
		this.actions = (actions == null) ? new ArrayList<UserSpecAction>(0)
						: actions;
	}

	public List<MentalStateCondition> getConditions() {
		return this.conditions;
	}

	public List<UserSpecAction> getActions() {
		return this.actions;
	}

	@Override
	public String toString() {
		boolean first = true;
		final StringBuilder builder = new StringBuilder();
		for (UserSpecAction action : this.actions) {
			if (first) {
				builder.append("do(").append(action.toString()).append(")");
				first = false;
			} else {
				builder.append(", ").append("do(").append(action.toString())
				.append(")");
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
}
