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

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;

/**
 * Adopts a goal by adding it to the {@link GoalBase}.
 * 
 * <p>
 * The preconditions of an adopt action are:
 * <ul>
 * 	<li>the agent does not yet believe that the goal to be adopted is the case.</li>
 * 	<li>there is no other goal G' that subsumes the goal G to be adopted. That is, G
 * 		does not follow from G' (in combination with the agent's knowledge base).</li>
 * </ul>
 * </p>
 */
public class AdoptAction extends MentalAction {

	/**
	 * Creates an adopt action for inserting a new goal into a goal base of an agent.
	 * 
	 * @param selector The {@link Selector} of this action.
	 * @param goal The goal, i.e. {@link Update}, to be adopted.
	 */
	public AdoptAction(Selector selector, Update goal, SourceInfo info) {
		super(AgentProgram.getTokenName(GOAL.ADOPT), selector, info);
		addParameter(goal);
	}

	/**
	 * Returns the goal, represented by an {@link Update}, that is to be adopted.
	 * 
	 * @return The goal to be adopted.
	 */
	public Update getUpdate() {
		return getParameters().get(0);
	}

	/**
	 * Returns the precondition of this {@link AdoptAction}. 
	 * 
	 * <p>An adopt action can be performed if the agent does not believe that the goal
	 * that is adopted has already been achieved, i.e., believes it already to be the
	 * case, and the goal does not follow from one of the goals that are already present
	 * in the goal base.</p>
	 * 
	 * @return A {@link MentalStateCondition} that represents the action's precondition.
	 */
	@Override
	public MentalStateCondition getPrecondition() {
		// Get the goal this action should add
		Query query = this.getUpdate().toQuery();
		List<MentalFormula> formulalist = new ArrayList<MentalFormula>();
		// Construct the belief part of the query: NOT(BEL(query)).
		formulalist.add(new BelLiteral(false, getSelector(), query, getSourceInfo()));
		// Construct the goal part of the query: NOT(GOAL(query)).
		formulalist.add(new GoalLiteral(false, getSelector(), query, getSourceInfo()));
		// Combine both parts.
		return new MentalStateCondition(formulalist);
	}

	@Override
	public AdoptAction applySubst(Substitution substitution) {
		return new AdoptAction(getSelector().applySubst(substitution), 
				getUpdate().applySubst(substitution), getSourceInfo());
	}

}
