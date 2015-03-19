package languageTools.analyzer.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import goalhub.krTools.KRFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import krTools.errors.exceptions.KRInitFailedException;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentWarning;
import languageTools.program.agent.AgentProgram;

import org.junit.Test;

public class AgentSyntaxErrorTest {
	private List<Message> syntaxerrors;
	private List<Message> errors;
	private List<Message> warnings;
	private AgentProgram program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource
	 *            The GOAL agent file used in the test.
	 * @throws KRInitFailedException
	 */
	private void setup(String resource) throws KRInitFailedException {
		AgentValidator validator = new AgentValidator(resource);
		validator.setKRInterface(KRFactory.getDefaultInterface());
		validator.validate();

		this.syntaxerrors = new ArrayList<Message>(validator.getSyntaxErrors());
		this.errors = new ArrayList<Message>(validator.getErrors());
		this.warnings = new ArrayList<Message>(validator.getWarnings());
		this.program = validator.getProgram();

		List<Message> all = new LinkedList<>();
		all.addAll(this.syntaxerrors);
		all.addAll(this.errors);
		all.addAll(this.warnings);
		System.out.println(this.program.getSourceFile() + ": " + all);
	}

	@Test
	public void test_IMPORT_NOT_A_MOD2G() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_IMPORT_NOT_A_MOD2G.goal");

		// Agent file should produce 1 syntax errors
		assertEquals(1, this.syntaxerrors.size());
		assertEquals(SyntaxError.INPUTMISMATCH, this.syntaxerrors.get(0)
				.getType());

		// Agent file should not produce any errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());

		// Program has 1 module and no imported files
		assertEquals(1, this.program.getModules().size());
		assertTrue(this.program.getImportedModules().isEmpty());
	}

	@Test
	public void test_MODULE_MISSING_NAME() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MODULE_MISSING_NAME.goal");

		// Agent file should not produce any syntax errors
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.MISSINGTOKEN, this.syntaxerrors.get(0)
				.getType());

		// Agent file should produce no errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.MODULE_NEVER_USED, this.warnings.get(0)
				.getType());

		// Program has 1 module and no imported files
		assertEquals(2, this.program.getModules().size());
		assertEquals("<missing ID>", this.program.getModules().get(1).getName());
	}

	@Test
	public void test_CORRECT() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test.goal");

		// Agent file should not produce any errors or warnings
		assertTrue(this.syntaxerrors.isEmpty());
		assertTrue(this.errors.isEmpty());
		assertTrue(this.warnings.isEmpty());
	}
}
