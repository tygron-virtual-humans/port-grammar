package languageTools.program.test.testsection.testconditions;

import languageTools.program.agent.msc.MentalStateCondition;
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
	public Eventually(MentalStateCondition query) {
		super(query);
	}

	@Override
	public String toString() {
		return "Eventually [query=" + this.query + "]";
	}
}
