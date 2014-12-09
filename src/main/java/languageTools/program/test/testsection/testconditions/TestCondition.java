package languageTools.program.test.testsection.testconditions;

import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Abstract base for any test condition. Test conditions are evaluated in the
 * context of a running agent and need to provide an evaluator that can do so.
 *
 * @author mpkorstanje
 */
public abstract class TestCondition {
	/**
	 * The mental state condition of the query
	 */
	protected final MentalStateCondition query;
	/**
	 * An optional nested condition (... -> ...)
	 */
	protected TestCondition nested;

	/**
	 * @return the mental state condition of the query
	 */
	public MentalStateCondition getQuery() {
		return this.query;
	}

	/**
	 * @return the nested condition (... -> ...) if it is present (null
	 *         otherwise)
	 */
	public TestCondition getNestedCondition() {
		return this.nested;
	}

	/**
	 * @return true when a nested condition is present
	 */
	public boolean hasNestedCondition() {
		return this.nested != null;
	}

	/**
	 * Creates a {@link TestCondition} using the mental state condition.
	 *
	 * @param query
	 *            A mental state condition.
	 */
	public TestCondition(MentalStateCondition query) {
		this.query = query;
	}

	/**
	 * Defines a nested condition (when ... -> ...)
	 *
	 * @param nested
	 *            The nested TestCondition.
	 */
	public void setNestedCondition(TestCondition nested) {
		this.nested = nested;
	}
}
