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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.language.Term;
import languageTools.analyzer.Validator;
import languageTools.analyzer.agent.AgentValidator;
import languageTools.analyzer.mas.MASValidator;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.test.TestError;
import languageTools.errors.test.TestErrorStrategy;
import languageTools.parser.GOAL;
import languageTools.parser.GOALLexer;
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
import languageTools.parser.Test.DoTestContext;
import languageTools.parser.Test.EvaluateInContext;
import languageTools.parser.Test.GoalsContext;
import languageTools.parser.Test.KnowledgeContext;
import languageTools.parser.Test.KrImportContext;
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
import languageTools.parser.Test.ReactTestContext;
import languageTools.parser.Test.RuleEvaluationOrderContext;
import languageTools.parser.Test.SelectorContext;
import languageTools.parser.Test.TestBoundaryContext;
import languageTools.parser.Test.TestConditionContext;
import languageTools.parser.Test.TestConditionPartContext;
import languageTools.parser.Test.TestContext;
import languageTools.parser.Test.TestMentalStateConditionContext;
import languageTools.parser.Test.TestSectionContext;
import languageTools.parser.Test.TimeoutContext;
import languageTools.parser.Test.UnitTestContext;
import languageTools.parser.TestVisitor;
import languageTools.parser.agent.MyGOALLexer;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.UserSpecAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.mas.Launch;
import languageTools.program.mas.LaunchRule;
import languageTools.program.mas.MASProgram;
import languageTools.program.test.AgentTest;
import languageTools.program.test.TestAction;
import languageTools.program.test.TestCollection;
import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.UnitTest;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.AtEnd;
import languageTools.program.test.testcondition.Eventually;
import languageTools.program.test.testcondition.Never;
import languageTools.program.test.testcondition.TestCondition;
import languageTools.program.test.testcondition.Until;
import languageTools.program.test.testcondition.While;
import languageTools.program.test.testsection.AssertTest;
import languageTools.program.test.testsection.DoActionSection;
import languageTools.program.test.testsection.EvaluateIn;
import languageTools.program.test.testsection.TestSection;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Validates a test file and constructs a test program.
 *
 * @author Koen Hindriks
 */
@SuppressWarnings("rawtypes")
public class TestValidator extends
		Validator<MyGOALLexer, Test, TestErrorStrategy, UnitTest> implements
		TestVisitor {
	private Test parser;
	private MASProgram masProgram;
	private AgentProgram agentProgram;
	private static TestErrorStrategy strategy = null;

	/**
	 * Creates the test validator.
	 *
	 * @param source
	 */
	public TestValidator(String filename) {
		super(filename);
	}

	@Override
	protected MyGOALLexer getNewLexer(CharStream stream,
			ANTLRErrorListener errorlistener) {
		return new MyGOALLexer(stream, errorlistener);
	}

	@Override
	protected Test getNewParser(TokenStream stream) {
		this.parser = new Test(stream);
		return this.parser;
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.unitTest();
	}

	@Override
	protected TestErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new TestErrorStrategy();
		}
		return strategy;
	}

	public void overrideMAS(MASProgram mas2g) {
		this.masProgram = mas2g;
	}

	@Override
	protected UnitTest getNewProgram(File file) {
		return new UnitTest(new InputStreamPosition(0, 0, 0, 0, file));
	}

	@Override
	protected void secondPass(ParseTree tree) {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation calls {@link ParseTree#accept} on the
	 * specified tree.
	 * </p>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Void visit(@NotNull ParseTree tree) {
		tree.accept(this);
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitUnitTest(UnitTestContext ctx) {
		if (ctx == null) {
			return null;
		} else if (ctx.masFile() == null) {
			reportError(TestError.MAS_MISSING, ctx);
			return null;
		}
		try {
			if (this.masProgram == null) {
				File masFile = visitMasFile(ctx.masFile());
				if (masFile == null) {
					// Error covered by visitor.
					return null;
				}

				// Parse mas program and children.
				MASValidator createMas = new MASValidator(masFile.getPath());
				createMas.validate();
				this.masProgram = createMas.getProgram();
				if (!this.masProgram.isValid()) {
					Set<Message> masErrors = createMas.getErrors();
					masErrors.addAll(createMas.getSyntaxErrors());
					reportError(TestError.MAS_INVALID, ctx.masFile(),
							masFile.getPath(), masErrors.toString());
					return null;
				}
			}
			getProgram().setMASProgram(this.masProgram);

			for (File agentFile : this.masProgram.getAgentFiles()) {
				AgentValidator createAgent = new AgentValidator(
						agentFile.getPath());
				createAgent.setKRInterface(this.masProgram
						.getKRInterface(agentFile));
				createAgent.validate();
				AgentProgram agent = createAgent.getProgram();
				if (agent.isValid()) {
					getProgram().addAgent(agent);
				} else {
					Set<Message> agentErrors = createAgent.getErrors();
					agentErrors.addAll(createAgent.getSyntaxErrors());
					reportError(TestError.AGENT_INVALID, ctx.masFile(),
							agentFile.getPath(), agentErrors.toString());
				}
			}

			long timeout = 0L;
			if (ctx.timeout() != null) {
				timeout = visitTimeout(ctx.timeout());
			}
			getProgram().setTimeout(timeout);

			List<AgentTests> tests = (ctx.agentTests() == null) ? null
					: visitAgentTests(ctx.agentTests());
			if (tests == null || tests.isEmpty() || tests.get(0).isEmpty()) {
				return null;
			}

			// We now have a list of tests for each agent. These have to be
			// transformed into a list of unit tests containing one test for
			// each agent.
			for (int i = 0; i < tests.get(0).size(); i++) {
				for (int j = 0; j < tests.size(); j++) {
					getProgram().addTest(tests.get(j).get(i));
				}
				// FIXME: For now only allow 1 unit test.
				// Silently ignores any other unit tests!
				break;
			}
			return null;
		} catch (Exception any) {
			// Convert stack trace to string
			StringWriter sw = new StringWriter();
			any.printStackTrace(new PrintWriter(sw));
			reportError(SyntaxError.FATAL, null,
					any.getMessage() + "\n" + sw.toString());
			return null;
		}
	}

	@Override
	public File visitMasFile(MasFileContext ctx) {
		if (ctx.MASFILE() == null) {
			reportError(TestError.MAS_MISSING, ctx);
			return null;
		}

		String masFileName = ctx.MASFILE().getText().replace("\"", "");
		File masFile = resolveFileReference(getSourceInfo(ctx).getSource()
				.getParent(), masFileName);

		if (masFile == null) {
			reportError(TestError.MAS_INVALID, ctx, masFileName, "no such file");
			return null;
		} else {
			return masFile;
		}
	}

	@Override
	public Long visitTimeout(TimeoutContext ctx) {
		String number = (ctx.FLOAT() == null) ? "" : ctx.FLOAT().getText();
		try {
			return Long.parseLong(number) * 1000;
		} catch (NumberFormatException e) {
			reportError(TestError.TIMEOUT_INVALID, ctx, number);
			return null;
		}
	}

	@Override
	public List<AgentTests> visitAgentTests(AgentTestsContext ctx) {
		List<AgentTests> tests = new ArrayList<>(ctx.agentTest().size());
		for (AgentTestContext testCtx : ctx.agentTest()) {
			AgentTests agentTests = visitAgentTest(testCtx);
			if (agentTests == null) {
				return null;
			}
			for (AgentTests t : tests) {
				// Check duplicates
				if (agentTests.getName().equals(t.getName())) {
					reportError(TestError.TEST_DUPLICATE, testCtx,
							agentTests.getName(), t.getName());
					return null;
				}
				// Check size matches other tests
				if (agentTests.size() != t.size()) {
					reportError(TestError.TEST_INVALID_SIZE, testCtx,
							agentTests.getName(),
							Integer.toString(agentTests.size()), t.getName(),
							Integer.toString(t.size()));
					return null;
				}
			}
			tests.add(agentTests);
		}
		return tests;
	}

	@Override
	public AgentTests visitAgentTest(AgentTestContext ctx) {
		if (ctx.ID() == null) {
			reportError(TestError.TEST_MISSING_AGENT, ctx);
			return null;
		}

		String agentName = null;
		File agentFile = null;
		for (LaunchRule launchrule : this.masProgram.getLaunchRules()) {
			for (Launch launch : launchrule.getInstructions()) {
				agentName = launch.getGivenName("", 0);
				if (ctx.ID().getText().equals(agentName)) {
					agentFile = launch.getAgentFile();
					break;
				}
			}
			if (agentFile != null) {
				break;
			}
		}
		if (agentFile == null) {
			reportError(TestError.TEST_MISSING_AGENT, ctx);
			return null;
		}

		this.agentProgram = getProgram().getAgent(agentFile);
		if (this.agentProgram == null) {
			reportError(TestError.AGENT_INVALID, ctx, agentFile.getPath(),
					"not found");
			return null;
		}

		if (ctx.test() == null) {
			return new AgentTests(agentName);
		} else if (ctx.test().isEmpty()) {
			return new AgentTests(agentName);
		}

		List<TestCollection> tests = new ArrayList<>(ctx.test().size());
		for (TestContext programCtx : ctx.test()) {
			TestCollection test = visitTest(programCtx);
			if (test == null) {
				return null;
			} else {
				tests.add(test);
			}
		}

		if (tests.isEmpty()) {
			reportError(TestError.TEST_MISSING, ctx, agentName);
			return null;
		}

		List<AgentTest> agentTests = new ArrayList<>(tests.size());
		for (TestCollection test : tests) {
			AgentTest agentTest = new AgentTest(agentName, test);
			agentTests.add(agentTest);
		}
		return new AgentTests(agentName, agentTests);
	}

	@Override
	public TestCollection visitTest(TestContext ctx) {
		if (ctx.ID() == null) {
			reportError(TestError.TEST_MISSING_NAME, ctx);
			return null;
		}

		String id = ctx.ID().getText();
		List<TestSection> testSections = new ArrayList<>(ctx.testSection()
				.size());
		for (TestSectionContext testSectionContext : ctx.testSection()) {
			TestSection section = visitTestSection(testSectionContext);
			if (section != null) {
				testSections.add(section);
			}
		}

		return new TestCollection(id, testSections);
	}

	@Override
	public TestSection visitTestSection(TestSectionContext ctx) {
		if (ctx.doActions() != null) {
			return visitDoActions(ctx.doActions());
		} else if (ctx.assertTest() != null) {
			return visitAssertTest(ctx.assertTest());
		} else if (ctx.evaluateIn() != null) {
			return visitEvaluateIn(ctx.evaluateIn());
		} else {
			return null;
		}
	}

	@Override
	public DoActionSection visitDoActions(DoActionsContext ctx) {
		if (ctx.actions() == null) {
			reportError(TestError.TEST_MISSING_ACTION, ctx);
			return null;
		}
		ActionCombo combo = visitActions(ctx.actions());
		return new DoActionSection(combo);
	}

	@Override
	public ActionCombo visitActions(ActionsContext ctx) {
		ActionCombo combo = new ActionCombo();
		GOAL parser = prepareGOALParser(ctx.getText());
		languageTools.parser.GOAL.ActionsContext comboContext = parser
				.actions();

		AgentValidator sub = new AgentValidator("inline-action");
		sub.setKRInterface(this.agentProgram.getKRInterface());
		ActionCombo subcombo = sub.visitActions(comboContext);
		Set<Message> errors = sub.getErrors();
		errors.addAll(sub.getSyntaxErrors());

		if (errors.isEmpty()) {
			for (Action<?> action : subcombo.getActions()) {
				Action<?> resolved;
				if (action instanceof UserSpecOrModuleCall) {
					resolved = AgentValidator.resolve(
							(UserSpecOrModuleCall) action, this.agentProgram);
				} else {
					resolved = action;
				}
				if (resolved == null) {
					reportError(TestError.TEST_INVALID_ACTION, ctx);
				} else {
					combo.addAction(resolved);
				}
			}
		} else {
			reportError(TestError.TEST_INVALID_ACTION, ctx);
		}

		return combo;
	}

	@Override
	public AssertTest visitAssertTest(AssertTestContext ctx) {
		if (ctx.mentalStateCondition() == null) {
			reportError(TestError.TEST_MISSING_TEST, ctx);
			return null;
		}

		MentalStateCondition condition = visitMentalStateCondition(ctx
				.mentalStateCondition());
		if (condition == null) {
			return null;
		} else if (ctx.StringLiteral() != null) {
			String text = ctx.StringLiteral().getText();
			String[] parts = text.split("(?<!\\\\)\"", 0);
			return new AssertTest(condition, parts[1].replace("\\\"", "\""));
		} else if (ctx.SingleQuotedStringLiteral() != null) {
			String text = ctx.SingleQuotedStringLiteral().getText();
			String[] parts = text.split("(?<!\\\\)'", 0);
			return new AssertTest(condition, parts[1].replace("\\'", "'"));
		} else {
			return new AssertTest(condition);
		}
	}

	@Override
	public MentalStateCondition visitMentalStateCondition(
			MentalStateConditionContext ctx) {
		GOAL parser = prepareGOALParser(ctx.getText());
		languageTools.parser.GOAL.MentalStateConditionContext conditionContext = parser
				.mentalStateCondition();

		AgentValidator sub = new AgentValidator("inline-condition");
		sub.setKRInterface(this.agentProgram.getKRInterface());
		MentalStateCondition condition = sub
				.visitMentalStateCondition(conditionContext);
		Set<Message> errors = sub.getErrors();
		errors.addAll(sub.getSyntaxErrors());

		if (!errors.isEmpty()) {
			reportError(TestError.TEST_INVALID_QUERY, ctx);
		}
		return condition;
	}

	@Override
	public EvaluateIn visitEvaluateIn(EvaluateInContext ctx) {
		Set<TestCondition> queries = new HashSet<>(ctx.testCondition().size());
		for (TestConditionContext subCtx : ctx.testCondition()) {
			TestCondition query = visitTestCondition(subCtx);
			if (query == null) {
				reportError(TestError.TEST_MISSING_TEST, subCtx);
			} else if (!queries.add(query)) {
				reportError(TestError.TEST_DUPLICATE, subCtx);
			}
		}
		for (ReactTestContext subCtx : ctx.reactTest()) {
			TestCondition query = visitReactTest(subCtx);
			if (query == null) {
				reportError(TestError.TEST_MISSING_TEST, subCtx);
			} else if (!queries.add(query)) {
				reportError(TestError.TEST_DUPLICATE, subCtx);
			}
		}

		ModuleCallAction module = null;
		if (ctx.doActions() != null) {
			DoActionSection actions = visitDoActions(ctx.doActions());
			if (actions != null && actions.getAction() != null
					&& actions.getAction().getActions() != null) {
				for (Action<?> action : actions.getAction().getActions()) {
					if (module == null && action instanceof ModuleCallAction) {
						module = (ModuleCallAction) action;
					} else {
						reportError(TestError.TEST_INVALID_ACTION, ctx);
						return null;
					}
				}
			}
		}
		if (module == null) {
			reportError(TestError.TEST_MISSING_ACTION, ctx);
			return null;
		}

		TestCondition boundary = null;
		if (ctx.testBoundary() != null) {
			boundary = visitTestBoundary(ctx.testBoundary());
		}

		return new EvaluateIn(queries, module, boundary, this.agentProgram);
	}

	@Override
	public TestCondition visitReactTest(ReactTestContext ctx) {
		if (ctx.testMentalStateCondition() != null
				&& ctx.testMentalStateCondition().size() == 2) {
			TestMentalStateCondition first = visitTestMentalStateCondition(ctx
					.testMentalStateCondition(0));
			TestMentalStateCondition second = visitTestMentalStateCondition(ctx
					.testMentalStateCondition(1));
			TestCondition returned = new Always(first);
			TestCondition nested = new Eventually(second);
			returned.setNestedCondition(nested);
			return returned;
		} else {
			return null;
		}
	}

	@Override
	public TestCondition visitTestCondition(TestConditionContext ctx) {
		TestCondition first = null, previous = null;
		for (TestConditionPartContext part : ctx.testConditionPart()) {
			TestCondition query = visitTestConditionPart(part);
			if (first == null) {
				first = query;
			} else if (previous != null) {
				previous.setNestedCondition(query);
			}
			previous = query;
		}
		return first;
	}

	@Override
	public TestCondition visitTestConditionPart(TestConditionPartContext ctx) {
		if (ctx.testMentalStateCondition() == null) {
			reportError(TestError.TEST_MISSING_TEST, ctx);
			return null;
		}

		TestMentalStateCondition condition = visitTestMentalStateCondition(ctx
				.testMentalStateCondition());
		if (condition == null) {
			reportError(TestError.TEST_INVALID_TEST,
					ctx.testMentalStateCondition());
			return null;
		}

		if (ctx.ATEND() != null) {
			return new AtEnd(condition);
		} else if (ctx.ALWAYS() != null) {
			return new Always(condition);
		} else if (ctx.NEVER() != null) {
			return new Never(condition);
		} else if (ctx.EVENTUALLY() != null) {
			return new Eventually(condition);
		} else {
			reportError(TestError.TEST_MISSING_OPERATOR, ctx);
			return null;
		}
	}

	@Override
	public TestCondition visitTestBoundary(TestBoundaryContext ctx) {
		if (ctx.testMentalStateCondition() == null) {
			reportError(TestError.TEST_MISSING_TEST, ctx);
			return null;
		}

		TestMentalStateCondition condition = visitTestMentalStateCondition(ctx
				.testMentalStateCondition());
		if (condition == null) {
			reportError(TestError.TEST_INVALID_TEST,
					ctx.testMentalStateCondition());
			return null;
		}

		if (ctx.WHILE() != null) {
			return new While(condition);
		} else if (ctx.UNTIL() != null) {
			return new Until(condition);
		} else {
			reportError(TestError.TEST_MISSING_OPERATOR, ctx);
			return null;
		}
	}

	@Override
	public UserSpecOrModuleCall visitDoTest(DoTestContext ctx) {
		UserSpecOrModuleCall call = null;
		if (ctx.PARLIST() != null) {
			List<Map.Entry<String, List<Term>>> actions = visitPARLIST(ctx
					.PARLIST().getText(), ctx);
			for (Map.Entry<String, List<Term>> action : actions) {
				if (action != null) {
					if (call == null) {
						call = new UserSpecOrModuleCall(action.getKey(),
								action.getValue(), getSourceInfo(ctx), null);
					} else {
						reportError(TestError.TEST_DUPLICATE_ACTION,
								ctx.PARLIST());
						break;
					}
				}
			}
		}
		return call;
	}

	public List<Map.Entry<String, List<Term>>> visitPARLIST(String parlist,
			ParserRuleContext ctx) {
		AgentValidator sub = new AgentValidator("inline-parlist");
		sub.setKRInterface(this.agentProgram.getKRInterface());
		List<Term> actions = sub.visitPARLIST(parlist, ctx);

		List<Map.Entry<String, List<Term>>> returned = new LinkedList<>();
		for (final Term action : actions) {
			GOAL parser = prepareGOALParser(action.toString());
			languageTools.parser.GOAL.DeclarationOrCallWithTermsContext callContext = parser
					.declarationOrCallWithTerms();
			Map.Entry<String, List<Term>> call = sub
					.visitDeclarationOrCallWithTerms(callContext);
			if (call != null) {
				returned.add(call);
			}
		}

		Set<Message> errors = sub.getErrors();
		errors.addAll(sub.getSyntaxErrors());
		if (!errors.isEmpty()) {
			reportError(TestError.TEST_INVALID_ACTION, ctx);
		}

		return returned;
	}

	@Override
	public TestMentalStateCondition visitTestMentalStateCondition(
			TestMentalStateConditionContext ctx) {
		InputStreamPosition first = null;
		MentalStateCondition condition = null;
		if (ctx.mentalStateCondition() != null) {
			condition = visitMentalStateCondition(ctx.mentalStateCondition());
			if (condition != null) {
				first = (InputStreamPosition) getSourceInfo(ctx
						.mentalStateCondition());
			}
		}
		InputStreamPosition second = null;
		TestAction testaction = null;
		if (ctx.doTest() != null) {
			UserSpecOrModuleCall call = visitDoTest(ctx.doTest());
			Action<?> action = AgentValidator.resolve(call, this.agentProgram);
			if (action instanceof UserSpecAction) {
				testaction = new TestAction((UserSpecAction) action, ctx
						.doTest().NOT() == null);
				second = (InputStreamPosition) getSourceInfo(ctx.doTest());
			} else {
				reportError(TestError.TEST_INVALID_ACTION, ctx.doTest());
			}
		}
		if (first == null) {
			return new TestMentalStateCondition(testaction, condition);
		} else if (second == null) {
			return new TestMentalStateCondition(condition, testaction);
		} else {
			if (first.compareTo(second) > 0) {
				return new TestMentalStateCondition(testaction, condition);
			} else {
				return new TestMentalStateCondition(condition, testaction);
			}
		}
	}

	// THE FUNCTIONS BELOW ARE UNUSED...

	@Override
	public Void visitSelector(SelectorContext ctx) {
		return null;
	}

	@Override
	public Void visitBeliefs(BeliefsContext ctx) {
		return null;
	}

	@Override
	public Void visitMentalOperator(MentalOperatorContext ctx) {
		return null;
	}

	@Override
	public Void visitNestedRules(NestedRulesContext ctx) {
		return null;
	}

	@Override
	public Void visitActionSpecs(ActionSpecsContext ctx) {
		return null;
	}

	@Override
	public Void visitRuleEvaluationOrder(RuleEvaluationOrderContext ctx) {
		return null;
	}

	@Override
	public Void visitModuleImport(ModuleImportContext ctx) {
		return null;
	}

	@Override
	public Void visitPostcondition(PostconditionContext ctx) {
		return null;
	}

	@Override
	public Void visitMentalAtom(MentalAtomContext ctx) {
		return null;
	}

	@Override
	public Void visitMacroDef(MacroDefContext ctx) {
		return null;
	}

	@Override
	public Void visitActionOperator(ActionOperatorContext ctx) {
		return null;
	}

	@Override
	public Void visitPrecondition(PreconditionContext ctx) {
		return null;
	}

	@Override
	public Void visitProgram(ProgramContext ctx) {
		return null;
	}

	@Override
	public Void visitKnowledge(KnowledgeContext ctx) {
		return null;
	}

	@Override
	public Void visitModule(ModuleContext ctx) {
		return null;
	}

	@Override
	public Void visitGoals(GoalsContext ctx) {
		return null;
	}

	@Override
	public Void visitActionSpec(ActionSpecContext ctx) {
		return null;
	}

	@Override
	public Void visitModules(ModulesContext ctx) {
		return null;
	}

	@Override
	public Void visitModuleDef(ModuleDefContext ctx) {
		return null;
	}

	@Override
	public Void visitAction(ActionContext ctx) {
		return null;
	}

	@Override
	public Void visitBasicCondition(BasicConditionContext ctx) {
		return null;
	}

	@Override
	public Void visitProgramRule(ProgramRuleContext ctx) {
		return null;
	}

	@Override
	public Void visitModuleOption(ModuleOptionContext ctx) {
		return null;
	}

	@Override
	public Void visitDeclaration(DeclarationContext ctx) {
		return null;
	}

	@Override
	public Void visitKrImport(KrImportContext ctx) {
		return null;
	}

	@Override
	public Void visitDeclarationOrCallWithTerms(
			DeclarationOrCallWithTermsContext ctx) {
		return null;
	}

	private class AgentTests {
		private final List<AgentTest> tests;
		private final String agentBaseName;

		public AgentTests(String agentBaseName, List<AgentTest> tests) {
			this.tests = tests;
			this.agentBaseName = agentBaseName;
		}

		public boolean isEmpty() {
			return this.tests.isEmpty();
		}

		public AgentTests(String agentName) {
			this(agentName, new ArrayList<AgentTest>(0));
		}

		public String getName() {
			return this.agentBaseName;
		}

		public int size() {
			return this.tests.size();
		}

		public AgentTest get(int i) {
			return this.tests.get(i);
		}
	}

	/**
	 * Tries to resolve a reference to a file.
	 * <p>
	 * Checks whether the reference refers to an existing file (is an absolute
	 * path), or else searches for the referenced file relative to the directory
	 * that is provided.
	 * </p>
	 *
	 * @param directory
	 *            Reference to the directory where the MAS file can be found
	 *            that contains the environment reference that needs to be
	 *            resolved.
	 * @param fileReference
	 *            A reference to an environment file contained in the MAS file.
	 * @return A file if the environment reference was resolved; {@code null}
	 *         otherwise.
	 */
	private static File resolveFileReference(String directory,
			String fileReference) {
		// Check whether reference refers to existing file.
		File file = new File(fileReference);
		if (file.exists()) {
			return file;
		}
		// Check whether reference refers to file that can be
		// located relative to the directory.
		File path = new File(directory);
		file = new File(path, fileReference);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	/**
	 * Creates an embedded GOAL parser that can parse the given string.
	 *
	 * @param pString
	 *            is the string to be parsed.
	 * @return a GOALWAlker that can parse text at the GOAL level.
	 */
	private GOAL prepareGOALParser(String pString) {
		try {
			ANTLRInputStream charstream = new ANTLRInputStream(
					new StringReader(pString));
			charstream.name = "";
			GOALLexer lexer = new GOALLexer(charstream);
			CommonTokenStream stream = new CommonTokenStream(lexer);
			return new GOAL(stream);
		} catch (IOException e) {
			return null;
		}
	}
}
