package languageTools.program.test.testcondition;

import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * Always operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should always hold during the execution
 * of the actions in the EvaluateIn rule.
 *
 * @author mpkorstanje
 */
public class Always extends TestCondition {
	/**
	 * Constructs a new always operator for the mental state condition.
	 *
	 * @param query
	 *            mental state condition to test
	 */
	public Always(MentalStateCondition query) {
		super(query);
	}

	@Override
	public String getOperator() {
		return "always";
	}
}
