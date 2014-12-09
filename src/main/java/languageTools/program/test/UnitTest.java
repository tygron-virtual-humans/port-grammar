package languageTools.program.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import krTools.parser.SourceInfo;
import languageTools.program.Program;
import languageTools.program.mas.MASProgram;

/**
 * UnitTest for GOAL. A unit test consists of a list of tests
 *
 * @author mpkorstanje
 */
public class UnitTest extends Program {
	private final Map<String, AgentTest> tests = new HashMap<>();
	private MASProgram masProgram;
	private long timeout;

	/**
	 * Constructs a new unit test with no agent tests.
	 *
	 * @param masProgram
	 *            of the system under test
	 */
	public UnitTest(MASProgram masProgram, SourceInfo info) {
		this(masProgram, new LinkedList<AgentTest>(), info);
	}

	/**
	 * Constructs a new unit test.
	 *
	 * @param masProgram
	 *            of the system under test
	 * @param tests
	 *            to run
	 */
	public UnitTest(MASProgram masProgram, List<AgentTest> tests,
			SourceInfo info) {
		this(masProgram, tests, 0, info);
	}

	/**
	 * Constructs a new unit test.
	 *
	 * @param masProgram
	 *            of the system under test
	 * @param tests
	 *            to run
	 * @param timeout
	 *            duration in ms before test times out
	 */
	public UnitTest(MASProgram masProgram, List<AgentTest> tests, long timeout,
			SourceInfo info) {
		super(info);
		this.masProgram = masProgram;
		for (AgentTest t : tests) {
			this.tests.put(t.getAgentName(), t);
		}
		this.timeout = timeout;
	}

	/**
	 * Constructs a new unit test.
	 *
	 * @param unitTestFile
	 *            file containing the unit test
	 * @param masProgram
	 *            of the system under test
	 * @param timeout
	 *            duration in ms before test times out
	 */
	public UnitTest(MASProgram masProgram, long timeout, SourceInfo info) {
		this(masProgram, new LinkedList<AgentTest>(), timeout, info);
	}

	/**
	 * Constructs a new unit test.
	 */
	public UnitTest(SourceInfo info) {
		this(null, new ArrayList<AgentTest>(0), 0, info);
	}

	public void setMASProgram(MASProgram masProgram) {
		this.masProgram = masProgram;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void addTest(AgentTest test) {
		this.tests.put(test.getAgentName(), test);
	}

	public Collection<AgentTest> getTests() {
		return this.tests.values();
	}

	public MASProgram getMasProgram() {
		return this.masProgram;
	}

	/**
	 * Returns a test for the agent with the given base name or null when the
	 * agent has no test associated with it.
	 *
	 * @param agentName
	 *            to find test for
	 * @return a test or null
	 */
	public AgentTest getTest(String agentName) {
		return this.tests.get(agentName);
	}

	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public String toString(String linePrefix, String indent) {
		return ""; // TODO Auto-generated method stub
	}

	@Override
	public boolean canRun() {
		return !this.tests.isEmpty(); // TODO Auto-generated method stub
	}
}
