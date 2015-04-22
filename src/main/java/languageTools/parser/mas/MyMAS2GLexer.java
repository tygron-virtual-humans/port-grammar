package languageTools.parser.mas;

import languageTools.parser.MAS2GLexer;
import languageTools.parser.MyLexer;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.Interval;

public class MyMAS2GLexer extends MyLexer<MAS2GLexer> {

	public MyMAS2GLexer(CharStream input, ANTLRErrorListener errorlistener) {
		super(input, errorlistener);
	}

	@Override
	protected MAS2GLexer getNewLexer(CharStream input) {
		return new MAS2GLexer(input) {
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
