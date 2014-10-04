package languageTools.errors.mas;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.MAS2GLexer;
import languageTools.parser.MAS2GParser;

import org.antlr.v4.runtime.Token;

public class MASErrorStrategy extends MyErrorStrategy {

	@Override
	protected String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch(t.getType()) {
		case MAS2GParser.ID:
			return txt + " '" + t.getText() + "'";
		case MAS2GParser.FLOAT:
			return txt + " " + t.getText();
		case MAS2GParser.INT:
			return txt + " " + t.getText();
		default:
			return txt;
		}
	}
	
	@Override
	protected String prettyPrintToken(int type) {
		switch(type) {
		case Token.EOF:
			return "end of file";
		case MAS2GParser.ID:
			return "identifier";
		case MAS2GParser.FLOAT:
			return "floating point";
		case MAS2GParser.INT:
			return "integer";
		case MAS2GParser.StringLiteral:
			return "double-quoted string";
		case MAS2GParser.SingleQuotedStringLiteral:
			return "single-quoted string";
		case MAS2GParser.AGENTFILENAME:
			return "agent file name (preceded by ':')";
		default:
			// Do not improve, simply return token symbol as is
			return MAS2GParser.tokenNames[type];
		}
	}

	@Override
	protected String prettyPrintRuleContext(int ruleIndex) {
		switch(ruleIndex) {
		case MAS2GParser.RULE_initExpr:
			return "initialization expression";
		default:
			return MAS2GParser.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		SyntaxError type = null;
		
		switch(token.getType()) {
		case MAS2GLexer.StringLiteral:
			type = SyntaxError.UNTERMINATEDSTRINGLITERAL;
			break;
		case MAS2GLexer.SingleQuotedStringLiteral:
			type = SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
			break;
		}
		
		return type;
	}

}
