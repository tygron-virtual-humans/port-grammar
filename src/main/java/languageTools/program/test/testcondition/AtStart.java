package languageTools.program.test.testcondition;

import languageTools.program.agent.Module;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * AtStart operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should hold after the execution of the
 * specified module in the EvaluateIn rule.
 *
 * @author K.Hindriks
 */
public class AtStart extends TestCondition {
	private final Module module;

	/**
	 * Constructs a new AtStart operator
	 *
	 * @param query
	 *            to evaluate at the start of a module
	 * @param module
	 *            the module (optionally null)
	 */
	public AtStart(MentalStateCondition query, Module module) {
		super(query);
		this.module = module;
	}

	/**
	 * @return An optional module associated with this operator (null if none)
	 */
	public Module getModule() {
		return this.module;
	}

	@Override
	public String toString() {
		return "AtStart[query=" + this.query + ", module="
				+ getModule().getSignature() + "]";
	}
}
