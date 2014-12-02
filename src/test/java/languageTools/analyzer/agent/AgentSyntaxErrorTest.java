package languageTools.analyzer.agent;

import static org.junit.Assert.*;
import goalhub.krTools.KRFactory;

import java.util.List;

import krTools.errors.exceptions.KRInitFailedException;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentWarning;
import languageTools.program.agent.AgentProgram;

import org.junit.Test;

public class AgentSyntaxErrorTest {
	
	List<Message> syntaxerrors;
	List<Message> errors;
	List<Message> warnings;
	AgentProgram program;
	
	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *  
	 * @param resource The GOAL agent file used in the test.
	 * @throws KRInitFailedException 
	 */
	private void setup(String resource) throws KRInitFailedException {
		AgentValidator validator = new AgentValidator(resource);
		validator.setKRInterface(KRFactory.getDefaultInterface());
		validator.validate();
		
		syntaxerrors = validator.getSyntaxErrors();
		errors = validator.getErrors();
		warnings = validator.getWarnings();
		program = validator.getProgram();
	}

	@Test
	public void test_IMPORT_NOT_A_MOD2G() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_IMPORT_NOT_A_MOD2G.goal");
		
		// Agent file should not produce any syntax errors
		assertEquals(1, syntaxerrors.size());
		
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors.get(0).getType());
				
		// Agent file should produce 1 error
		assertTrue(errors.isEmpty());
				
		//assertEquals(AgentError.IMPORT_NOT_A_MOD2G, errors.get(0).getType());
				
		// Agent file should produce no warnings
		assertTrue(warnings.isEmpty());
				
		// Program has 1 module and no imported files
		assertEquals(1, program.getModules().size());
		assertTrue(program.getImportedModules().isEmpty());
	}
	
	@Test
	public void test_MODULE_MISSING_NAME() throws KRInitFailedException {
		setup("src/test/resources/languageTools/analyzer/agent/test_MODULE_MISSING_NAME.goal");
		
		// Agent file should not produce any syntax errors
		assertEquals(1, syntaxerrors.size());
		
		assertEquals(SyntaxError.MISSINGTOKEN, syntaxerrors.get(0).getType());
				
		// Agent file should produce 1 error
		assertTrue(errors.isEmpty());
				
		// Agent file should produce no warnings
		assertEquals(1,warnings.size());
		
		assertEquals(AgentWarning.MODULE_NEVER_USED, warnings.get(0).getType());
				
		// Program has 1 module and no imported files
		assertEquals(2, program.getModules().size());
		assertEquals("<missing ID>", program.getModules().get(1).getName());
	}

}