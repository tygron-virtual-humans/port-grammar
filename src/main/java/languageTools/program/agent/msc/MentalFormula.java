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

import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

/**
 * Abstraction of a {@link MentalLiteral} and a {@link Macro}, to facilitate
 * storing {@link Macro}s in {@link MentalStateCondition}s.
 */
public interface MentalFormula {
	/**
	 * Applies substitution to this {@link MentalFormula}.
	 * 
	 * @param substitution
	 *            The substitution to transform this atom with.
	 * @return Mental formula in which variables bound by substitution have been substituted.
	 */
	MentalFormula applySubst(Substitution substitution);

	/**
	 * @return All free variables in this atom. When this atom is used in a
	 *         query, these variables are bound.
	 */
	Set<Var> getFreeVar();
	
	/**
	 * @return Source info object for this formula.
	 */
	SourceInfo getSourceInfo();

}
