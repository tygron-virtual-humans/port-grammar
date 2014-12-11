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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A launch rule, part of a MAS program, launches agents. Launch rules that are
 * conditional on the availability of entities in an environment only launch an
 * agent when an entity becomes available and connect the agent to that entity.
 */
public class LaunchRule {
	// Maximum number of applications of this rule; 0 means there is no maximum
	private int max = 0;
	// Indicates whether rule is conditional on availability of entity or not
	private boolean conditional = false;
	// Required label of entity; empty means no label is required
	private String entityName = "";
	// Required type of the entity that agent is connected to; empty means no
	// type is required
	private String entityType = "";
	// Launch instructions for launching agents.
	private final List<Launch> instructions;

	/**
	 * Creates a new launch rule.
	 *
	 * @param instructions
	 *            A list of launch instructions.
	 */
	public LaunchRule(List<Launch> instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return The launch instructions of this rule.
	 */
	public List<Launch> getInstructions() {
		return this.instructions;
	}

	public boolean getConditional() {
		return this.conditional;
	}

	/**
	 * Sets flag to indicate that this rule is a conditional launch rule.
	 */
	public void setConditional() {
		this.conditional = true;
	}

	/**
	 * @return The name that the entity should have for this rule to be
	 *         applicable.
	 */
	public String getRequiredEntityName() {
		return this.entityName;
	}

	/**
	 * @param entityName
	 *            The name the entity should have for this rule to be
	 *            applicable.
	 */
	public void setRequiredEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * @return The type that the entity should have for this rule to be
	 *         applicable.
	 */
	public String getRequiredEntityType() {
		return this.entityType;
	}

	/**
	 * @param entityType
	 *            The name the entity should have for this rule to be
	 *            applicable.
	 */
	public void setRequiredEntityType(String entityType) {
		this.entityType = entityType;
	}

	/**
	 * @return The maximum number of applications this rule should have.
	 */
	public int getMaxNumberOfApplications() {
		return this.max;
	}

	/**
	 * @param The
	 *            maximum number of applications this rule should have.
	 */
	public void setMaxNumberOfApplications(int max) {
		this.max = max;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		if (!this.entityName.isEmpty()) {
			string.append("when ");
			Map<String, String> description = new HashMap<>();
			if (!this.entityType.isEmpty()) {
				description.put("type", this.entityType);
			}
			if (!this.entityName.isEmpty()) {
				description.put("name", this.entityName);
			}
			if (this.max > 0) {
				description.put("max", Integer.toString(this.max));
			}
			if (description.isEmpty()) {
				string.append("entity");
			} else {
				string.append(description.toString());
			}
			string.append("@env do ");
		}
		string.append("launch ");
		for (Launch launch : this.instructions) {
			string.append(launch.toString());
		}
		string.append(".");
		return string.toString();
	}
}
