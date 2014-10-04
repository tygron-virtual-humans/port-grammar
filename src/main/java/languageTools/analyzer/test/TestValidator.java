/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package languageTools.analyzer.test;

import languageTools.analyzer.Validator;
import languageTools.errors.agent.AgentErrorStrategy;

import java.io.File;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

import languageTools.parser.InputStreamPosition;
import languageTools.parser.Test;
import languageTools.parser.Test.ActionContext;
import languageTools.parser.Test.ActionOperatorContext;
import languageTools.parser.Test.ActionSpecContext;
import languageTools.parser.Test.ActionSpecsContext;
import languageTools.parser.Test.ActionsContext;
import languageTools.parser.Test.AgentTestContext;
import languageTools.parser.Test.AgentTestsContext;
import languageTools.parser.Test.AssertTestContext;
import languageTools.parser.Test.BasicConditionContext;
import languageTools.parser.Test.BeliefsContext;
import languageTools.parser.Test.DeclarationContext;
import languageTools.parser.Test.DeclarationOrCallWithTermsContext;
import languageTools.parser.Test.DoActionsContext;
import languageTools.parser.Test.EvaluateInContext;
import languageTools.parser.Test.GoalsContext;
import languageTools.parser.Test.KnowledgeContext;
import languageTools.parser.Test.MacroDefContext;
import languageTools.parser.Test.MasFileContext;
import languageTools.parser.Test.MentalAtomContext;
import languageTools.parser.Test.MentalOperatorContext;
import languageTools.parser.Test.MentalStateConditionContext;
import languageTools.parser.Test.ModuleContext;
import languageTools.parser.Test.ModuleDefContext;
import languageTools.parser.Test.ModuleImportContext;
import languageTools.parser.Test.ModuleOptionContext;
import languageTools.parser.Test.ModulesContext;
import languageTools.parser.Test.NestedRulesContext;
import languageTools.parser.Test.PostconditionContext;
import languageTools.parser.Test.PreconditionContext;
import languageTools.parser.Test.ProgramContext;
import languageTools.parser.Test.ProgramRuleContext;
import languageTools.parser.Test.RuleEvaluationOrderContext;
import languageTools.parser.Test.SelectorContext;
import languageTools.parser.Test.TestConditionContext;
import languageTools.parser.Test.TestContext;
import languageTools.parser.Test.TestSectionContext;
import languageTools.parser.Test.TimeoutContext;
import languageTools.parser.Test.UnitTestContext;
import languageTools.parser.agent.MyGOALLexer;
import languageTools.parser.TestVisitor;
import languageTools.program.test.AgentTest;

/**
 * Validates a test file and constructs a test program. 
 * 
 * @author Koen Hindriks
 */
@SuppressWarnings("rawtypes")
public class TestValidator extends Validator<MyGOALLexer, Test, AgentErrorStrategy, AgentTest> implements TestVisitor {
	
	private Test parser;
	private static AgentErrorStrategy strategy = null;
	
	/**
	 * Creates the test validator.
	 * 
	 * @param source
	 */
	public TestValidator(String filename) {
		super(filename);
	}
	
	@Override
	protected MyGOALLexer getNewLexer(CharStream stream, ANTLRErrorListener errorlistener) {
		return new MyGOALLexer(stream, errorlistener);
	}

	@Override
	protected Test getNewParser(TokenStream stream) {
		return new Test(stream);
	}

	@Override
	protected ParseTree startParser() {
		return parser.unitTest();
	}

	@Override
	protected AgentErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new AgentErrorStrategy();
		}
		return strategy;
	}

	@Override
	protected AgentTest getNewProgram(File file) {
		return new AgentTest(new InputStreamPosition(0, 0, 0, 0, file));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation calls {@link ParseTree#accept} on the
	 * specified tree.</p>
	 */
	@SuppressWarnings("unchecked")
	public Void visit(@NotNull ParseTree tree) {
		tree.accept(this);
		
		return null; // Java says must return something even when Void
	}

	@Override
	public Object visitSelector(SelectorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBeliefs(BeliefsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTest(TestContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMentalOperator(MentalOperatorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNestedRules(NestedRulesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object visitActionSpecs(ActionSpecsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitRuleEvaluationOrder(RuleEvaluationOrderContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitModuleImport(ModuleImportContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPostcondition(PostconditionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAgentTest(AgentTestContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTestCondition(TestConditionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMentalAtom(MentalAtomContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMacroDef(MacroDefContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitActionOperator(ActionOperatorContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPrecondition(PreconditionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitEvaluateIn(EvaluateInContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitProgram(ProgramContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitKnowledge(KnowledgeContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTestSection(TestSectionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssertTest(AssertTestContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMasFile(MasFileContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitModule(ModuleContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAgentTests(AgentTestsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitGoals(GoalsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnitTest(UnitTestContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitActionSpec(ActionSpecContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitModules(ModulesContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitDoActions(DoActionsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTimeout(TimeoutContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitActions(ActionsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitModuleDef(ModuleDefContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAction(ActionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBasicCondition(BasicConditionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitProgramRule(ProgramRuleContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitMentalStateCondition(MentalStateConditionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitModuleOption(ModuleOptionContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void secondPass(ParseTree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object visitDeclarationOrCallWithTerms(
			DeclarationOrCallWithTermsContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitDeclaration(DeclarationContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

}
