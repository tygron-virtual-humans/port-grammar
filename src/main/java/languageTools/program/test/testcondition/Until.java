package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

/**
 * Until operator. When the mental state condition evaluated by this operator
 * holds at some point during the execution of the actions in the EvaluateIn
 * section, the corresponding agent is terminated.
 *
 * @author V.Koeman
 */
public class Until extends TestCondition {
	/**
	 * Constructs a new Until operator
	 *
	 * @param query
	 *            to evaluate
	 */
	public Until(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException(
				"Boundaries cannot have a nested condition");
	}

	@Override
	public String getOperator() {
		return "until";
	}
}
