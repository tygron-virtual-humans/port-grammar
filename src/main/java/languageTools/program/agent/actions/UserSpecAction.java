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
import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;

/**
 * A user-specified action of the form 'name(parameters)' with one or more
 * associated action specifications (i.e., precondition, postcondition pairs).
 * Parameters are optional.
 * <p>
 * A user-specified action should at least have one associated action
 * specification. In case an action has multiple action specifications the order
 * of the specifications in the program is taken into account: a specification
 * that occurs before another one is used whenever it is applicable.
 * </p>
 */
public class UserSpecAction extends Action<Term> {

	/**
	 * Representing whether the action should be sent to an external
	 * environment. Default value is {@code true} meaning that an attempt should
	 * be made to sent the action to an external environment. In case value is
	 * {@code false} no such attempt should be made.
	 */
	private final boolean external;
	/**
	 * The pre-condition of the action, i.e., a query representing a condition
	 * for successful action execution.
	 */
	private final Query precondition;
	/**
	 * The post-condition of the action, i.e., an update representing the effects of the action.
	 */
	private final Update postcondition;

	/**
	 * Creates a {@link UserSpecAction} with name, parameter list, and sets flag
	 * whether action should be sent to external environment or not.
	 * 
	 * @param name
	 *            The name of the action.
	 * @param parameters
	 *            The action parameters.
	 * @param external
	 *            Parameter indicating whether action should be sent to external
	 *            environment or not. {@code true} indicates that action should
	 *            be sent to environment; {@code false} indicates that action
	 *            should not be sent to environment.
	 */
	public UserSpecAction(String name, List<Term> parameters, boolean external, 
			Query precondition, Update postcondition, SourceInfo info) {
		super(name, info);
		
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
		this.external = external;
		this.precondition = precondition;
		this.postcondition = postcondition;
	}
	
	/**
	 * @return {@code true} if this is an external action, i.e., one that should be sent to environment.
	 */
	public boolean getExernal() {
		return external;
	}

	/**
	 * @return A {@link MentalStateCondition} of the form "bel(precondition)" that
	 * 			represents the precondition of this action.
	 */
	@Override
	public MentalStateCondition getPrecondition() {
		// Create mental state condition of the form "self.bel(precondition)".
		List<MentalFormula> formulalist = new ArrayList<MentalFormula>();
		formulalist.add(new BelLiteral(true, new Selector(SelectorType.SELF), precondition, precondition.getSourceInfo()));
		return new MentalStateCondition(formulalist);
	}
	
	/**
	 * @return An {@link Update} that represents the effect of this {@link UserSpecAction}.
	 */
	public Update getPostcondition() {
		return postcondition;
	}
	
	/**
	 * Variables in the precondition of a user-specified action are bound.
	 */
	@Override
	public Set<Var> getFreeVar() {
		Set<Var> free = super.getFreeVar();
		free.removeAll(getPrecondition().getFreeVar());
		return free;
	}

	@Override
	public UserSpecAction applySubst(Substitution substitution) {
		ArrayList<Term> parameters = new ArrayList<Term>();

		// Apply substitution to action parameters, pre- and post-condition.
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		Query precondition = this.precondition.applySubst(substitution);
		Update postcondition = this.postcondition.applySubst(substitution);

		return new UserSpecAction(getName(), parameters, external, precondition, postcondition, getSourceInfo());
	}

}
