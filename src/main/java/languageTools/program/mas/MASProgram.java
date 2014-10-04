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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import krTools.KRInterface;
import krTools.parser.SourceInfo;
import languageTools.program.Program;
import eis.iilang.Parameter;

/**
 * A MAS program consists of:
 * <ul>
 * 	<li>An (optional) <i>environment section</i>, including:</li>
 * 	<ul>
 * 		<li>a reference to an environment interface file</li>
 * 		<li>an optional list of key-value pairs for initializing the environment.</li>
 * 	</ul>
 *  <li>An <i>agent files section</i> with a non-empty list of references to GOAL agent programs.</li>
 *  <li>A <i>launch policy section</i> with a non-empty list of launch rules.</li>
 * </ul>
 */
public class MASProgram extends Program {

	/**
	 * The environment interface file.
	 */
	private File environmentfile = null;
	/**
	 * The environment initialization parameters.
	 */
	private Map<String, Parameter> parameters = new HashMap<String, Parameter>();
	
	/**
	 * The agent file references.
	 */
	private List<File> agentFiles = new ArrayList<File>();
	/**
	 * Knowledge representation technology associated with agent file.
	 */
	private Map<File, KRInterface> agentFile2krInterface = new HashMap<File, KRInterface>();
	
	/**
	 * An ordered list of launch rules.
	 */
	private final List<LaunchRule> launchRules = new ArrayList<LaunchRule>();

	/**
	 * Creates a new (empty) MAS program.
	 * 
	 * @param masfile The source file used to construct this MAS program.
	 */
	public MASProgram(SourceInfo masfile) {
		super(masfile);
	}

	/**
	 * @return The environment interface file.
	 */
	public File getEnvironmentfile() {
		return environmentfile;
	}

	/**
	 * Sets the environment interface file specified in the MAS file.
	 * 
	 * @param environmentfile The environment interface file.
	 */
	public void setEnvironmentfile(File environmentfile) {
		this.environmentfile = environmentfile;
	}
	
	/**
	 * @return {@code true} if environment file has been specified (correctly); {@code false} otherwise. 
	 */
	public boolean hasEnvironment() {
		return (environmentfile != null);
	}
	
	/**
	 * @return The environment initialization parameters.
	 */
	public Map<String, Parameter> getInitParameters() {
		return parameters;
	}
	
	/**
	 * Adds a key-value pair to the map of environment initialization parameters.
	 * 
	 * @param key The key of the parameter.
	 * @param value The value of the initialization parameter (an EIS language parameter).
	 */
	public void addInitParameter(String key, eis.iilang.Parameter value) {
		parameters.put(key, value);
	}
	
	/**
	 * @return The list of agent file references.
	 */
	public List<File> getAgentFiles() {
		return agentFiles;
	}
	
	/**
	 * Adds an agent file to this MAS program.
	 *  
	 * @param file The agent file that is added.
	 */
	public void addAgentFile(File file) {
		agentFiles.add(file);
	}

	/**
	 * @return The KR interface associated with the agent file.
	 */
	public KRInterface getKRInterface(File file) {
		return agentFile2krInterface.get(file);
	}

	/**
	 * @param krInterface The KR interface to set
	 */
	public void setKRInterface(File file, KRInterface krInterface) {
		agentFile2krInterface.put(file, krInterface);
	}

	/**
	 * @return The launch rules.
	 */
	public List<LaunchRule> getLaunchRules() {
		return launchRules;
	}
	
	/**
	 * Adds a launch rule to this MAS program.
	 * 
	 * @param rule The launch rule that is added.
	 */
	public void addLaunchRule(LaunchRule rule) {
		launchRules.add(rule);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canRun() {
		if (isValid()) {
			// TODO Perhaps should not be here? MAS program cannot tell itself whether it can run or not...
		}
		return false;
	}

	/**
	 * @return A string with all program parts of this {@link MASProgram}.
	 */
	@Override
	public String toString() {
		return toString("", " ");
	}
	
	/**
	 * Builds a string representation of this {@link MASProgram}.
	 * 
	 * @param linePrefix A prefix used to indent parts of a program, e.g., a single space or tab.
	 * @param indent A unit to increase indentation with, e.g., a single space or tab.
	 * @return A string-representation of this MAS program.
	 */
	@Override
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();
		
		// MAS file name
		str.append(linePrefix + "<MAS file: " + getSourceFile().getName() + ",\n");
		
		// Environment section
		if (hasEnvironment()) {
			str.append(linePrefix + indent + "<environment interface: " + environmentfile.getName() + ">,\n");
		}
		if (!parameters.isEmpty()) {
			str.append(linePrefix + indent + "<environment initialization parameters: " + parameters + ">,\n");
		}
		
		// Agent files section
		for (File file : agentFiles) {
			str.append(linePrefix + indent + "<agent file: " + file + " uses KR interface " + agentFile2krInterface.get(file) + ">,\n");
		}
		
		// Launch policy section
		for (LaunchRule rule : launchRules) {
			str.append(linePrefix + indent + "<laucnh rule: " + rule + ">,");
		}
		
		str.append(linePrefix + ">");
		
		return str.toString();
	}

}
