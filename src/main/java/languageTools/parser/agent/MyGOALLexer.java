package languageTools.parser.agent;

import languageTools.parser.GOALLexer;
import languageTools.parser.MyLexer;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.Interval;

public class MyGOALLexer extends MyLexer<GOALLexer> {

	public MyGOALLexer(CharStream input, ANTLRErrorListener errorlistener) {
		super(input, errorlistener);
	}

	@Override
	protected GOALLexer getNewLexer(CharStream input) {
		return new GOALLexer(input) {
			@Override
			public void notifyListeners(LexerNoViableAltException e) {
				String text = this._input.getText(Interval.of(
						this._tokenStartCharIndex, this._input.index()));
				String msg = this.getErrorDisplay(text);

				ANTLRErrorListener listener = getErrorListenerDispatch();
				listener.syntaxError(this, null, this._tokenStartLine,
						this._tokenStartCharPositionInLine, msg, e);
			}
		};
	}
}
