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

package languageTools.program.agent.rules;

import krTools.language.Substitution;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * A rule consists of a condition (body) and an action (head). The condition of
 * a rule is a {@link MentalStateCondition}. The action of a rule is an
 * {@link ActionCombo}. A rule is applicable if the condition of the rule AND
 * the precondition of the action hold. In that case, the action can be selected
 * for execution.
 */
public abstract class Rule {

	/**
	 * The condition of the rule.
	 */
	private final MentalStateCondition condition;
	/**
	 * The action of the rule.
	 */
	private ActionCombo action;

	/**
	 * Creates a new {@link Rule}
	 * 
	 * @param condition
	 *            Determines when the rule is applicable.
	 * @param action
	 *            The action to perform if the rule is applicable.
	 * @param source
	 *            The source code location of this action, if available;
	 *            {@code null} otherwise.
	 * 
	 */
	protected Rule(MentalStateCondition condition, ActionCombo action) {
		this.condition = condition;
		this.action = action;
	}

	/**
	 * Gets the condition (head) of this {@link Rule}.
	 * 
	 * @return The condition of this {@link Rule} used for evaluating whether
	 *         the rule is applicable.
	 */
	public MentalStateCondition getCondition() {
		return this.condition;
	}

	/**
	 * Returns the action of this rule.
	 * 
	 * @return The {@link ActionCombo} that is performed if this {@link Rule} is
	 *         applied.
	 */
	public ActionCombo getAction() {
		return this.action;
	}

	/**
	 * Sets the {@link ActionCombo} for this {@link Rule}.
	 * 
	 * @param action
	 *            The action to be associated with this rule.
	 */
	public void setAction(ActionCombo action) {
		this.action = action;
	}
	
	/**
	 * Applies a substitution to this rule.
	 * 
	 * @param substitution A substitution.
	 * @return A rule where variables that are bound by the substitution have been instantiated (or renamed).
	 */
	public abstract Rule applySubst(Substitution substitution);

	/**
	 * Check if this rule must evaluate as a single-goal rule. A single-goal
	 * rule evaluates different from a normal rule: it can use only a single
	 * goal base for the entire condition. This is necessary for focus=SELECT
	 * rules.<br>
	 * This is implicit in the actions of the rule: If (at least one) of the
	 * actions in the rule is a module AND that module is focus=SELECT, this
	 * rule will be a single-goal rule.
	 */
	public boolean isRuleSinglegoal() {
		for (Action<?> a : action) {
			if (a instanceof ModuleCallAction
					&& ((ModuleCallAction) a).getTarget().getFocusMethod() == FocusMethod.SELECT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return A string with the condition and action(s) of this rule.
	 */
	@Override
	public String toString() {
		return "<condition: " + condition + ", action: " + action + ">";
	}
	
	/**
	 * Pretty print possibly incomplete rule for error reporting.
	 * 
	 * @return String with rule.
	 */
	public abstract String prettyPrint();

}
