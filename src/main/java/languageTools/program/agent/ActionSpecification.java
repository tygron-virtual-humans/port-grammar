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

package languageTools.program.agent;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.actions.UserSpecAction;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Container for an {@link ActionSpecification}. An action specification
 * includes:
 * <ul>
 * <li>The name and parameters of the action (a {@link UserSpecAction})</li>
 * <li>The precondition of the action that is specified (a {@link Query}), and</li>
 * <li>The postcondition of the action that is specified (a {@link Update}).</li>
 * </ul>
 *
 * An action specification is obtained from the action specification section in
 * a module in a GOAL program. Specifications look like:<br>
 * <tt>
 * 		move(X, Y) {<br>
		&nbsp;   pre{ clear(X), clear(Y), on(X, Z), not(on(X, Y)) }<br>
		&nbsp;   post{ not(on(X, Z)), on(X, Y) }<br>
		}<br>
	</tt>
 */
public class ActionSpecification {

	/**
	 * The action that is specified, including action parameters.
	 */
	private final UserSpecAction action;

	/**
	 * Creates a new {@link ActionSpecification}.
	 *
	 * @param action
	 *            The {@link UserSpecAction} that is specified.
	 * @param precondition
	 *            The precondition of the action.
	 * @param postcondition
	 *            The postcondition of the acton.
	 * @param source
	 *            The input position in the source program text.
	 */
	public ActionSpecification(UserSpecAction action) {
		this.action = action;
	}

	/**
	 * Returns the {@link UserSpecAction} that is specified.
	 *
	 * @return The action that is specified.
	 */
	public UserSpecAction getAction() {
		return this.action;
	}

	/**
	 * Returns the precondition of the specification.
	 *
	 * @return The precondition of the specification.
	 */
	public MentalStateCondition getPreCondition() {
		return this.action.getPrecondition();
	}

	/**
	 * Returns the postcondition of the specification.
	 *
	 * @return The postcondition of the specification.
	 */
	public Update getPostCondition() {
		return this.action.getPostcondition();
	}

	/**
	 * Creates a new instance of this {@link ActionSpecification} by applying
	 * the parameter substitution to it.
	 *
	 * @param subst
	 *            The {@link Substitution} that is applied to this
	 *            specification.
	 * @return An instantiation (or version with variables renamed) of this
	 *         specification obtained by applying the given substitution.
	 */
	public ActionSpecification applySubst(Substitution subst) {
		return new ActionSpecification(this.action.applySubst(subst));
	}

	/**
	 * @return A string with the name and parameters of the specified action.
	 */
	@Override
	public String toString() {
		return this.action.toString();
	}

	/**
	 * Builds a string representation of this {@link ActionSpecification}.
	 *
	 * @param linePrefix
	 *            A prefix used to indent parts of a program, e.g., a single
	 *            space or tab.
	 * @param indent
	 *            A unit to increase indentation with, e.g., a single space or
	 *            tab.
	 * @return A string-representation of this action specification.
	 */
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();

		str.append(linePrefix + "<action specification: " + this + ",\n");

		str.append(linePrefix + indent + "<precondition: " + getPreCondition()
				+ ">,\n");

		str.append(linePrefix + indent + "<postcondition: "
				+ getPostCondition() + ">\n");

		str.append(linePrefix + ">");

		return str.toString();
	}

	/**
	 * Returns a string representing the signature of this
	 * {@link ActionSpecification}.
	 *
	 * @return A string of the format {action name}/{number of parameters}.
	 */
	public String getSignature() {
		return this
				.getAction()
				.getName()
				.concat("/")
				.concat(String.valueOf(this.getAction().getParameters().size()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.action == null) ? 0 : this.action.hashCode());
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
		ActionSpecification other = (ActionSpecification) obj;
		if (this.action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!this.action.equals(other.action)) {
			return false;
		}
		return true;
	}

}
