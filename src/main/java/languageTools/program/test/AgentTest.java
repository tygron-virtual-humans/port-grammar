/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package languageTools.program.test;

import languageTools.program.agent.Module;
import languageTools.program.test.testsection.AssertTest;

/**
 * A test program for a single agent. A test consists of three phases, a setup,
 * a test and clean up phase. In the setup phase a GOAL{@link Module} can be
 * executed that sets the bot in the proper configuration. During the test phase
 * the module under test is executed after which a test of {@link AssertTest}s
 * is ran. Finally the clean up is done by running the after module.
 *
 * A test can be ran by starting an agent that uses the
 * {@link UnitTestInterpreter} controller. Or using {@link UnitTestRun}.
 *
 * @see AgentTestResult
 * @author M.P. Korstanje
 */
public class AgentTest {
	/**
	 * Base name of the agent under test.
	 */
	private final String agentName;
	/**
	 * Module under test.
	 */
	private final TestCollection test;

	/**
	 * Constructs an a test for the agent with <code>agentName</code> as its
	 * base name. The program is the test that will be will be executed.
	 *
	 * @param agentName
	 *            base name of the agent
	 * @param program
	 *            to run
	 */
	public AgentTest(String agentName, TestCollection program) {
		this.agentName = agentName;
		this.test = program;
	}

	/**
	 * Base name of the agent under test.
	 *
	 * @return base name of the agent under test
	 */
	public String getAgentName() {
		return this.agentName;
	}

	public TestCollection getTests() {
		return this.test;
	}

	@Override
	public String toString() {
		return "AgentTest [agentName=" + this.agentName + ", test=" + this.test
				+ "]";
	}
}