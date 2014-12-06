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

package languageTools.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Extensions of files recognized by GOAL.
 */
public enum Extension {
	MAS2G("mas2g"), GOAL("goal"), PL("Prolog"), MOD2G("module"), LEARNING("lrn"), TEST2G(
			"test");

	private String extension;

	private Extension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return this.extension;
	}

	/**
	 * Determines the {@link Extension} of a certain file.
	 *
	 * @param filename
	 *            The name or path to a file to get the extension of
	 * @return The {@link Extension} of the given file, or <code>null</code> if
	 *         the file has no known extension.
	 */
	public static Extension getFileExtension(String filename) {
		try {
			return Extension.valueOf(FilenameUtils.getExtension(filename)
					.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Determines the {@link Extension} of a certain file.
	 *
	 * @param file
	 *            The file to get the extension of.
	 * @return The {@link Extension} of the given file, or <code>null</code> if
	 *         the file has no known extension.
	 */
	public static Extension getFileExtension(File file) {
		return Extension.getFileExtension(file.getName());
	}

	@Override
	public String toString() {
		return this.getExtension();
	}

}
