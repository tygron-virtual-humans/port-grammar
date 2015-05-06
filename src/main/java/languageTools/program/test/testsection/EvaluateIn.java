package languageTools.program.test.testsection;

import java.nio.channels.Channel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.testcondition.TestCondition;

/**
 * Evaluate-in statement. The evaluate-in statement evaluates a list of
 * {@link TestCondition}s while executing an action or module.
 *
 * The queries in the evaluate-in statement are evaluated by a
 * {@link TestConditionEvaluator}. Each {@link TestCondition} provides an
 * evaluator that can be used to evaluate that condition.
 *
 * When an test condition is evaluated, the corresponding evaluator is run as
 * debugger. The evaluators are {@link DebugObserver}s that listen to
 * {@link Channel#ACTION_EXECUTED_BUILTIN} and
 * {@link Channel#ACTION_EXECUTED_USERSPEC}.
 *
 * Evaluation of the test conditions happens in three phases.
 *
 * <ol>
 * <li>Install evaluators.
 * <li>Trigger {@link TestConditionEvaluator#firstEvaluation()}.
 * <li>Execute the action associated with {@link EvaluateIn} using the installed
 * Evaluators. Whenever an action is executed the test condition is evaluated.
 * <li>Trigger {@link TestConditionEvaluator#lastEvaluation()}.
 * <li>Uninstall evaluators.
 * <ol>
 *
 * @author mpkorstanje
 */
public class EvaluateIn implements TestSection {
	private final Set<TestCondition> conditions;
	private final ModuleCallAction module;
	private final TestCondition boundary;

	/**
	 * Constructs a new EvaluateIn rule.
	 *
	 * @param queries
	 *            to execute
	 * @param module
	 *            on which to evaluate queries
	 * @param boundary
	 *            an optional boundary on the evaluation (until/while)
	 * @param program
	 *            the AgentProgram source
	 */
	public EvaluateIn(Set<TestCondition> queries, ModuleCallAction module,
			TestCondition boundary, AgentProgram program) {
		this.conditions = queries;
		this.module = module;
		this.boundary = boundary;
	}

	/**
	 * Returns the {@link TestCondition}s evaluated in this section.
	 *
	 * @return the test conditions evaluated in this section.
	 */
	public Set<TestCondition> getConditions() {
		return this.conditions;
	}

	/**
	 * Returns the module on which queries are evaluated.
	 *
	 * @return the module on which queries are evaluated
	 */
	public ModuleCallAction getAction() {
		return this.module;
	}

	/**
	 * Returns the boundary condition on which queries are evaluated.
	 *
	 * @return the boundary condition on which queries are evaluated
	 */
	public TestCondition getBoundary() {
		return this.boundary;
	}

	@Override
	public List<MentalStateCondition> getQueries() {
		List<MentalStateCondition> conditions = new LinkedList<>();
		if (this.boundary != null && this.boundary.getQuery() != null) {
			conditions.add(this.boundary.getQuery().getCondition());
		}
		for (final TestCondition test : this.conditions) {
			conditions.addAll(getQueries(test));
		}
		return conditions;
	}

	private List<MentalStateCondition> getQueries(TestCondition condition) {
		List<MentalStateCondition> conditions = new LinkedList<>();
		if (condition.getQuery() != null) {
			conditions.add(condition.getQuery().getCondition());
		}
		if (condition.getNestedCondition() != null) {
			conditions.addAll(getQueries(condition.getNestedCondition()));
		}
		return conditions;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("evaluate {\n");
		for (TestCondition query : this.conditions) {
			str.append(query.toString() + "\n");
		}
		str.append("} in ").append(this.module.toString());
		if (this.boundary != null) {
			str.append(" ").append(this.boundary.toString());
		}
		return str.toString();
	}
}
