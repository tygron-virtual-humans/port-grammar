package languageTools.program.test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import krTools.parser.SourceInfo;
import languageTools.program.Program;
import languageTools.program.agent.AgentProgram;
import languageTools.program.mas.MASProgram;

/**
 * UnitTest for GOAL. A unit test consists of a list of tests
 *
 * @author mpkorstanje
 */
public class UnitTest extends Program {
	private final Map<String, AgentTest> tests = new HashMap<>();
	private MASProgram masProgram;
	private final Map<File, AgentProgram> agents = new HashMap<>();
	private long timeout;

	/**
	 * Constructs a new unit test.
	 */
	public UnitTest(SourceInfo info) {
		super(info);
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

	public void addAgent(AgentProgram agent) {
		this.agents.put(agent.getSourceFile(), agent);
	}

	public Collection<AgentTest> getTests() {
		return this.tests.values();
	}

	public Map<File, AgentProgram> getAgents() {
		return this.agents;
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

	public AgentProgram getAgent(File agentFile) {
		return this.agents.get(agentFile);
	}

	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public String toString(String linePrefix, String indent) {
		return ""; // TODO Auto-generated method stub
	}
}
