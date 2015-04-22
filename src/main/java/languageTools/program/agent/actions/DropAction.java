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

import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.selector.Selector;

/**
 * Drops all goals entailed by the goal to be dropped from the {@link GoalBase}.
 * <p>
 * If the action is closed, the drop action can be performed.
 * </p>
 */
public class DropAction extends MentalAction {

	/**
	 * Creates a {@link DropAction} that drops all goals from the
	 * {@link GoalBase} that follow from the goal to be dropped.
	 *
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param goal
	 *            The goal, i.e., {@link Update}, to be dropped.
	 * @param source
	 *            The source code location of this action, if available;
	 *            {@code null} otherwise.
	 *            @param kr the {@link KRInterface}
	 */
	public DropAction(Selector selector, Update goal, SourceInfo info,KRInterface kr) {
		super(AgentProgram.getTokenName(GOAL.DROP), selector, info,  kr);
		addParameter(goal);
	}

	/**
	 * Returns the goal, represented by an {@link Update}, that is to be
	 * dropped.
	 *
	 * @return The goal to be dropped.
	 */
	public Update getUpdate() {
		return getParameters().get(0);
	}

	@Override
	public DropAction applySubst(Substitution substitution) {
		return new DropAction(getSelector().applySubst(substitution),
				getUpdate().applySubst(substitution), getSourceInfo(),getKRInterface());
	}

}
