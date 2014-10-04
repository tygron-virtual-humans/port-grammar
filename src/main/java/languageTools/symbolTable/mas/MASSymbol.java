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

package languageTools.symbolTable.mas;

import java.io.File;

import krTools.parser.SourceInfo;

import languageTools.symbolTable.Symbol;

/**
 * An agent symbol associates an agent name in the MAS file with an agent file listed in the
 * same MAS file. This info is derived from the agentfiles section in a MAS file.
 */
public class MASSymbol extends Symbol {
	
	private File file;

	public MASSymbol(String name, File file, SourceInfo info) {
		super(name, info);
		this.file = file;
	}

	/**
	 * @return The agent file.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * @return String representation of this {@link #AgentSymbol(String, File)}.
	 */
	@Override
	public String toString() {
		return "<AgentSymbol: "+ getName() + ", " + file + ">";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MASSymbol other = (MASSymbol) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

}
