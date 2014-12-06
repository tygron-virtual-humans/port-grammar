package languageTools.errors.agent;

import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.parser.GOAL;
import languageTools.parser.GOALLexer;

import org.antlr.v4.runtime.Token;

public class AgentErrorStrategy extends MyErrorStrategy {

	@Override
	protected String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case GOAL.ID:
			return txt + " '" + t.getText() + "'";
		case GOAL.VAR:
			return txt + " '" + t.getText() + "'";
		case GOAL.ERROR:
			return txt + " '" + t.getText() + "'";
		case GOAL.KR_BLOCK:

		default:
			return txt;
		}
	}

	@Override
	protected String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file";
		case GOAL.ID:
			return "identifier";
		case GOAL.StringLiteral:
			return "double-quoted string";
		case GOAL.SingleQuotedStringLiteral:
			return "single-quoted string";
		case GOAL.VAR:
			return "parameter";
		case GOAL.ERROR:
			return "";
		case GOAL.KR_BLOCK:
			return "KR expression";
		case GOAL.LINE_COMMENT:
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
		case GOAL.RULE_actionOperator:
			return "an action name";
		case GOAL.RULE_beliefs:
			return "belief section";
		case GOAL.RULE_knowledge:
			return "knowledge section";
		case GOAL.RULE_mentalStateCondition:
			return "a mental state condition";
		case GOAL.RULE_moduleDef:
			return "module declaration";
		case GOAL.RULE_programRule:
			return "rule";
		case GOAL.RULE_ruleEvaluationOrder:
			return "a rule evaluation order option";
		case GOAL.RULE_program:
			return "a section with rules and macros";
		default:
			return GOAL.ruleNames[ruleIndex];
		}
	}

	@Override
	public SyntaxError getLexerErrorType(Token token) {
		SyntaxError type = null;

		switch (token.getType()) {
		case GOALLexer.StringLiteral:
			type = SyntaxError.UNTERMINATEDSTRINGLITERAL;
			break;
		case GOALLexer.SingleQuotedStringLiteral:
			type = SyntaxError.UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL;
			break;
		}

		return type;
	}

}
