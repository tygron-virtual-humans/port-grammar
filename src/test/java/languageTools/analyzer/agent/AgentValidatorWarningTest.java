package languageTools.analyzer.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import goalhub.krTools.KRFactory;

import java.util.List;

import krTools.errors.exceptions.KRInitFailedException;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentError;
import languageTools.errors.agent.AgentWarning;
import languageTools.program.agent.AgentProgram;

import org.junit.Test;

public class AgentValidatorWarningTest {

	List<Message> syntaxerrors;
	List<Message> errors;
	List<Message> warnings;
	AgentProgram program;

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

		this.syntaxerrors = validator.getSyntaxErrors();
		this.errors = validator.getErrors();
		this.warnings = validator.getWarnings();
		this.program = validator.getProgram();
	}

	@Test
	public void test_ACTIONSPEC_DUPLICATE_PARAMETER()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_ACTIONSPEC_DUPLICATE_PARAMETER.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 2 errors
		assertEquals(2, this.errors.size());
		assertEquals(AgentError.ACTIONSPEC_DUPLICATE_PARAMETER, this.errors
				.get(0).getType());
		assertEquals(AgentError.ACTION_USED_NEVER_DEFINED, this.errors.get(1)
				.getType());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.ACTIONSPEC_PARAMETER_NOT_USED, this.warnings
				.get(0).getType());
	}

	@Test
	public void test_ACTIONSPEC_MISSING_PRE() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_ACTIONSPEC_MISSING_PRE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce no errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce 2 warnings
		assertEquals(2, this.warnings.size());

		assertEquals(AgentWarning.ACTIONSPEC_MISSING_PRE, this.warnings.get(0)
				.getType());
		assertEquals(AgentWarning.ACTIONSPEC_MISSING_POST, this.warnings.get(1)
				.getType());
	}

	@Test
	public void test_EXITMODULE_CANNOT_REACH() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_EXITMODULE_CANNOT_REACH.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce no errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.EXITMODULE_CANNOT_REACH, this.warnings.get(0)
				.getType());
	}

	@Test
	public void test_INVALID_MODULE_OPTIONS() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_INVALID_MODULE_OPTIONS.goal");

		// Agent file should not produce any syntax errors
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.INPUTMISMATCH, this.syntaxerrors.get(0)
				.getType());

		// Agent file should produce no errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce 3 warnings
		assertEquals(3, this.warnings.size());

		assertEquals(AgentWarning.MODULE_USELESS_EXIT, this.warnings.get(0)
				.getType());
		assertEquals(AgentWarning.MODULE_ILLEGAL_FOCUS, this.warnings.get(1)
				.getType());
		assertEquals(AgentWarning.MODULE_UNKNOWN_OPTION, this.warnings.get(2)
				.getType());
	}

	@Test
	public void test_MODULE_EMPTY_PROGRAMSECTION() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MODULE_EMPTY_PROGRAMSECTION.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce no errors
		assertTrue(this.errors.isEmpty());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.MODULE_EMPTY_PROGRAMSECTION, this.warnings
				.get(0).getType());
	}

}
