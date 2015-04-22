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

package languageTools.program.agent.msc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;

/**
 * A macro definition derived from:
 * <code>#define macroname(arglist) definition</code>
 */
public class Macro implements MentalFormula {

	/**
	 * The name/label of the macro
	 */
	private final String name;
	/**
	 * The arguments of this macro (can be empty)
	 */
	private final List<Term> parameters;
	/**
	 * The MSC this {@link Macro} is a shorthand for
	 */
	private MentalStateCondition definition;
	/**
	 * Source info object for this macro.
	 */
	private final SourceInfo info;

	/**
	 * Creates a new macro definition.
	 *
	 * @param name
	 *            The name/label of the macro
	 * @param parameters
	 *            The parameters of the macro (can be the empty list but not
	 *            null).
	 * @param definition
	 *            The {@link MentalStateCondition} the macro is a shorthand for.
	 *            If null, this macro reference still needs to be resolved. See
	 *            {@link RuleValidator#doValidate} of Should only be null for
	 *            macro instances.
	 * @param info
	 *            Source info about this object.
	 */
	public Macro(String name, List<Term> parameters,
			MentalStateCondition definition, SourceInfo info) {
		super();

		this.name = name;
		this.parameters = parameters;
		this.definition = definition;
		this.info = info;
	}

	/**
	 * The name of this {@link Macro}.
	 *
	 * @return The name of this macro.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the parameters of this {@link Macro}.
	 *
	 * @return The list of parameters of this macro.
	 */
	public List<Term> getParameters() {
		return this.parameters;
	}

	/**
	 * Returns the definition of this {@link Macro}.
	 *
	 * @return The definition of this macro.
	 */
	public MentalStateCondition getDefinition() {
		return this.definition;
	}

	/**
	 * Set the definition of this {@link Macro}.
	 *
	 * @param The
	 *            definition to be used for this macro.
	 */
	public void setDefinition(MentalStateCondition definition) {
		this.definition = definition;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	/**
	 * Different from other situations where substitutions are applied, applying
	 * a substitution to a macro means applying it only to the parameters of the
	 * macro and not to any other variables that may occur in the macro's
	 * definition.
	 *
	 * There are two issues here: 1. The variables hidden (not part of the
	 * macro's parameters) should not be instantiated by the substitution; 2.
	 * Any (new) variables introduced by applying the substitution should not be
	 * identical to any of the hidden variables (which would otherwise become
	 * 'visible' again).
	 *
	 * TODO (See also {@link Module})
	 */
	@Override
	public Macro applySubst(Substitution substitution) {
		List<Term> parameters = new ArrayList<Term>();
		for (Term term : this.parameters) {
			parameters.add(term.applySubst(substitution));
		}
		MentalStateCondition definition = this.definition
				.applySubst(substitution);

		return new Macro(this.name, parameters, definition, this.info);
	}

	@Override
	public Set<Var> getFreeVar() {
		LinkedHashSet<Var> freeVars = new LinkedHashSet<Var>();
		// the set of free variables for a macro is the variables
		// used in the parameters.
		for (Term t : this.parameters) {
			freeVars.addAll(t.getFreeVar());
			if (t.isVar() && !t.isClosed()) {
				freeVars.add((Var) t);
			}
		}
		return freeVars;
	}

	/**
	 * Two macros are considered equal iff they share the same name/label and
	 * have the same number of parameters.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Macro)) {
			return false;
		}
		Macro other = (Macro) o;
		if (!other.getName().equals(getName())) {
			return false;
		}
		if (other.getParameters().size() != getParameters().size()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 31 * getName().hashCode() + getParameters().size();
	}

	/**
	 * @return A string with macro name and parameters.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append(this.name);

		if (!this.parameters.isEmpty()) {
			str.append("(");
			Iterator<Term> pars = this.parameters.iterator();
			while (pars.hasNext()) {
				str.append(pars.next());
				str.append(pars.hasNext() ? ", " : "");
			}
			str.append(")");
		}

		return str.toString();
	}

	/**
	 * Builds a string representation of this {@link Macro}.
	 *
	 * @param linePrefix
	 *            A prefix used to indent parts of a program, e.g., a single
	 *            space or tab.
	 * @param indent
	 *            A unit to increase indentation with, e.g., a single space or
	 *            tab.
	 * @return A string-representation of this macro.
	 */
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();

		str.append(linePrefix + "<macro: " + this);

		str.append(", " + this.definition + ">");

		return str.toString();
	}

	/**
	 * Gets a string representing the signature of this {@link Macro}.
	 *
	 * @return A string of the format {macro name}/{number of parameters}
	 */
	public String getSignature() {
		return this.name.concat("/").concat(
				String.valueOf(getParameters().size()));
	}

}
