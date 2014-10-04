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
	
	private L lexer;

	public MyLexer(CharStream input, ANTLRErrorListener errorlistener) {
		super(input);
		lexer = getNewLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(errorlistener);
	}
	
	protected abstract L getNewLexer(CharStream input);
	
	@Override
	public Token nextToken() {
		return lexer.nextToken();
	}
	
//	@Override public void notifyListeners(LexerNoViableAltException e) in implementation of this class.

	@Override
	public String[] getRuleNames() {
		return lexer.getRuleNames();
	}

	@Override
	public String getGrammarFileName() {
		return lexer.getGrammarFileName();
	}

	@Override
	public ATN getATN() {
		return lexer.getATN();
	}

}
