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

import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.program.mas.MASProgram;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.mas.MASSymbol;

import org.junit.Test;

import eis.iilang.Parameter;
import goalhub.krTools.KRFactory;

public class MASSyntaxErrorTest {
	
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
	
	@Test
	public void test_INIT_UNRECOGNIZED_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_UNRECOGNIZED_KEY.mas2g");
		
		// MAS should produce 1 syntax error
		assertEquals(1, syntaxerrors.size());
		
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors.get(0).getType());
		
		// MAS should produce no errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce no warnings
		assertTrue(warnings.isEmpty());
		
		assertEquals(new File("src/test/resources/languageTools/analyzer/mas/dummy_environment.jar"),
				program.getEnvironmentfile());
		assertEquals(new HashMap<String, Parameter>(), program.getInitParameters());
	}

	@Test
	public void test_AGENTFILE_UNKNOWN_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENTFILE_UNKNOWN_KEY.mas2g");
		
		// MAS should produce a syntax error
		assertEquals(1, syntaxerrors.size());
		
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors.get(0).getType());

		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce no warnings
		assertEquals(0,warnings.size());

		// MAS program should have 1 agent file
		assertEquals(1, program.getAgentFiles().size());

		File file = new File("src/test/resources/languageTools/analyzer/mas/template.goal"); 
		assertEquals(file, ((MASSymbol)table.resolve("empty")).getFile());
		assertEquals(KRFactory.SWI_PROLOG, program.getKRInterface(file).getName());
	}
	
	@Test
	public void test_CONSTRAINT_UNKNOWN_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_CONSTRAINT_UNKNOWN_KEY.mas2g");

		// MAS should produce 1 syntax error
		assertEquals(1,syntaxerrors.size());
		
		assertEquals(SyntaxError.NOVIABLEALTERNATIVE, syntaxerrors.get(0).getType());
		
		// MAS should not produce any errors
		assertTrue(errors.isEmpty());
		
		// MAS should produce no warnings
		assertEquals(0,warnings.size());
		
		assertEquals("bot", program.getLaunchRules().get(0).getRequiredEntityName());
		assertEquals("required", program.getLaunchRules().get(0).getRequiredEntityType());
		assertEquals(1, program.getLaunchRules().get(0).getMaxNumberOfApplications());

		assertEquals(1, program.getLaunchRules().size());
	}
	
}
