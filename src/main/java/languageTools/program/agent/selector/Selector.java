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

package languageTools.program.agent.selector;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * A {@link Selector} is used for indicating the mental model(s) that a mental
 * literal should be evaluated on or an action should be applied to. A selector
 * is a list of {@link SelectExpression}s, which need to be resolved into agent
 * names at runtime.
 * 
 * <p>Default selector has select expression with type {@link SelectorType.THIS}.
 * See {@link #getDefault().</p>
 */
public class Selector {
	
	/**
	 * Selector types represent different categories of selector mechanisms
	 * that can be used to prefix mental atoms and actions.
	 */
	public enum SelectorType {
		SELF, ALL, ALLOTHER, SOME, SOMEOTHER, THIS, PARAMETERLIST;
	}
	
	/**
	 * Type of this {@link Selector}.
	 */
	private final SelectorType type;
	
	/**
	 * The parameters that are part of this {@link Selector}. Should only be non-empty
	 * list in case the selector type of this selector is {@link SelectorType#PARAMETERLIST}.
	 */
	private final List<Term> parameters;

	/**
	 * Creates a {@link Selector} of a particular {@link SelectorType} without parameters. 
	 * 
	 * @param type The selector type of this {@link Selector}. 
	 * 			Should not be {@link SelectorType#PARAMETERLIST}.
	 */
	public Selector(SelectorType type) {
		this.type = type;
		// selector has no parameters
		parameters = new ArrayList<Term>();
	}
	
	/**
	 * Creates a {@link Selector} using the given parameters and sets type of selector
	 * to {@link SelectorType#PARAMETERLIST}. 
	 * 			
	 * @param parameters List of {@link Term}s.
	 */
	public Selector(List<Term> parameters) {
		this.parameters = parameters;
		// selector type must be PARAMETERLIST
		type = SelectorType.PARAMETERLIST;
	}
	
	/**
	 * @return The {@link SelectorType} of this {@link Selector}.
	 */
	public SelectorType getType() {
		return type;
	}
	
	/**
	 * @return The parameters of this {@link Selector}.
	 */
	public List<Term> getParameters() {
		return parameters;
	}
	
	/**
	 * @return The default selector, use this ;-) if no selector is specified.
	 */
	public static Selector getDefault() {
		return new Selector(SelectorType.THIS);
	}

	/**
	 * Applies a {@link Substitution} to this {@link Selector} and returns a new
	 * instantiated selector.
	 * 
	 * @param substitution A substitution.
	 * @return Selector with same type as this one where parameters have been instantiated
	 * 			by applying the substitution. No effect if selector has no parameters.
	 */
	public Selector applySubst(Substitution substitution) {
		if (parameters.isEmpty()) {
			return this;
		} else {
			List<Term> terms = new ArrayList<Term>();
			for (Term term : parameters) {
				terms.add(term.applySubst(substitution));
			}
			return new Selector(terms);
		}
	}

	/**
	 * Returns the set of free variables that occur in this {@link Selector}.
	 * 
	 * @return the set of free variables that occur in this selector.
	 */
	public Set<Var> getFreeVar() {
		Set<Var> vars = new LinkedHashSet<Var>();

		for (Term term : parameters) {
			vars.addAll(term.getFreeVar());
		}

		return vars;
	}

	/**
	 * Checks whether this {@link Selector} is closed, i.e. does not contain any
	 * occurrences of free variables.
	 * 
	 * @return {@code true} if this selector is closed; {@code false} otherwise.
	 */
	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	/**
	 * @return A string with type or list of parameters.
	 */
	@Override
	public String toString() {
		switch (type) {
		case SELF:
		case ALL:
		case ALLOTHER:
		case SOME:
		case SOMEOTHER:
		case THIS:
			return type.toString().toLowerCase();
		case PARAMETERLIST:
			StringBuilder str = new StringBuilder();
			str.append("(");			
			for (int i=0; i<parameters.size(); i++) {
				str.append(parameters.get(i).toString());
				str.append((i < parameters.size()-1 ? ", " : ""));
			}
			str.append(")");
			return str.toString();
		}
		
		return null;
	}
	
	/**
	 * Returns string that can be used as prefix for mental actions and mental literals.
	 * 
	 * @return String with selector name followed by dot or the empty string if selector
	 * 			is default selector.
	 */
	public String toPrefixString() {
		return (this.equals(getDefault()) ? "" : this + ".");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Selector other = (Selector) obj;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
