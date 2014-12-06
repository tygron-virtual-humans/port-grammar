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

package languageTools.symbolTable;

import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.ParserRuleContext;

public class Symbol {

	// A symbol has a name
	private final String name;
	// And a location where it was found
	private final SourceInfo info;

	/**
	 * Creates a symbol.
	 *
	 * @param name
	 *            A string representing the name of the symbol.
	 */
	public Symbol(String name, SourceInfo info) {
		this.name = name;
		this.info = info;
	}

	/**
	 * @return The name of the symbol.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The source info for this symbol.
	 */
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	/**
	 * Does not use {@link ParserRuleContext} because it does not define
	 * hashCode.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	/**
	 * Does not use {@link ParserRuleContext} because it does not define equals.
	 */
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
		Symbol other = (Symbol) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
