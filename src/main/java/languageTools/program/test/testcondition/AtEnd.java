package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * AtEnd operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should hold after the execution of the
 * specified module in the EvaluateIn rule.
 *
 * @author mpkorstanje
 */
public class AtEnd extends TestCondition {
	/**
	 * Constructs a new AtEnd operator
	 *
	 * @param query
	 *            to evaluate at the end of a module
	 */
	public AtEnd(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public String getOperator() {
		return "atend";
	}
}
