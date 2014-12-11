package languageTools.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import krTools.parser.SourceInfo;
import languageTools.errors.Message;
import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.ValidatorError;
import languageTools.errors.ValidatorError.ValidatorErrorType;
import languageTools.errors.ValidatorWarning;
import languageTools.errors.ValidatorWarning.ValidatorWarningType;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.MyLexer;
import languageTools.program.Program;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;

/**
 * A validator parses and validates a program file, i.e., checks for both
 * syntactic and semantic issues. During the validation process a
 * {@link Program} is created that can be obtained by {@link #getProgram()}
 * after calling {@link #validate()}.
 *
 * {@link #validate()} may generate {@link ParserError}s if the parser detected
 * problems and {@link ValidatorError}s if the program specific validator
 * detected problems. It may also generate {@link ValidatorWarning}s. Upon
 * completion, {@link #validate()} sets a flag in the program which indicates
 * whether the program is valid or not.
 */
public abstract class Validator<L extends MyLexer<?>, P extends Parser, E extends MyErrorStrategy, Q extends Program>
		implements ANTLRErrorListener {

	/**
	 * Name of the file that is validated.
	 */
	private final String filename;
	protected final File source;

	private Q program;

	/**
	 * Lexer generated tokens.
	 */
	CommonTokenStream tokens;

	private List<Message> syntaxErrors = new ArrayList<Message>();
	private List<Message> errors = new ArrayList<Message>();
	private List<Message> warnings = new ArrayList<Message>();

	/**
	 * Creates the validator.
	 *
	 * @param filename
	 *            The name of the file to be validated.
	 */
	public Validator(String filename) {
		this.filename = filename;
		this.source = new File(filename);
	}

	protected abstract L getNewLexer(CharStream stream,
			ANTLRErrorListener errorlistener);

	protected abstract P getNewParser(TokenStream stream);

	/**
	 * Starts parser at a specific grammar rule.
	 *
	 * @return parse tree
	 */
	protected abstract ParseTree startParser();

	/**
	 * Gets the error strategy.
	 *
	 * A validator should need only one instance of this.
	 *
	 * @return The error strategy used by this {@link #Validator(String)}.
	 */
	protected abstract E getTheErrorStrategy();

	protected abstract Q getNewProgram(File file);

	/**
	 * @return Name of the file that is validated.
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return The program that was constructed during validation.
	 */
	public Q getProgram() {
		return this.program;
	}

	/**
	 * Parses the file to be validated.
	 */
	/**
	 * Parses the file.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException
	 *             If the file does not exist.
	 */
	public ParseTree parseFile() throws IOException {
		ANTLRFileStream stream = new ANTLRFileStream(getFilename());

		// Create a lexer that feeds off of input CharStream (also redirects
		// error listener).
		L lexer = getNewLexer(stream, this);

		// Create a buffer of tokens pulled from the lexer.
		this.tokens = new CommonTokenStream(lexer);

		// generatedTokens = lexer.getAllTokens();

		// Create a parser that feeds off the tokens buffer.
		P parser = getNewParser(this.tokens);
		// Use custom error reporting
		parser.setErrorHandler(getTheErrorStrategy());
		// Redirect error output
		parser.removeErrorListeners();
		parser.addErrorListener(this);
		// Parse file
		parser.setBuildParseTree(true);
		return startParser();
	}

	/**
	 * Builds a symbol table and validates file.
	 *
	 * Each time you call this a new lexer, parser and program are created.
	 */
	public void validate() {
		try {
			// Prepare by parsing the file.
			ParseTree tree = null;
			try {
				tree = parseFile();
			} catch (IOException e) {
				reportError(SyntaxError.FILE_COULDNOT_OPEN, null, getFilename());
				return;
			}

			// Initialize program; file existence is checked above
			this.program = getNewProgram(new File(getFilename()));

			// Build and validate program
			firstPass(tree);
			secondPass(tree);

			// Set validity flag; a program is valid if it did not generate any
			// parsing or validation errors
			this.program.setValid(getSyntaxErrors().isEmpty()
					&& getErrors().isEmpty());
		} catch (Exception e) {
			// Convert stack trace to string
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			reportError(SyntaxError.FATAL, null,
					e.getMessage() + "\n" + sw.toString());
		}
	}

	/**
	 * First pass over parsed program.
	 *
	 * @param tree
	 *            Parse tree of program.
	 */
	private void firstPass(ParseTree tree) {
		this.visit(tree);
	}

	/**
	 * Second pass over parsed program.
	 *
	 * @param tree
	 *            Parse tree of program.
	 */
	protected abstract void secondPass(ParseTree tree);

	/**
	 * Reports the results of the validation, listing all syntax and validation
	 * errors and warnings.
	 */
	public String report() {
		// Sort errors on line and position numbers
		this.syntaxErrors = sort(this.syntaxErrors);
		this.errors = sort(this.errors);
		this.warnings = sort(this.warnings);

		StringBuilder report = new StringBuilder();
		// Report parsing errors
		report.append("\n");
		report.append("-----------------------------------------------------------------\n");
		report.append(" PARSING REPORT: ");
		if (getSyntaxErrors().size() == 0) {
			report.append("Parsing of file was successful.\n");
		} else {
			report.append("Found " + getSyntaxErrors().size()
					+ " parsing error(s).\n");
		}
		report.append(" File parsed: " + getFilename() + "\n");
		report.append("-----------------------------------------------------------------\n");
		for (Message error : getSyntaxErrors()) {
			report.append(error + "\n");
		}
		report.append("\n");
		// Report validation errors
		report.append("-----------------------------------------------------------------\n");
		report.append(" VALIDATOR REPORT: ");
		report.append("Found " + getErrors().size() + " error(s) and "
				+ getWarnings().size() + " warning(s).\n");
		report.append("-----------------------------------------------------------------\n");
		for (Message error : getErrors()) {
			report.append(error + "\n");
		}
		report.append("\n");
		for (Message warning : getWarnings()) {
			report.append(warning + "\n");
		}
		report.append("-----------------------------------------------------------------\n");

		return report.toString();
	}

	/**
	 * Dumps all tokens to console.
	 */
	public void printLexerTokens() {
		for (Token token : this.tokens.getTokens()) {
			System.out.print("'" + token.getText() + "<" + token.getType()
					+ ">' ");
		}
	}

	/**
	 * @return The list of semantic (validation) errors found during validation.
	 */
	public List<Message> getSyntaxErrors() {
		return this.syntaxErrors;
	}

	/**
	 * Report syntax error.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR ParserRuleContext object.
	 *
	 * @param error
	 *            The type of syntax error that is added.
	 * @param info
	 *            Source info object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	protected boolean reportError(SyntaxError type, SourceInfo info,
			String... args) {
		return this.syntaxErrors.add(new ParserError(type, info, args));
	}

	/**
	 * Report syntax error.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR ParserRuleContext object.
	 *
	 * @param error
	 *            The type of syntax error that is added.
	 * @param pos
	 *            The input stream position where the error was detected.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	protected boolean reportError(SyntaxError type, InputStreamPosition pos,
			String... args) {
		return this.syntaxErrors.add(new ParserError(type, pos, args));
	}

	/**
	 * @return The list of semantic (validation) errors found during validation.
	 */
	public List<Message> getErrors() {
		return this.errors;
	}

	/**
	 * Report (validation) error.
	 *
	 * @param error
	 *            The semantic (validation) error that is added.
	 * @param info
	 *            A source info object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	public boolean reportError(ValidatorErrorType type, SourceInfo info,
			String... args) {
		return this.errors.add(new ValidatorError(type, info, args));
	}

	/**
	 * Report (validation) error.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR ParserRuleContext object.
	 *
	 * @param error
	 *            The semantic (validation) error that is added.
	 * @param context
	 *            The ANTLR ParserRuleContext object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	public boolean reportError(ValidatorErrorType type,
			ParserRuleContext context, String... args) {
		return this.errors.add(new ValidatorError(type, getSourceInfo(context),
				args));
	}

	/**
	 * Report (validation) error.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR TerminalNode object.
	 *
	 * @param error
	 *            The semantic (validation) error that is added.
	 * @param node
	 *            An ANTLR TerminalNode object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	protected boolean reportError(ValidatorErrorType type, TerminalNode node,
			String... args) {
		return this.errors.add(new ValidatorError(type, getSourceInfo(node),
				args));
	}

	/**
	 * @param ctx
	 *            An ANTLR {@link ParserRuleContext}.
	 * @return A source info object ({@link InputStreamPosition}) with
	 *         information extracted from the rule context.
	 */
	public SourceInfo getSourceInfo(ParserRuleContext ctx) {
		InputStreamPosition pos = new InputStreamPosition(ctx.getStart(),
				ctx.getStop() == null ? ctx.getStart() : ctx.getStop(),
				this.source);
		return pos;
	}

	/**
	 * @param node
	 *            An ANTLR {@link TerminalNode}.
	 * @return A source info object ({@link InputStreamPosition}) with
	 *         information extracted from the terminal node.
	 */
	public SourceInfo getSourceInfo(TerminalNode node) {
		return new InputStreamPosition(node.getSymbol(), node.getSymbol(),
				this.source);
	}

	/**
	 * @return The list of warnings found during validation.
	 */
	public List<Message> getWarnings() {
		return this.warnings;
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR ParserRuleContext object.
	 *
	 * @param warning
	 *            The warning that is added.
	 * @param info
	 *            A source info object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	public boolean reportWarning(ValidatorWarningType type, SourceInfo info,
			String... args) {
		return this.warnings.add(new ValidatorWarning(type, info, args));
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR ParserRuleContext object.
	 *
	 * @param warning
	 *            The warning that is added.
	 * @param context
	 *            The ANTLR ParserRuleContext object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	public boolean reportWarning(ValidatorWarningType type,
			ParserRuleContext context, String... args) {
		return this.warnings.add(new ValidatorWarning(type,
				getSourceInfo(context), args));
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an
	 * ANTLR TerminalNode object.
	 *
	 * @param warning
	 *            The warning that is added.
	 * @param node
	 *            The ANTLR TerminalNode object.
	 * @param args
	 *            Additional info to be inserted into warning message.
	 */
	protected boolean reportWarning(ValidatorWarningType type,
			TerminalNode node, String... args) {
		return this.warnings.add(new ValidatorWarning(type,
				getSourceInfo(node), args));
	}

	// -------------------------------------------------------------
	// Syntax error handling (implements ANTLRErrorListener)
	// -------------------------------------------------------------

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		int start = recognizer.getInputStream().index();
		int stop = start;
		if (offendingSymbol != null) {
			CommonToken token = (CommonToken) offendingSymbol;
			start = token.getStartIndex();
			stop = token.getStopIndex();
		}
		InputStreamPosition pos = new InputStreamPosition(line,
				charPositionInLine, start, stop, this.source);

		if (recognizer instanceof Lexer) { // lexer error
			handleLexerError(recognizer, offendingSymbol, pos, msg, e);
		} else {
			handleParserError(recognizer, offendingSymbol, pos, msg, e);
		}
	}

	/**
	 * Adds new error for token recognition problem (lexer).
	 *
	 * @param pos
	 *            input stream position
	 * @param stop
	 *            stopIndex of last recognition error
	 * @param text
	 *            character(s) that could not be recognized
	 */
	public void handleLexerError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, InputStreamPosition pos, String text,
			RecognitionException e) {
		// Determine type of syntax error
		SyntaxError type = null;
		if (offendingSymbol instanceof Token) {
			type = getTheErrorStrategy().getLexerErrorType(
					(Token) offendingSymbol);
		} else { // if nothing else, by default, assume token recognition
			// problem
			type = SyntaxError.TOKENRECOGNITIONERROR;
		}

		switch (type) {
		case TOKENRECOGNITIONERROR:
			// Check if this and last error were both token recognition errors;
			// if so, merge them
			int last = this.syntaxErrors.size() - 1;
			if (last >= 0 && this.syntaxErrors.get(last).getType().equals(type)) {
				Message error = this.syntaxErrors.get(last);
				// Use old input stream position, but first get new stop index
				int stop = pos.getStopIndex();
				pos = (InputStreamPosition) error.getSource();
				pos.setStopIndex(stop);
				// Concatenate symbols that were not recognized
				text = error.getArguments()[0] + text;
				// Remove previous error
				this.syntaxErrors.remove(last);
			}
			break;
		case UNTERMINATEDSTRINGLITERAL:
		case UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL:
			// nothing to do
			break;
		default:
			throw new UnsupportedOperationException("Unexpected lexer error: "
					+ text, e);
		}

		// Remove tabs and newlines
		text = removeTabsAndNewLines(text);

		this.syntaxErrors.add(new ParserError(type, pos, text));
	}

	/**
	 * Adds error for parsing problem.
	 *
	 * <p>
	 * Simply pushes parser error msg forward. See {@link #MASErrorStrateg} for
	 * handling of parsing errors.
	 * </p>
	 *
	 * @param pos
	 *            input stream position
	 * @param msg
	 *            reported parser error msg
	 */
	public void handleParserError(Recognizer<?, ?> recognizer,
			Object offendingSymbol, InputStreamPosition pos,
			String expectedtokens, RecognitionException e) {
		// We need the strategy to get access to our customized token displays
		MyErrorStrategy strategy = (MyErrorStrategy) ((Parser) recognizer)
				.getErrorHandler();

		// Report the various types of syntax errors
		SyntaxError type = null;
		String offendingTokenText = strategy
				.getTokenErrorDisplay((Token) offendingSymbol);
		if (e.getMessage().equals("NoViableAlternative")) {
			type = SyntaxError.NOVIABLEALTERNATIVE;
			reportError(type, pos, offendingTokenText, expectedtokens);
		}
		if (e.getMessage().equals("InputMismatch")) {
			type = SyntaxError.INPUTMISMATCH;
			reportError(type, pos, offendingTokenText, expectedtokens);
		}
		if (e.getMessage().equals("FailedPredicate")) {
			type = SyntaxError.FAILEDPREDICATE;
			reportError(type, pos, offendingTokenText, expectedtokens);
		}
		if (e.getMessage().equals("UnwantedToken")) {
			type = SyntaxError.UNWANTEDTOKEN;
			reportError(type, pos, offendingTokenText);
		}
		if (e.getMessage().equals("MissingToken")) {
			type = SyntaxError.MISSINGTOKEN;
			reportError(type, pos, expectedtokens);
		}
		if (type == null) {
			type = SyntaxError.UNEXPECTEDINPUT;
			reportError(type, pos, offendingTokenText, expectedtokens);
		}
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex,
			int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
		System.out
				.println("Found ambiguous readings of file! Please report this finding and send us this file.");
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, BitSet conflictingAlts,
			ATNConfigSet configs) {

	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa,
			int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

	}

	/**
	 * {@inheritDoc}
	 */
	public abstract Void visit(@NotNull ParseTree tree);

	/**
	 * {@inheritDoc}
	 */
	public Void visitChildren(@NotNull RuleNode node) {
		return null;
	};

	/**
	 * {@inheritDoc}
	 */
	public Void visitTerminal(@NotNull TerminalNode node) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Void visitErrorNode(@NotNull ErrorNode node) {
		return null;
	}

	// -------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------

	/**
	 * Appends path of source file to filename; files should be located relative
	 * to source file.
	 *
	 * @param filename
	 *            The name of a file.
	 * @return The filename with path to source file appended.
	 */
	protected String getPathRelativeToSourceFile(String filename) {
		return FilenameUtils.getFullPath(getFilename()).concat(filename);
	}

	/**
	 * Sorts a list of messages on line and position numbers.
	 *
	 * @param messages
	 *            A list of messages.
	 */
	private List<Message> sort(List<Message> messages) {
		List<Message> sorted = new ArrayList<>();
		Iterator<Message> msgIterator = messages.iterator();
		Message msg;
		int i;

		// Add elements in right place
		while (msgIterator.hasNext()) {
			msg = msgIterator.next();
			i = 0;
			// add messages without source info to the front of the list
			if (msg.getSource() != null) {
				// skip messages in list without source info
				while (i < sorted.size() && sorted.get(i).getSource() == null) {
					i++;
				}
				while (i < sorted.size()
						&& before(sorted.get(i).getSource(), msg.getSource())) {
					i++;
				}
			}
			sorted.add(i, msg);
		}

		return sorted;
	}

	/**
	 * @param info1
	 *            A source info object.
	 * @param info2
	 *            A source info object.
	 * @return {@code true} if source position of info1 object occurs before
	 *         position of info2 object.
	 */
	private boolean before(SourceInfo info1, SourceInfo info2) {
		boolean source = (info1.getSource().getName()
				.compareTo(info2.getSource().getName()) < 0);
		boolean sourceEqual = (info1.getSource().getName()
				.compareTo(info2.getSource().getName()) == 0);
		boolean lineNr = sourceEqual
				&& (info1.getLineNumber() < info2.getLineNumber());
		boolean lineNrEqual = (info1.getLineNumber() == info2.getLineNumber());
		boolean position = sourceEqual
				&& lineNrEqual
				&& (info1.getCharacterPosition() < info2.getCharacterPosition());

		return source || lineNr || position;
	}

	/**
	 * @param set
	 *            Of items to pretty print as list
	 * @return string with comma separated list of set items, or plain single
	 *         item, or empty string
	 */
	public String prettyPrintSet(Set<?> set) {
		StringBuilder str = new StringBuilder();
		Iterator<?> setIterator = set.iterator();
		if (setIterator.hasNext()) {
			str.append(setIterator.next());
		}
		while (setIterator.hasNext()) {
			String next = setIterator.next().toString();
			str.append((setIterator.hasNext() ? ", " : " and "));
			str.append(next);
		}

		return str.toString();
	}

	/**
	 * Removes leading and trailing characters from a string.
	 *
	 * @param quoted
	 *            A string with quotes.
	 * @return The string without quotes.
	 */
	protected String removeLeadTrailCharacters(String quoted) {
		return quoted.substring(1, quoted.length() - 1);
	}

	/**
	 * Removes tabs, newlines, etc. from string.
	 *
	 * @param text
	 *            Input string
	 * @return Output string without tabs, etc.
	 */
	private String removeTabsAndNewLines(String text) {
		text = text.replace("\\r", "").replace("\\n", " ").replace("\\t", " ")
				.replace("\\f", "");
		return text;
	}

	/**
	 * Turns a list terminal nodes into a single string.
	 *
	 * @param nodes
	 *            List of terminal nodes.
	 * @return A string representing the string content of the nodes.
	 */
	protected String implode(List<TerminalNode> nodes) {
		StringBuilder builder = new StringBuilder();
		for (TerminalNode character : nodes) {
			if (character != null) {
				builder.append(character.getText());
			}
		}
		return builder.toString();
	}

}
