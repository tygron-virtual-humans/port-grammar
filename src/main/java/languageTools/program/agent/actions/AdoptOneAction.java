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
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;

/**
 * Adopts a goal by adding it into the {@link GoalBase}.
 * <p>
 * The preconditions of the adoptone action are:
 * <ul>
 * <li>the agent does not already have a 'similar' goal of the same form;</li>
 * <li>does not yet believe the goal to be adopted is the case;</li>
 * <li>there is no other goal G' that subsumes the goal G to be adopted. That
 * is, G does not follow from G' (in combination with the agent's knowledge
 * base).</li>
 * </ul>
 * </p>
 * <p>
 * TODO: currently not yet supported because code that checks whether a
 * 'similar' goal already exists in the goal base needs still to be created.
 * Grammar GOAL.g also does not yet support adoptone action.
 * </p>
 */
public class AdoptOneAction extends MentalAction {

	// TODO: create code to compute template?
	private Query template;

	/**
	 * Creates an {@link AdoptOneAction} action from an {@link Update} formula
	 * (the goal to be adopted) and a {@link Query} template that is assumed to
	 * subsume the goal to be adopted.
	 * 
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param goal
	 *            The goal, i.e. {@link Update}, to be adopted.
	 * @param template
	 *            TODO: template that may not occur in goal base if AdoptOne
	 *            action is to be executed.
	 */
	public AdoptOneAction(Selector selector, Update goal, Query template, SourceInfo info) {
		super("adoptone", selector, info);
//		TODO: not yet part of grammar
//		super(AgentProgram.getTokenName(GOAL.ADOPTONE), selector);
		addParameter(goal);
		this.template = template;
	}

	/**
	 * Returns the precondition of this {@link AdoptOneAction}. The adoptone
	 * action can be performed if the goal to be adopted is not believed to be
	 * the case and the goal does not already follow from one of the goals in
	 * the goal base. TODO: Also, it should be checked whether a 'similar' goal
	 * does not yet exist.
	 * 
	 * @return A {@link MentalStateCondition} that represents the action's
	 *         precondition.
	 */
	@Override
	public MentalStateCondition getPrecondition() {
		// Construct the mental state condition that represents the
		// precondition.
		Query query = getParameters().get(0).toQuery();
		List<MentalFormula> formulalist = new ArrayList<MentalFormula>();
		// Construct the belief part of the query: NOT(BEL(query)).
		formulalist.add(new BelLiteral(false, getSelector(), query, getSourceInfo()));
		// Construct the goal part of the query: NOT(GOAL(query)).
		formulalist.add(new GoalLiteral(false, getSelector(), query, getSourceInfo()));
		// Combine both parts.
		// TODO: need to construct check that a 'similar' goal is absent.
		return new MentalStateCondition(formulalist);
	}
	
	@Override
	public AdoptOneAction applySubst(Substitution substitution) {
		return new AdoptOneAction(getSelector().applySubst(substitution),
				getParameters().get(0).applySubst(substitution), template, getSourceInfo());
	}

	// TODO: CHECK equals and hashCode as we now ignore the template field. Also double check applySubst and toString.

}
