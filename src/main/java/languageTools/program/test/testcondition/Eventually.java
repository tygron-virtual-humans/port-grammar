package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * Eventually operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should hold at some point during the
 * execution of the actions in the EvaluateIn section.
 *
 * @author mpkorstanje
 */
public class Eventually extends TestCondition {
	/**
	 * Constructs a new Eventually operator
	 *
	 * @param query
	 *            to evaluate at the end
	 */
	public Eventually(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public String getOperator() {
		return "eventually";
	}
}
