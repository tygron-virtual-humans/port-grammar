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
import krTools.language.Var;

/**
 * Mental state conditions occur as conditions in the rules of a GOAL program
 * but are also used for other purposes.
 * <p>
 * Mental state conditions are conjunctions of mental atoms and are represented
 * as a list of belief and goal literals. The order of literals is important for
 * evaluation. Because GOAL also allows the use of macros, a mental state
 * condition is represented as a list of {@link MentalFormula}s.
 * </p>
 */
public class MentalStateCondition {

	/**
	 * A {@link MentalStateCondition} is defined in terms of a list of
	 * {@link MentalFormula}s. Mental formulas are used here to be able to
	 * define an MSC in terms of both {@link MentalLiteral}s as well as
	 * {@link Macro}s. So {@link #formulas} stores the <i>original</i>
	 * definition of this {@link MentalStateCondition} but should only be used if
	 * this original definition is needed for presentation purposes. For al
	 * other purposes, use {@link #literals}.
	 */
	private final List<MentalFormula> formulas;

	/**
	 * Creates a {@link MentalStateCondition} from a list of {@link MentalFormula}.
	 * Mental state conditions occur as conditions in the rules of a GOAL
	 * program but are also used for other purposes.
	 * 
	 * @param formulas
	 *            The formulas from which the mental state condition is created.
	 * @param source
	 *            The location of definition of the mental state condition in
	 *            the source code. May be null if not created by a parser.
	 */
	public MentalStateCondition(List<MentalFormula> formulas) {
		this.formulas = formulas;
	}

	/**
	 * Returns the sub-{@link MentalFormula}s of this {@link MentalStateCondition},
	 * where a sub-formula may be a {@link Macro} or a {@link MentalLiteral}.
	 * 
	 * @return A {@link List} of the sub-formulas of this mental state
	 *         condition.
	 */
	public List<MentalFormula> getSubFormulas() {
		return this.formulas;
	}

	/**
	 * 
	 */
	public Set<Var> getFreeVar() {
		Set<Var> freeVar = new LinkedHashSet<Var>();
		for (MentalFormula formula : this.formulas) {
			freeVar.addAll(formula.getFreeVar());
		}
		return freeVar;
	}

	/**
	 * Applies a given substitution to this {@link MentalStateCondition}.
	 * 
	 * @param subst
	 *            The substitution that is applied to the mental state
	 *            condition.
	 */
	public MentalStateCondition applySubst(Substitution subst) {
		ArrayList<MentalFormula> instantiatedFormulas = new ArrayList<MentalFormula>();
		for (MentalFormula formula : this.formulas) {
			instantiatedFormulas.add(formula.applySubst(subst));
		}
		return new MentalStateCondition(instantiatedFormulas);
	}

	/**
	 * @return A string representation of this mental state condition.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		Iterator<MentalFormula> formulas = this.formulas.iterator();
		if (!formulas.hasNext()) {
			str.append("true");
		} else {
			// Process macros
			while(formulas.hasNext()) {
				str.append(formulas.next());
				str.append((formulas.hasNext() ? ", " : ""));
			}
		}
		
		return str.toString();
	}

}
