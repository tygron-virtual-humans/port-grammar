package languageTools.program.test.testsection.testconditions;

import languageTools.program.agent.Module;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.testsection.EvaluateIn;

/**
 * AtEnd operator for LTL queries in {@link EvaluateIn}. The mental state
 * condition evaluated by this operator should hold after the execution of the
 * actions in the EvaluateIn rule.
 *
 * @author mpkorstanje
 */
public class AtEnd extends TestCondition {
	private final Module module;

	/**
	 * Constructs a new AtEnd operator
	 *
	 * @param query
	 *            to evaluate at the end of a module
	 * @param module
	 *            the module (optionally null)
	 */
	public AtEnd(MentalStateCondition query, Module module) {
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
		return "AtEnd [query=" + this.query + ", module=" + getModuleName()
				+ "]";
	}
}
