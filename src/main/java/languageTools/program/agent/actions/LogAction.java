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

package languageTools.program.agent.actions;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.selector.Selector;

/**
 * Exports the mental state to a file. The argument should be one of the
 * {@link LogOptions}s. Everything else is mapped to {@link LogOptions#TEXT}.
 *
 * TODO: - support multiple {@link LogOptions} at the same time. - support a
 * more liberal style of argument with variables? - support use of
 * {@link Selector} to be able to also log mental models of other agents.
 */
public class LogAction extends Action<Term> {

	/**
	 * The argument that determines what will be logged.
	 */
	private final String argument;

	/**
	 * Creates a {@link LogAction} that logs content to a file.
	 *
	 * @param argument
	 *            The argument that determines what needs to be logged.
	 */
	public LogAction(String argument, SourceInfo info) {
		super(AgentProgram.getTokenName(GOAL.LOG), info);
		this.argument = argument;
	}

	@Override
	public LogAction applySubst(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		return "log(" + this.argument + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((this.argument == null) ? 0 : this.argument.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LogAction other = (LogAction) obj;
		if (this.argument == null) {
			if (other.argument != null) {
				return false;
			}
		} else if (!this.argument.equals(other.argument)) {
			return false;
		}
		return true;
	}

}
