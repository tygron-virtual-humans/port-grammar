package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

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
	protected final TestMentalStateCondition query;
	/**
	 * An optional nested condition (... -> ...)
	 */
	protected TestCondition nested;

	/**
	 * @return the mental state condition of the query
	 */
	public TestMentalStateCondition getQuery() {
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
	public TestCondition(TestMentalStateCondition query) {
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

	/**
	 * @return A readable version of the temporal operator.
	 */
	public abstract String getOperator();

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getOperator());
		str.append(" ");
		str.append(this.query.toString());
		if (this.nested != null) {
			str.append(" -> ");
			// str.append(this.nested.toString());
		}
		return str.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getOperator() == null) ? 0 : getOperator().hashCode());
		result = prime * result
				+ ((this.query == null) ? 0 : this.query.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TestCondition other = (TestCondition) obj;
		if (getOperator() == null) {
			if (other.getOperator() != null) {
				return false;
			}
		} else if (!getOperator().equals(other.getOperator())) {
			return false;
		}
		if (this.query == null) {
			if (other.getQuery() != null) {
				return false;
			}
		} else if (!this.query.equals(other.getQuery())) {
			return false;
		}
		return true;
	}
}
