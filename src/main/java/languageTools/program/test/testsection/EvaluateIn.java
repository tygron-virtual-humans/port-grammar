package languageTools.program.test.testsection;

import java.nio.channels.Channel;
import java.util.List;

import languageTools.program.agent.AgentProgram;
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
	@Override
	public String toString() {
		return "EvaluateIn [conditions= " + this.conditions + ", action="
				+ this.action + ", boundary=" + this.boundary + "]";
	}

	private final List<TestCondition> conditions;
	private final DoActionSection action;
	private final TestCondition boundary;

	/**
	 * Constructs a new EvaluateIn rule.
	 *
	 * @param queries
	 *            to execute
	 * @param action
	 *            on which to evaluate queries
	 * @param boundary
	 *            an optional boundary on the evaluation (until/while)
	 * @param program
	 *            the AgentProgram source
	 */
	public EvaluateIn(List<TestCondition> queries, DoActionSection action,
			TestCondition boundary, AgentProgram program) {
		this.conditions = queries;
		this.action = action;
		this.boundary = boundary;
	}

	/**
	 * Returns the {@link TestCondition}s evaluated in this section.
	 *
	 * @return the test conditions evaluated in this section.
	 */
	public List<TestCondition> getQueries() {
		return this.conditions;
	}

	/**
	 * Returns the action or module on which queries are evaluated.
	 *
	 * @return the action or module on which queries are evaluated
	 */
	public TestSection getAction() {
		return this.action;
	}

	/**
	 * Returns the boundary condition on which queries are evaluated.
	 *
	 * @return the boundary condition on which queries are evaluated
	 */
	public TestCondition getBoundary() {
		return this.boundary;
	}
}
