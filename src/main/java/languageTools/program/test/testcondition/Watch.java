package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * Watch operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator is simply printed each time.
 */
public class Watch extends TestCondition {
	/**
	 * Constructs a new watch operator for the mental state condition.
	 *
	 * @param query
	 *            mental state condition to test
	 */
	public Watch(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException(
				"Watch-condition cannot have a nested condition");
	}

	@Override
	public String getOperator() {
		return "watch";
	}
}
