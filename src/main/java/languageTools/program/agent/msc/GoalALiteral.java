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

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.selector.Selector;

public class GoalALiteral extends MentalLiteral {

	public GoalALiteral(boolean polarity, Selector selector, Query query,
			SourceInfo info) {
		super(polarity, selector, query, info);
	}

	@Override
	public MentalLiteral applySubst(Substitution substitution) {
		return new GoalALiteral(this.polarity,
				this.selector.applySubst(substitution),
				this.query.applySubst(substitution), this.info);
	}

	@Override
	public String getOperator() {
		return AgentProgram.getTokenName(GOAL.GOALA_OP);
	}

}
