package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

/**
 * While operator. When the mental state condition evaluated by this operator
 * does not hold at some point during the execution of the actions in the
 * EvaluateIn section, the corresponding agent is terminated.
 *
 * @author V.Koeman
 */
public class While extends TestCondition {
	/**
	 * Constructs a new While operator
	 *
	 * @param query
	 *            to evaluate
	 */
	public While(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException(
				"Boundaries cannot have a nested condition");
	}

	@Override
	public String getOperator() {
		return "while";
	}
}
