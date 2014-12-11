package languageTools.program.test.testcondition;

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
	 * @return An optional module associated with this operator (null if none)
	 */
	public Module getModule() {
		return this.module;
	}

	private String getModuleName() {
		return (this.module == null) ? "" : ("[" + this.module.getName() + "]");
	}

	@Override
	public String getOperator() {
		return "atend" + getModuleName();
	}
}
