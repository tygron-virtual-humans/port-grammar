package grammar;

// Import of ANTLR's runtime libraries
import org.antlr.v4.runtime.*;

/**
 * Simple tester class to verify that GOAL file parses correctly.
 * Slightly modified code from ANTLR 4 Reference.
 * Does <i>not</i> (yet) handle MAS2G files.
 * 
 * @author Koen Hindriks
 */
public class Tester {

	public static void main(String[] args) throws Exception {
		
		// Create a CharStream that reads from standard input.
		ANTLRInputStream input = new ANTLRInputStream(System.in);

		// Create a lexer that feeds off of input CharStream.
		GOALLexer lexer = new GOALLexer(input);
		
		// Create a buffer of tokens pulled from the lexer.
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		// Create a parser that feeds off the tokens buffer.
		GOALParser parser = new GOALParser(tokens);

		// Begin parsing at the rule for modules.
		parser.modules();
		
		if (parser.getNumberOfSyntaxErrors() == 0) {
			System.out.println("Parsing of file was successful.");
		}

	}

}
