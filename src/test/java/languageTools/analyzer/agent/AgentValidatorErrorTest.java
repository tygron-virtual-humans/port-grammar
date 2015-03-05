package languageTools.analyzer.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import goalhub.krTools.KRFactory;

import java.util.LinkedList;
import java.util.List;

import krTools.errors.exceptions.KRInitFailedException;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentError;
import languageTools.errors.agent.AgentWarning;
import languageTools.program.agent.AgentProgram;
import languageTools.symbolTable.SymbolTable;

import org.junit.Test;

public class AgentValidatorErrorTest {

	List<Message> syntaxerrors;
	List<Message> errors;
	List<Message> warnings;
	SymbolTable table;
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

		List<Message> all = new LinkedList<>();
		all.addAll(this.syntaxerrors);
		all.addAll(this.errors);
		all.addAll(this.warnings);
		System.out.println(this.program.getSourceFile() + ": " + all);
	}

	@Test
	public void test_ACTION_LABEL_ALREADY_DEFINED()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_ACTION_LABEL_ALREADY_DEFINED.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 2 errors
		assertEquals(2, this.errors.size());

		assertEquals(AgentError.ACTION_LABEL_ALREADY_DEFINED, this.errors
				.get(0).getType());
		assertEquals(AgentError.ACTION_LABEL_ALREADY_DEFINED, this.errors
				.get(1).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_ACTION_USED_NEVER_DEFINED() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_ACTION_USED_NEVER_DEFINED.goal");

		// Agent file should have no syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 4 errors
		assertEquals(4, this.errors.size());

		assertEquals(AgentError.ACTION_USED_NEVER_DEFINED, this.errors.get(0)
				.getType());
		assertEquals(AgentError.ACTION_USED_NEVER_DEFINED, this.errors.get(1)
				.getType());
		assertEquals(AgentError.MACRO_NOT_DEFINED, this.errors.get(2).getType());
		assertEquals(AgentError.KR_BELIEF_QUERIED_NEVER_DEFINED, this.errors
				.get(3).getType());

		// Agent file should produce 3 warnings
		assertEquals(3, this.warnings.size());

		assertEquals(AgentWarning.ACTION_NEVER_USED, this.warnings.get(0)
				.getType());
		assertEquals(AgentWarning.MODULE_NEVER_USED, this.warnings.get(1)
				.getType());
		assertEquals(AgentWarning.MACRO_NEVER_USED, this.warnings.get(2)
				.getType());
	}

	// @Test
	public void test_GOAL_UNINSTANTIATED_VARIABLE()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_GOAL_UNINSTANTIATED_VARIABLE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.GOAL_UNINSTANTIATED_VARIABLE, this.errors
				.get(0).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());

		// Init module has 0 goals
		assertTrue(this.program.getModules().get(0).getGoals().isEmpty());
	}

	/**
	 * Only test whether goal is not a rule clause.
	 *
	 * @throws KRInitFailedException
	 */
	@Test
	public void test_GOALSECTION_NOT_A_QUERY() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_GOALSECTION_NOT_A_QUERY.goal");

		// Agent file should not produce any syntax errors
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, this.syntaxerrors
				.get(0).getType());

		// Agent file should produce 1 error
		assertTrue(this.errors.isEmpty());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());

		// Init module has 0 goals
		assertTrue(this.program.getModules().get(0).getGoals().isEmpty());
	}

	@Test
	public void test_IMPORT_MISSING_FILE() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_IMPORT_MISSING_FILE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.IMPORT_MISSING_FILE, this.errors.get(0)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());

		// Program has 1 module and no imported files
		assertEquals(1, this.program.getModules().size());
		assertTrue(this.program.getImportedModules().isEmpty());
	}

	@Test
	public void test_KR_SAYS_PARAMETER_INVALID() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_KR_SAYS_PARAMETER_INVALID.goal");

		// Agent file should produce 2 syntax errors
		assertEquals(2, this.syntaxerrors.size());

		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, this.syntaxerrors
				.get(0).getType());

		// Agent file should produce 2 errors
		assertEquals(2, this.errors.size());

		assertEquals(AgentError.PARAMETER_NOT_A_VARIABLE, this.errors.get(0)
				.getType());
		assertEquals(AgentError.ACTION_USED_NEVER_DEFINED, this.errors.get(1)
				.getType());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.ACTION_NEVER_USED, this.warnings.get(0)
				.getType());
	}

	@Test
	public void test_KR_EXPRESSION_QUERIED_NEVER_DEFINED()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_KR_EXPRESSION_QUERIED_NEVER_DEFINED.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 3 errors
		assertEquals(3, this.errors.size());

		assertEquals(AgentError.KR_BELIEF_QUERIED_NEVER_DEFINED, this.errors
				.get(0).getType());
		assertEquals(AgentError.KR_BELIEF_QUERIED_NEVER_DEFINED, this.errors
				.get(1).getType());
		assertEquals(AgentError.KR_GOAL_QUERIED_NEVER_DEFINED,
				this.errors.get(2).getType());

		// Agent file should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.KR_KNOWLEDGE_OR_BELIEF_NEVER_USED,
				this.warnings.get(0).getType());
	}

	@Test
	public void test_MACRO_DUPLICATE_NAME() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MACRO_DUPLICATE_NAME.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.MACRO_DUPLICATE_NAME, this.errors.get(0)
				.getType());

		// Agent file should produce no warnings
		assertEquals(1, this.warnings.size());

		assertEquals(AgentWarning.MACRO_NEVER_USED, this.warnings.get(0)
				.getType());
	}

	@Test
	public void test_MACRO_PARAMETERS_NOT_IN_DEFINITION()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MACRO_PARAMETERS_NOT_IN_DEFINITION.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.MACRO_PARAMETERS_NOT_IN_DEFINITION, this.errors
				.get(0).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	// @Test
	public void test_POSTCONDITION_UNBOUND_VARIABLE()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_POSTCONDITION_UNBOUND_VARIABLE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.POSTCONDITION_UNBOUND_VARIABLE, this.errors
				.get(0).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	// @Test FIXME
	public void test_PROLOG_ANONYMOUS_VARIABLE() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_PROLOG_ANONYMOUS_VARIABLE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 3 errors
		assertEquals(3, this.errors.size());

		assertEquals(AgentError.PROLOG_ANONYMOUS_VARIABLE, this.errors.get(0)
				.getType());
		assertEquals(AgentError.PROLOG_LISTALL_ANONYMOUS_VARIABLE, this.errors
				.get(1).getType());
		assertEquals(AgentError.RULE_VARIABLE_NOT_BOUND, this.errors.get(2)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	// @Test FIXME
	public void test_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 2 errors
		assertEquals(2, this.errors.size());

		assertEquals(AgentError.PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE,
				this.errors.get(0).getType());
		assertEquals(AgentError.PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE,
				this.errors.get(1).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_MODULE_MISSING_PROGRAM_SECTION()
			throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MODULE_MISSING_PROGRAM_SECTION.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.MODULE_MISSING_PROGRAM_SECTION, this.errors
				.get(0).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_PROGRAM_NO_MAIN_NOR_EVENT() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_PROGRAM_NO_MAIN_NOR_EVENT.goal");

		// Agent file should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.PROGRAM_NO_MAIN_NOR_EVENT, this.errors.get(0)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_RULE_MISSING_BODY() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_RULE_MISSING_BODY.goal");

		// Agent file should have 1 syntax error
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.NOVIABLEALTERNATIVE, this.syntaxerrors.get(0)
				.getType());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.RULE_MISSING_BODY, this.errors.get(0).getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_RULE_MISSING_CONDITION() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_RULE_MISSING_CONDITION.goal");

		// Agent file should have 1 syntax error
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.NOVIABLEALTERNATIVE, this.syntaxerrors.get(0)
				.getType());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.MSC_INVALID_OPERATOR, this.errors.get(0)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_RULE_VARIABLE_NOT_BOUND() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_RULE_VARIABLE_NOT_BOUND.goal");

		// Agent file should have 1 syntax error
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 1 error
		assertEquals(1, this.errors.size());

		assertEquals(AgentError.RULE_VARIABLE_NOT_BOUND, this.errors.get(0)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_SEND_INVALID_SELECTOR() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_SEND_INVALID_SELECTOR.goal");

		// Agent file should have no syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 6 errors
		assertEquals(4, this.errors.size());

		assertEquals(AgentError.SEND_INVALID_SELECTOR, this.errors.get(0)
				.getType());
		assertEquals(AgentError.SEND_INVALID_SELECTOR, this.errors.get(1)
				.getType());
		assertEquals(AgentError.SEND_INVALID_SELECTOR, this.errors.get(2)
				.getType());
		assertEquals(AgentError.SEND_INVALID_SELECTOR, this.errors.get(3)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_SELECTOR_VAR_NOT_BOUND() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_SELECTOR_VAR_NOT_BOUND.goal");

		// Agent file should have no syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// Agent file should produce 2 errors
		assertEquals(2, this.errors.size());

		assertEquals(AgentError.SELECTOR_VAR_NOT_BOUND, this.errors.get(0)
				.getType());
		assertEquals(AgentError.SELECTOR_VAR_NOT_BOUND, this.errors.get(1)
				.getType());

		// Agent file should produce no warnings
		assertTrue(this.warnings.isEmpty());
	}

}
