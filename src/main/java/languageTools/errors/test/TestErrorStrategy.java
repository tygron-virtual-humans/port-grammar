package languageTools.errors.test;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.GOAL;
import languageTools.parser.GOALLexer;
import languageTools.parser.Test;

import org.antlr.v4.runtime.Token;

public class TestErrorStrategy extends MyErrorStrategy {
	@Override
	protected String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case Test.ID:
			return txt + " '" + t.getText() + "'";
		case Test.VAR:
			return txt + " '" + t.getText() + "'";
		case Test.ERROR:
			return txt + " '" + t.getText() + "'";
		default:
			return txt;
		}
	}

	@Override
	protected String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file";
		case Test.ID:
			return "identifier";
		case Test.StringLiteral:
			return "double-quoted string";
		case Test.SingleQuotedStringLiteral:
			return "single-quoted string";
		case Test.VAR:
			return "parameter";
		case Test.ERROR:
			return "";
		case Test.KR_BLOCK:
			return "KR expression";
		case Test.LINE_COMMENT:
			return "line comment";
		default:
			// Do not improve, simply return token symbol as is
			if (type < GOAL.tokenNames.length) {
				return GOAL.tokenNames[type];
			} else {
				return "something that does not make sense";
			}
		}
	}

	@Override
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		case Test.RULE_actionOperator:
			return "an action name";
		case Test.RULE_beliefs:
			return "belief section";
		case Test.RULE_knowledge:
			return "knowledge section";
		case Test.RULE_mentalStateCondition:
			return "a mental state condition";
		case Test.RULE_moduleDef:
			return "module declaration";
		case Test.RULE_programRule:
			return "rule";
		case Test.RULE_ruleEvaluationOrder:
			return "a rule evaluation order option";
		case Test.RULE_program:
			return "a section with rules and macros";
		default:
			return Test.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		switch (token.getType()) {
		case GOALLexer.StringLiteral:
			return SyntaxError.UNTERMINATEDSTRINGLITERAL;
		case GOALLexer.SingleQuotedStringLiteral:
			return SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
		default:
			return null;
		}
	}
}
