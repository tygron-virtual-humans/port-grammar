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

package languageTools.analyzer.mas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.mas.MASError;
import languageTools.errors.mas.MASWarning;
import languageTools.program.mas.MASProgram;

import org.junit.Test;

public class MASValidatorErrorTest {

	List<Message> syntaxerrors;
	List<Message> errors;
	List<Message> warnings;
	MASProgram program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource
	 *            The MAS file used in the test.
	 */
	private void setup(String resource) {
		MASValidator validator = new MASValidator(resource);
		validator.validate();

		this.syntaxerrors = validator.getSyntaxErrors();
		this.errors = validator.getErrors();
		this.warnings = validator.getWarnings();
		this.program = validator.getProgram();

		List<Message> all = new LinkedList<>();
		all.addAll(this.syntaxerrors);
		all.addAll(this.errors);
		all.addAll(this.warnings);
		System.out.println(this.program.getSourceFile() + ": " + all);
	}

	@Test
	public void test_ENVIRONMENT_COULDNOT_FIND() {
		setup("src/test/resources/languageTools/analyzer/mas/test_ENVIRONMENT_COULDNOT_FIND.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(MASError.ENVIRONMENT_COULDNOT_FIND, this.errors.get(0)
				.getType());

		// MAS should produce no warnings
		assertTrue(this.warnings.isEmpty());

		assertEquals(null, this.program.getEnvironmentfile());
	}

	@Test
	public void test_INIT_UNRECOGNIZED_PARAMETER() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_UNRECOGNIZED_PARAMETER.mas2g");

		// MAS should not produce any syntax errors
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.NOVIABLEALTERNATIVE, this.syntaxerrors.get(0)
				.getType());

		// MAS should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(MASError.INIT_UNRECOGNIZED_PARAMETER, this.errors.get(0)
				.getType());

		// MAS should produce no warnings
		assertTrue(this.warnings.isEmpty());

		assertEquals(new HashMap<String, Object>(),
				this.program.getInitParameters());
	}

	@Test
	public void test_AGENTFILE_OTHER_EXTENSION() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_OTHER_EXTENSION.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(MASError.AGENTFILE_OTHER_EXTENSION, this.errors.get(0)
				.getType());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		// MAS should produce warning that no agents were added to MAS program
		assertEquals(MASWarning.AGENTFILES_NO_AGENTS, this.warnings.get(0)
				.getType());

		// MAS should have empty list of agent files
		assertTrue(this.program.getAgentFiles().isEmpty());
	}

	@Test
	public void test_AGENTFILE_COULDNOT_FIND() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_COULDNOT_FIND.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(MASError.AGENTFILE_COULDNOT_FIND, this.errors.get(0)
				.getType());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		// MAS should produce warning that no agents were added to MAS program
		assertEquals(MASWarning.AGENTFILES_NO_AGENTS, this.warnings.get(0)
				.getType());

		// MAS should have empty list of agent files
		assertTrue(this.program.getAgentFiles().isEmpty());
	}

	@Test
	public void test_KRINTERFACE_NOT_SUPPORTED() {
		setup("src/test/resources/languageTools/analyzer/mas/test_KRINTERFACE_NOT_SUPPORTED.mas2g");

		// MAS should produce a syntax error
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(MASError.KRINTERFACE_NOT_SUPPORTED, this.errors.get(0)
				.getType());

		// MAS should produce no warnings
		assertEquals(0, this.warnings.size());

		// MAS program should have 1 agent file
		assertEquals(1, this.program.getAgentFiles().size());
	}

}
