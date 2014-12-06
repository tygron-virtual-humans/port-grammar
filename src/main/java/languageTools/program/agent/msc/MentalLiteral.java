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

import krTools.KRInterface;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.selector.Selector;

/**
 * A mental literal is an object of the form <selector>.bel(...) or
 * <selector>.goal(...), (or goal-a or a-goal). It can also be of the negated
 * form, so not(...).
 */

public abstract class MentalLiteral implements MentalFormula {

	protected boolean polarity;
	protected final Query query;
	protected final Selector selector;
	protected final SourceInfo info;

	/**
	 * A mental literal is an atomic query on the mental state. Examples:
	 * bel(...), goal(...) or not(bel(...)).
	 *
	 * @param polarity
	 *            The polarity of the mental literal. Default is true. If set to
	 *            false, the literal is of the form not(...).
	 * @param selector
	 *            A {@link Selector} prefix of this mental literal.
	 * @param query
	 *            A {@link KRInterface} query.
	 * @param info
	 *            Source info about this object.
	 */
	public MentalLiteral(boolean polarity, Selector selector, Query query,
			SourceInfo info) {
		this.polarity = polarity;
		this.query = query;
		this.selector = selector;
		this.info = info;
	}

	@Override
	public abstract MentalLiteral applySubst(Substitution substitution);

	/**
	 * @return {@code true} if the literal is positive, {@code false} otherwise.
	 */
	public boolean isPositive() {
		return this.polarity;
	}

	/**
	 * Flag that indicates whether this mental literal is negated.
	 *
	 * @param polarity
	 *            {@code true} if negated, {@code false} otherwise.
	 */
	public void setPolarity(boolean polarity) {
		this.polarity = polarity;
	}

	public Query getFormula() {
		return this.query;
	}

	public Selector getSelector() {
		return this.selector;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public Set<Var> getFreeVar() {
		return this.query.getFreeVar();
	}

	public boolean isClosed() {
		return this.query.isClosed();
	}

	/**
	 * @return The main mental state operator of this literal, i.e., "bel",
	 *         "goal", etc.
	 */
	public abstract String getOperator();

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		// Process negation
		if (!isPositive()) {
			str.append("not(");
		}

		// Process selector; suppress default selector
		str.append(this.selector.toPrefixString());

		// Add mental operator and KR content
		str.append(getOperator());
		str.append("(");
		str.append(this.query.toString());
		str.append(")");

		// Add closing bracket if negated
		if (!isPositive()) {
			str.append(")");
		}

		return str.toString();
	}

}
