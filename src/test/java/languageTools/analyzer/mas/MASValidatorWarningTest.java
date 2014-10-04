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

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import languageTools.errors.mas.MASWarning;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.program.mas.MASProgram;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.mas.MASSymbol;

import org.junit.Test;

import eis.iilang.Parameter;
import goalhub.krTools.KRFactory;

public class MASValidatorWarningTest {
	
	List<Message> syntaxerrors;
	List<Message> errors;
	List<Message> warnings;
	SymbolTable table;
	MASProgram program;
	
	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *  
	 * @param resource The MAS file used in the test.
	 */
	private void setup(String resource) {
		MASValidator validator = new MASValidator(resource);
		validator.validate();
		
		syntaxerrors = validator.getSyntaxErrors();
		errors = validator.getErrors();
		warnings = validator.getWarnings();
		table = validator.getSymbolTable();
		program = validator.getProgram();
	}

	/**
	 * Covers warnings AGENTFILES_NO_AGENTS and LAUNCH_NO_RULES.
	 */
	@Test
	public void testMAStemplate() {
		setup("src/test/resources/languageTools/analyzer/mas/template.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS template should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS template should produce 2 warnings
		assertEquals(2,warnings.size());
		
		assertEquals(MASWarning.AGENTFILES_NO_AGENTS, warnings.get(0).getType());
		assertEquals(MASWarning.LAUNCH_NO_RULES, warnings.get(1).getType());
	}
	
	@Test
	public void test_ENVIRONMENT_NO_REFERENCE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_ENVIRONMENT_NO_REFERENCE.mas2g");
		
		// MAS should not produce any syntax errors
		assertEquals(1,syntaxerrors.size());
		
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors.get(0).getType());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.ENVIRONMENT_NO_REFERENCE, warnings.get(0).getType());
		
		assertEquals(null, program.getEnvironmentfile());
		assertEquals(new HashMap<String, Parameter>(), program.getInitParameters());
	}
	
	@Test
	public void test_ENVIRONMENT_NO_REFERENCE_2() {
		setup("src/test/resources/languageTools/analyzer/mas/test_ENVIRONMENT_NO_REFERENCE_2.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any validation errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.ENVIRONMENT_NO_REFERENCE, warnings.get(0).getType());
		
		assertEquals(null, program.getEnvironmentfile());
		assertEquals(new HashMap<String, Parameter>(), program.getInitParameters());
	}
	
	@Test
	public void test_INIT_DUPLICATE_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_DUPLICATE_KEY.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.INIT_DUPLICATE_KEY, warnings.get(0).getType());
		
		assertEquals(new File("src/test/resources/languageTools/analyzer/mas/dummy_environment.jar"),
				program.getEnvironmentfile());
		assertEquals("value1", ((eis.iilang.Identifier) program.getInitParameters().get("key")).getValue());
	}
	
	@Test
	public void test_AGENTFILES_DUPLICATE_NAME() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILES_DUPLICATE_NAME.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILES_DUPLICATE_NAME, warnings.get(0).getType());

		// MAS program should have only 1 agent file
		assertEquals(1, program.getAgentFiles().size());
	}
	
	@Test
	public void test_AGENTFILES_DUPLICATE_NAME_2() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILES_DUPLICATE_NAME_2.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILES_DUPLICATE_NAME, warnings.get(0).getType());

		// MAS program should have only 1 agent file
		assertEquals(1, program.getAgentFiles().size());
	}
	
	@Test
	public void test_SAME_AGENTFILE_ALIASED_TWICE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_SAME_AGENTFILE_ALIASED_TWICE.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce no warnings
		assertTrue(warnings.isEmpty());

		// MAS program should have only 1 agent file
		assertEquals(1, program.getAgentFiles().size());
		
		// MAS program should have 2 launch rules
		assertEquals(2, program.getLaunchRules().size());
	}
	
	@Test
	public void test_AGENTFILE_DUPLICATE_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_DUPLICATE_KEY.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILE_DUPLICATE_KEY, warnings.get(0).getType());

		// MAS program should have 1 agent file
		assertEquals(1, program.getAgentFiles().size());

		File file = new File("src/test/resources/languageTools/analyzer/mas/template.goal"); 
		assertEquals(file, ((MASSymbol)table.resolve("empty1")).getFile());
		assertEquals(KRFactory.SWI_PROLOG, program.getKRInterface(file).getName());
	}
	
	@Test
	public void test_AGENTFILE_UNUSED() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_UNUSED.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILE_UNUSED, warnings.get(0).getType());

		// MAS program should have 2 agent files
		assertEquals(2, program.getAgentFiles().size());
	}
	
	@Test
	public void test_AGENTFILE_NONEXISTANT_REFERENCE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_NONEXISTANT_REFERENCE.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILE_NONEXISTANT_REFERENCE, warnings.get(0).getType());

		assertEquals(1, program.getLaunchRules().size());
	}
	
	/**
	 * This test also demonstrates that the 'name' parameter blocks access to the agent file name itself;
	 * in other words, a launch rule can only reference an agent file using the alias defined by the 'name'
	 * parameter.
	 */
	@Test
	public void test_AGENTFILE_NONEXISTANT_REFERENCE_2() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_NONEXISTANT_REFERENCE_2.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.AGENTFILE_NONEXISTANT_REFERENCE, warnings.get(0).getType());

		assertEquals(1, program.getLaunchRules().size());
	}
	
	@Test
	public void test_LAUNCH_INVALID_WILDCARD() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_INVALID_WILDCARD.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.LAUNCH_INVALID_WILDCARD, warnings.get(0).getType());

		assertEquals(1, program.getLaunchRules().size());
	}
	
	@Test
	public void test_CONSTRAINT_DUPLICATE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_CONSTRAINT_DUPLICATE.mas2g");
		
		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.CONSTRAINT_DUPLICATE, warnings.get(0).getType());
		
		assertEquals("bot", program.getLaunchRules().get(0).getRequiredEntityName());
		assertEquals("required", program.getLaunchRules().get(0).getRequiredEntityType());
		assertEquals(1, program.getLaunchRules().get(0).getMaxNumberOfApplications());

		assertEquals(1, program.getLaunchRules().size());
	}

	@Test
	public void test_LAUNCH_NO_CONDITIONAL_RULES() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_NO_CONDITIONAL_RULES.mas2g");
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce 1 warning
		assertEquals(1,warnings.size());
		
		assertEquals(MASWarning.LAUNCH_NO_CONDITIONAL_RULES, warnings.get(0).getType());

		assertEquals(1, program.getLaunchRules().size());
	}

}
