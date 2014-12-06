package languageTools.parser;

import languageTools.analyzer.Validator;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;

/**
 * Redirects {@link LexerNoViableAltException}s to {@link Validator}.
 *
 * See: {@link Validator#syntaxError()} and {@link Validator#parseFile()}.
 *
 * @author Koen Hindriks
 *
 * @param <L>
 */
public abstract class MyLexer<L extends Lexer> extends Lexer {

	private final L lexer;

	public MyLexer(CharStream input, ANTLRErrorListener errorlistener) {
		super(input);
		this.lexer = getNewLexer(input);
		this.lexer.removeErrorListeners();
		this.lexer.addErrorListener(errorlistener);
	}

	protected abstract L getNewLexer(CharStream input);

	@Override
	public Token nextToken() {
		return this.lexer.nextToken();
	}

	// @Override public void notifyListeners(LexerNoViableAltException e) in
	// implementation of this class.

	@Override
	public String[] getRuleNames() {
		return this.lexer.getRuleNames();
	}

	@Override
	public String getGrammarFileName() {
		return this.lexer.getGrammarFileName();
	}

	@Override
	public ATN getATN() {
		return this.lexer.getATN();
	}

}
