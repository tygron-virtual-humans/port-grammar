/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
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

package languageTools.parser;

import java.io.File;

import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.Token;

/**
 * Container that stores a position in a file or stream by means of a line
 * number index and character position on that line.
 */
public class InputStreamPosition implements SourceInfo,
		Comparable<InputStreamPosition> {

	/**
	 * The name or description of the file or other stream this
	 * {@link InputStreamPosition} points into.
	 */
	private final File source;
	/**
	 * On what line the input stream's pointer is located (0-based)
	 */
	private final int lineNumber;
	/**
	 * At which character on the given line the input stream's pointer is
	 * located (0-based)
	 */
	private final int characterPosition;
	/**
	 * The current (token) startindex of the stream's pointer
	 */
	private final int startIndex;
	/**
	 * The current (token) stopindex of the stream's pointer
	 */
	private int stopIndex;

	/**
	 * TODO
	 *
	 * @param start
	 * @param stop
	 * @param sourceFile
	 */
	public InputStreamPosition(Token start, Token stop, File sourceFile) {
		this(start.getLine(), start.getCharPositionInLine() + 1, start
				.getStartIndex(), stop.getStopIndex(), sourceFile);
	}

	/**
	 * TODO
	 *
	 * @param token
	 * @param index
	 * @param source
	 */
	public InputStreamPosition(Token token, int index, File source) {
		this(token.getLine(), token.getCharPositionInLine() + 1, index, token
				.getText() == null ? index : index + token.getText().length(),
						source);
	}

	/**
	 * TODO
	 * 
	 * @param lineNumber
	 * @param characterPosition
	 * @param startIndex
	 * @param stopIndex
	 * @param source
	 */
	public InputStreamPosition(int lineNumber, int characterPosition,
			int startIndex, int stopIndex, File source) {
		this.source = source;
		this.lineNumber = lineNumber;
		this.characterPosition = characterPosition;
		this.startIndex = startIndex;
		this.stopIndex = stopIndex;
	}

	/**
	 * @return The source file this input stream position is associated with.
	 */
	@Override
	public File getSource() {
		return this.source;
	}

	/**
	 * @return The line number this marker marks.
	 */
	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * @return The index of the character in its line that this marker marks.
	 */
	@Override
	public int getCharacterPosition() {
		return this.characterPosition;
	}

	/**
	 * @return The (token) startindex of the character that this marker marks.
	 */
	public int getStartIndex() {
		return this.startIndex;
	}

	/**
	 * @return The (token) stopindex of the character that this marker marks.
	 */
	public int getStopIndex() {
		return this.stopIndex;
	}

	/**
	 * @param stopIndex
	 *            The (token) stopindex of the character that this marker marks.
	 */
	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}

	/**
	 * @return A short representation of this {@link InputStreamPosition}. The
	 *         returned value is of the format <code>L&lt;LINE&gt;,
	 * C&lt;COL&gt;</code>.
	 */
	public String toShortString() {
		return "L" + (this.lineNumber + 1) + ", C"
				+ (this.characterPosition + 1);
	}

	/**
	 * Determines if this {@link InputStreamPosition} is located after the given
	 * location. WARNING: the line number is 0-based, while the line number in
	 * this {@link InputStreamPosition} is 1-based.
	 *
	 * @param source
	 *            The referenced file.
	 * @param lineNumber
	 *            The referenced line number.
	 * @return {@code true} iff this {@link InputStreamPosition} is located in
	 *         the given file, after or at the start of the given line.
	 */
	public boolean definedAfter(File source, int lineNumber) {
		if (!this.source.equals(source)) {
			return false;
		}
		return this.lineNumber >= lineNumber + 1;
	}

	public InputStreamPosition end(InputStreamPosition end) {
		if (end != null) {
			this.stopIndex = end.getStopIndex();
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(this.lineNumber);
		builder.append(", position ");
		builder.append(this.characterPosition);
		if (this.source != null) {
			builder.append(" in ");
			builder.append(this.source.getName());
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = (31 * this.lineNumber) << 16 + this.characterPosition;
		if (this.source != null) {
			hash += this.source.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof InputStreamPosition)) {
			return false;
		}
		InputStreamPosition that = (InputStreamPosition) other;

		if (this.lineNumber != that.lineNumber) {
			return false;
		} else if (this.characterPosition != that.characterPosition) {
			return false;
		}

		if (this.source == null) {
			return this.source == null;
		} else {
			return this.source.getAbsoluteFile().equals(
					this.source.getAbsoluteFile());
		}
	}

	@Override
	public int compareTo(InputStreamPosition o) {
		// ASSUMES the two ISPs being compared are in the same file.

		// first order by line number
		if (this.lineNumber < o.lineNumber) {
			return -1;
		} else if (this.lineNumber > o.lineNumber) {
			return 1;
		} else
			// then by character position
			if (this.characterPosition < o.characterPosition) {
				return -1;
			} else if (this.characterPosition > o.characterPosition) {
				return 1;
			} else {
				return 0;
			}
	}

	@Override
	public String getMessage() {
		return new String();
	}

}
