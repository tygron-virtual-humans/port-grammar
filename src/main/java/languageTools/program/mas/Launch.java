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

package languageTools.program.mas;

import java.io.File;

/**
 * A launch (instruction) is an instruction, part of a launch rule, in a MAS
 * file to launch an agent.
 */
public class Launch {

	/**
	 * The agent file that is used for launching an agent.
	 */
	private final File agentFile;
	/**
	 * The name given to the agent when it is launched; three options:
	 * <ul>
	 * <li>The name of the entity is used (the default)</li>
	 * <li>The name specified in the launch instruction is used</li>
	 * <li>The base name of the agent file is used</li>
	 * </ul>
	 */
	private String givenName = "*";
	/**
	 * The number of agents that should be launched; default is one.
	 */
	private int numberOfAgentsToLaunch = 1;

	/**
	 * Creates a launch instruction.
	 *
	 * @param agentFile
	 *            The agent file used by this launch instruction.
	 */
	public Launch(File agentFile) {
		this.agentFile = agentFile;
	}

	/**
	 * @return The agent-file.
	 */
	public File getAgentFile() {
		return this.agentFile;
	}

	/**
	 * Creates a given name for agent, derived from either the name of the
	 * entity the agent is connect to or a given name, and the number of
	 * applications of the launch instruction.
	 *
	 * @param entityName
	 *            The name of the entity that this agent is connected to, if
	 *            any. Assumes that entity names are unique.
	 * @param applications
	 *            The number of times this instruction has been applied. Used to
	 *            differentiate agents when this instruction is applied more
	 *            than once.
	 * @return The name that should be given to the agent that is launched.
	 */
	public String getGivenName(String entityName, int applications) {
		if (this.givenName.equals("*")) {
			return entityName;
		} else {
			return this.givenName + (applications == 0 ? "" : applications);
		}
	}

	/**
	 * @param givenName
	 *            The name that should be given to the agent that is launched.
	 */
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	/**
	 * @return The number of agents that this instruction should launch.
	 */
	public int getNumberOfAgentsToLaunch() {
		return this.numberOfAgentsToLaunch;
	}

	/**
	 * @param numberOfAgentsToLaunch
	 *            The number of agents that should be launched.
	 */
	public void setNumberOfAgentsToLaunch(int numberOfAgentsToLaunch) {
		this.numberOfAgentsToLaunch = numberOfAgentsToLaunch;
	}

	/**
	 * @return String representation of this Launch instruction.
	 */
	@Override
	public String toString() {
		return "Launch[basename=" + this.givenName + ", nr="
				+ this.numberOfAgentsToLaunch + " file=" + this.agentFile + "]";
	}

}
