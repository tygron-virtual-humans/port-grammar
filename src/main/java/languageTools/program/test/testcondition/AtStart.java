package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * AtStart operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should hold when starting the execution
 * of the specified module in the EvaluateIn rule.
 *
 * @author K.Hindriks
 */
public class AtStart extends TestCondition {
	/**
	 * Constructs a new AtStart operator
	 *
	 * @param query
	 *            to evaluate at the start of a module
	 */
	public AtStart(TestMentalStateCondition query) {
		super(query);
	}

	@Override
	public String getOperator() {
		return "atstart";
	}
}
