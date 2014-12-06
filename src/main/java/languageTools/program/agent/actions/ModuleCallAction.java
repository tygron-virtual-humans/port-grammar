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

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.Module.TYPE;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.rules.Rule;

/**
 * Action that makes the agent focus on a module, using one of four methods;
 * <ol>
 * <li>When the action is executed, and the agent focuses on the module, the new
 * attention set will consist of one goal, which validates the entire context of
 * the {@link Module}.<br>
 * To use this method, focus on a Module with the {@link FocusMethod#SELECT}
 * focus method.<br>
 * <br>
 * Note that this method has better performance, as no new databases need to be
 * created due to the context. (there still might be some module-specific goals
 * to be created however)<br>
 * <br>
 * </li>
 * <li>When the action is executed, and the agent focuses on the module, the new
 * attention set will consist of one goal for each of the positive
 * {@link GoalLiteral} and {@link AGoalLiteral}s in the instantiated context.<br>
 * To use this method, focus on a Module with the {@link FocusMethod#FILTER}
 * focus method.<br>
 * <br>
 * Note that this method makes the agent forget any information on its goals on
 * a lower level than the information provided in the positive goal and a-goal
 * literals. Eg (in blocksworld): a context with only
 * <code>a-goal(tower[T])</code> will make the agent forget that its original
 * goal was a conjunction of <code>on(.,.)</code> predicates.</li>
 * <li>When the action is executed, and the agent focuses on the module, the new
 * attention set will be empty.<br>
 * To use this method, focus on a Module with the {@link FocusMethod#NEW} focus
 * method.</li>
 * <li>When the action is executed, and the agent focuses on the module, the new
 * attention set will be the same attention set as before (aside from any added
 * goals from the <code>goals { }</code> section). This means that any changes
 * in the attention set will be propagated back to the parent module when the
 * agent de-focuses.<br>
 * To use this method, focus on a Module with the {@link FocusMethod#NONE} focus
 * method.
 * </ol>
 * Note that regardless of the focus method, the new attention set will always
 * contain the goals defined in the Module's <code>goals { }</code> section.
 */
public class ModuleCallAction extends Action<Term> {

	/**
	 * The module the agent will focus on if this action is executed. Must be
	 * set with #setTarget
	 */
	private final Module targetModule;

	/**
	 * Creates a new {@link ModuleCallAction}, which will focus the agent's
	 * attention to a certain module when executed.
	 *
	 * @param targetModule
	 *            The name of the {@link Module} the agent that executes this
	 *            action will focus on.
	 */
	public ModuleCallAction(Module targetModule, SourceInfo info) {
		super(targetModule.getName(), info);

		this.targetModule = targetModule;

		for (Term term : targetModule.getParameters()) {
			addParameter(term);
		}
	}

	/**
	 * Returns the module that should be entered when executing this focus
	 * action.
	 *
	 * @return The {@link Module} that will be entered when executing this
	 *         action.
	 */
	public Module getTarget() {
		return this.targetModule;
	}

	/**
	 * Applies the substitution to the parameters of the target module (only).
	 * <p>
	 * Does not apply the substitution to the target module itself. Instead
	 * substitution is passed on to module itself via
	 * {@link #run(RunState, Substitution)}.
	 * </p>
	 *
	 * @return Instantiated focus action, where free variables in parameters
	 *         have been substituted with terms from the substitution.
	 * 
	 *         TODO: check
	 */
	@Override
	public Action<Term> applySubst(Substitution substitution) {
		List<Term> parameters = new ArrayList<Term>();

		if (getTarget().getType() != TYPE.ANONYMOUS) {
			for (Term term : getParameters()) {
				parameters.add(term.applySubst(substitution));
			}
		}

		// Create new focus action with instantiated parameters.
		ModuleCallAction focus = new ModuleCallAction(getTarget(),
				getSourceInfo());
		// Store substitution for later reference when we call the target
		// module.
		// TODO: focus.substitutionToPassOnToModule = substitution;

		return focus;
	}

	/**
	 * String with name and parameters of module called, or, in case of
	 * anonymous module, list of rules in that module.
	 */
	@Override
	public String toString() {
		if (getTarget().getType() != TYPE.ANONYMOUS) {
			return super.toString();
		} else {
			StringBuilder str = new StringBuilder();
			str.append("{\n");

			for (Rule rule : getTarget().getRules()) {
				str.append(rule.toString() + "\n");
			}

			str.append("}\n");

			return str.toString();
		}
	}

}
