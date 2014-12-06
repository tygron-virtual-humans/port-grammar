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

package languageTools.symbolTable.agent;

import krTools.parser.SourceInfo;
import languageTools.program.agent.msc.Macro;
import languageTools.symbolTable.Symbol;

public class MacroSymbol extends Symbol {

	private final Macro macro;

	public MacroSymbol(String name, Macro macro, SourceInfo info) {
		super(name, info);
		this.macro = macro;
	}

	/**
	 * @return The macro associated with this symbol.
	 */
	public Macro getMacro() {
		return this.macro;
	}

	/**
	 * @return String representation of this {@link #PredicateSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<MacroSymbol: " + this.macro + ">";
	}

}
