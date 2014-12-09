package languageTools.program.test.testsection.testconditions;

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
	 * @return A textual representation of the module associated with this
	 *         operator (empty string if none)
	 */
	public String getModuleName() {
		return (this.module == null) ? "" : ("[" + this.module.getName() + "]");
	}

	@Override
	public String toString() {
		return "AtStart [query=" + this.query + ", module=" + getModuleName()
				+ "]";
	}
}
