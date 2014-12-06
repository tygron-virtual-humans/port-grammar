package languageTools.parser.agent;

import languageTools.parser.GOALLexer;
import languageTools.parser.MAS2GLexer;
import languageTools.parser.MyLexer;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
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

				ANTLRErrorListener listener = this.getErrorListenerDispatch();
				listener.syntaxError(this, null, this._tokenStartLine,
						this._tokenStartCharPositionInLine, msg, e);
			}
		};
	}

	@Override
	public Token emit() {
		Token result;
		ANTLRErrorListener listener = getErrorListenerDispatch();

		switch (getType()) {
		case MAS2GLexer.UnterminatedStringLiteral:
			// Pretend everything is back to normal now... (give user time to
			// take action ;-)
			setType(MAS2GLexer.StringLiteral);
			result = super.emit();

			// Report error
			listener.syntaxError(this, result, this._tokenStartLine,
					this._tokenStartCharPositionInLine, "", null);
			return result;
		case MAS2GLexer.UnterminatedSingelQuotedStringLiteral:
			// Pretend everything is back to normal now... (give user time to
			// take action ;-)
			setType(MAS2GLexer.SingleQuotedStringLiteral);
			result = super.emit();

			// Report error
			listener.syntaxError(this, result, this._tokenStartLine,
					this._tokenStartCharPositionInLine, "", null);
			return result;
		default:
			return super.emit();
		}
	}

}
