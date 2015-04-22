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

import java.util.ArrayList;
import java.util.List;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;

/**
 * Action used for parsing purposes only. Used because at the parsing time it
 * cannot yet be determined whether the parsed object should be resolved to
 * either a user-specified action or to a focus action (module invocation).<br>
 * This 'action' is replaced during validation of the agent program by a proper
 * action.<br>
 * A UserOrFocusAction cannot be executed.
 */
public class UserSpecOrModuleCall extends Action<Term> {

	/**
	 * Creates an action that can either be a {@link ModuleCallAction} or a
	 * {@link UserSpecAction}.
	 * <p>
	 * Method calls will result in an {@link UnsupportedOperationException} with
	 * the following exceptions:
	 * <ul>
	 * <li>{@link #getName()}</li>
	 * <li>{@link #getParameters()}</li>
	 * <li>{@link #getFreeVar()}</li>
	 * <li>{@link #getRule()}</li>
	 * <li>{@link #setRule(Rule)}</li>
	 * <li>{@link #toString()}</li>
	 * </ul>
	 * </p>
	 *
	 * @param name
	 *            The name of the action.
	 * @param parameters
	 *            The parameters of the action.
	 * @param kr
	 *            the {@link KRInterface}
	 */
	public UserSpecOrModuleCall(String name, List<Term> parameters,
			SourceInfo info, KRInterface kr) {
		super(name, info, kr);

		for (Term parameter : parameters) {
			addParameter(parameter);
		}
	}

	@Override
	public Expression applySubst(Substitution substitution) {
		ArrayList<Term> parameters = new ArrayList<Term>();

		// Apply substitution to action parameters.
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}

		return new UserSpecOrModuleCall(getName(), parameters, getSourceInfo(), getKRInterface());
	}

}
