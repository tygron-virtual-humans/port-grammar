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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * An action that an agent can perform.
 *
 * <p>
 * There are two types of actions: so-called <i>built-in</i> (also called
 * reserved) actions and so-called <i>user-specified</i> actions. Adopting and
 * dropping a goal, inserting and deleting beliefs, and sending a message are
 * examples of built-in actions.
 * </p>
 * <p>
 * By default, whenever an agent is connected to an environment, a
 * user-specified action is sent to that environment for execution. A programmer
 * can indicate using the "@int" option (inserted directly after the action name
 * and parameters in the action specification) that an action should NOT be sent
 * to an environment.
 * </p>
 * <p>
 * Every action has a precondition and a postcondition. A precondition specifies
 * the conditions that need to hold to be able to perform the action
 * (successfully); a postcondition specifies the (expected) effects of the
 * action. Only user-specified actions may have <i>multiple</i> action
 * specifications, i.e., preconditions and corresponding postconditions.
 * </p>
 */
public abstract class Action<Parameter extends Expression> implements
Expression {

	/**
	 * The name of the action.
	 */
	private final String name;
	/**
	 * The parameters of the action.
	 */
	private final List<Parameter> parameters = new ArrayList<Parameter>();
	/**
	 * The knowledge representation interface used for representing the action's
	 * parameters and pre- and post-conditions.
	 */
	private KRInterface kri;
	/**
	 * Source info about this object.
	 */
	private final SourceInfo info;

	/**
	 * Creates an action (without instantiating its parameters, if any).
	 *
	 * @param name
	 *            The name of the action.
	 * @param kri
	 *            The kr interface used for representing the action's
	 *            parameters.
	 */
	public Action(String name, SourceInfo info) {
		this.name = name;
		this.info = info;
	}

	/**
	 * Returns the name of this {@link Action}.
	 *
	 * @return The name of the action.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the parameters of this {@link Action}.
	 *
	 * @return The parameters of the action.
	 */
	public List<Parameter> getParameters() {
		return this.parameters;
	}

	/**
	 * Adds a parameter of the action.
	 *
	 * @param parameter
	 *            The parameter to be added.
	 */
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}

	/**
	 * Returns the KR interface used for representing the action's parameters
	 * and pre- and post-conditions.
	 *
	 * @return The KR interface used for representing the action's parameters
	 *         and pre- and post-conditions.
	 */
	public KRInterface getKRInterface() {
		return this.kri;
	}

	/**
	 * Sets the KR interface.
	 *
	 * @param kri
	 *            A KR interface.
	 */
	public void setKRInterface(KRInterface kri) {
		this.kri = kri;
	}

	/**
	 * @return Source info about this object.
	 */
	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	/**
	 * Returns the precondition for this {@link Action}.
	 *
	 * <p>
	 * The precondition of an action should provide the conditions for
	 * successfully performing the action. That is, in principle, if the
	 * precondition holds, one should reasonably be able to expect the action to
	 * succeed.
	 * </p>
	 *
	 * <p>
	 * The precondition is a mental state condition because the built-in actions
	 * for adopting a goal include conditions on the agent's goal base.
	 * </p>
	 *
	 * <p>
	 * This is a default implementation of the method that assumes the action
	 * can always be performed, i.e., its precondition is true (represented by
	 * an empty mental state condition).
	 *
	 * @return A {@link MentalStateCondition} that represents the action's
	 *         precondition.
	 */
	public MentalStateCondition getPrecondition() {
		List<MentalFormula> formulaList = new ArrayList<MentalFormula>();
		return new MentalStateCondition(formulaList);
	}

	// -------------------------------------------------------------
	// Implements Expression
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSignature() {
		return this.name + "/" + getParameters().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVar() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Var> getFreeVar() {
		Set<Var> vars = new LinkedHashSet<Var>();
		for (Expression parameter : getParameters()) {
			vars.addAll(parameter.getFreeVar());
		}

		return vars;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * <b>Note</b>: Assumes that expr is an {@link Action}, and, if the action
	 * does not have any parameters, that the KR interface has been initialized.
	 * </p>
	 */
	@Override
	public Substitution mgu(Expression expr) {
		Substitution substitution;
		Action<?> other = (Action<?>) expr;

		if (!getParameters().isEmpty()
				&& getParameters().size() == other.getParameters().size()) {
			// Get mgu for first parameter
			substitution = getParameters().get(0).mgu(
					other.getParameters().get(0));
			// Get mgu's for remaining parameters
			for (int i = 1; i < getParameters().size() && substitution != null; i++) {
				Substitution mgu = getParameters().get(i).mgu(
						other.getParameters().get(i));
				substitution = substitution.combine(mgu);
			}

		} else {
			substitution = this.kri
					.getSubstitution(new LinkedHashMap<Var, Term>());
		}

		return substitution;
	}

	/**
	 * Default implementation of string representation for an action.
	 */
	@Override
	public String toString() {
		String str = this.name;

		if (!getParameters().isEmpty()) {
			str += "(";
			for (int i = 0; i < getParameters().size(); i++) {
				str += getParameters().get(i);
				str += (i < getParameters().size() - 1 ? ", " : "");
			}
			str += ")";
		}

		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result
				+ ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Action<?> other = (Action<?>) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		return true;
	}

}