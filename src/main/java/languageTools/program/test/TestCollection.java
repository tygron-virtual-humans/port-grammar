package languageTools.program.test;

import java.util.ArrayList;
import java.util.List;

import languageTools.program.test.testsection.TestSection;

/**
 * A test program. A test program consists of a list of {@link TestSection}s.
 * These rules are interpreted in sequential order. The rules are fast failing,
 * meaning that the program will stop at the first rule that fails.
 *
 * @author mpkorstanje
 */
public class TestCollection {
	/**
	 * The name of the test.
	 */
	private final String id;
	/**
	 * A test without rules.
	 */
	public final static TestCollection EMPTY = new TestCollection("empty");

	private final List<TestSection> testSections;

	@Override
	public String toString() {
		return "Test " + this.id + " [Test Sections =" + this.testSections
				+ "]";
	}

	/**
	 * Creates a test.
	 *
	 * @param id
	 *            Name of the test.
	 * @param testSections
	 *            Parts of the test.
	 */
	public TestCollection(String id, List<TestSection> testSections) {
		this.testSections = testSections;
		this.id = id;
	}

	public List<TestSection> getTestSections() {
		return this.testSections;
	}

	/**
	 * Creates a test without any rules.
	 *
	 * @param id
	 *            Name of the test.
	 */
	public TestCollection(String id) {
		this(id, new ArrayList<TestSection>(0));
	}

	/**
	 * @return The name of this test section.
	 */
	public String getTestID() {
		return this.id;
	}

	/**
	 * @return {@code true} if the program is an actual test.
	 */
	public boolean isTest() {
		return !isBefore() && !isAfter();
	}

	/**
	 * @return true if the program is a before section
	 */
	public boolean isBefore() {
		return this.id.equalsIgnoreCase("before");
	}

	/**
	 * @return true if the program is an after section
	 */
	public boolean isAfter() {
		return this.id.equalsIgnoreCase("after");
	}
}
