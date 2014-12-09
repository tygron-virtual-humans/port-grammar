package languageTools.program.test.testcondition;

import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * Never operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should never hold during the execution
 * of the actions in the EvaluateIn rule.
 *
 * @author V.Koeman
 */
public class Never extends TestCondition {

	/**
	 * Constructs a new never operator for the mental state condition.
	 *
	 * @param query
	 *            mental state condition to test
	 */
	public Never(MentalStateCondition query) {
		super(query);
	}

	@Override
	public String toString() {
		return "Never [query=" + this.query + "]";
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException(
				"Never-condition cannot have a nested condition");
	}
}
