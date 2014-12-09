package languageTools.program.test.testsection.testconditions;

import languageTools.program.agent.msc.MentalStateCondition;

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
	public While(MentalStateCondition query) {
		super(query);
	}

	@Override
	public String toString() {
		return "While [query=" + this.query + "]";
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException(
				"Boundaries cannot have a nested condition");
	}
}
