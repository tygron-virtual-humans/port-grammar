package languageTools.program.test.testsection;

import java.util.ArrayList;
import java.util.List;

import languageTools.program.agent.msc.MentalStateCondition;

/**
 * A mental state test is executed on the agents {@link RunState}. This can be
 * used to check if a certain condition holds once the agent has executed the
 * modules under test.
 *
 * @author M.P. Korstanje
 */
public class AssertTest implements TestSection {
	private final String message;
	private final MentalStateCondition condition;

	/**
	 * Constructs a new assert test.
	 *
	 * @param condition
	 *            to test
	 * @param message
	 *            to display when test fails.
	 */
	public AssertTest(MentalStateCondition condition, String message) {
		this.condition = condition;
		this.message = message;
	}

	/**
	 * Constructs a new assert test.
	 *
	 * @param condition
	 *            to test
	 */
	public AssertTest(MentalStateCondition condition) {
		this(condition, "");
	}

	/**
	 * @return the message to display if the test does not pass.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return the query that is tested.
	 */
	public MentalStateCondition getMentalStateTest() {
		return this.condition;
	}

	@Override
	public List<MentalStateCondition> getQueries() {
		List<MentalStateCondition> condition = new ArrayList<>(1);
		condition.add(this.condition);
		return condition;
	}

	@Override
	public String toString() {
		return "assert " + this.condition.toString();
	}
}
