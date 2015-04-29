package languageTools.program.test.testsection;

import java.util.List;

import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.TestCollection;

/**
 * Test sections consist of do statements for performing actions and modules, of
 * assert statements, and of test conditions.
 *
 * Upon successful evaluation a {@link TestSectionResult} should be provided. If
 * evaluation fails, a {@link TestSectionFailed} may be thrown.
 *
 * @see TestCollection
 *
 * @author mpkorstanje
 */
public interface TestSection {
	public List<MentalStateCondition> getQueries();
}