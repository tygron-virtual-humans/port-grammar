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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import languageTools.analyzer.Validator;
import languageTools.analyzer.agent.AgentValidator;
import languageTools.analyzer.mas.MASValidator;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentErrorStrategy;
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
import languageTools.parser.Test.TestBoundaryContext;
import languageTools.parser.Test.TestConditionContext;
import languageTools.parser.Test.TestConditionPartContext;
import languageTools.parser.Test.TestContext;
import languageTools.parser.Test.TestModuleContext;
import languageTools.parser.Test.TestSectionContext;
import languageTools.parser.Test.TimeoutContext;
import languageTools.parser.Test.UnitTestContext;
import languageTools.parser.TestVisitor;
import languageTools.parser.agent.MyGOALLexer;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.TYPE;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.mas.Launch;
import languageTools.program.mas.LaunchRule;
import languageTools.program.mas.MASProgram;
import languageTools.program.test.AgentTest;
import languageTools.program.test.TestCollection;
import languageTools.program.test.UnitTest;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.AtEnd;
import languageTools.program.test.testcondition.AtStart;
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
Validator<MyGOALLexer, Test, AgentErrorStrategy, UnitTest> implements
TestVisitor {
	private Test parser;
	private MASProgram masProgram;
	private Map<File, AgentProgram> agentPrograms;
	private AgentProgram agentProgram;
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
	protected MyGOALLexer getNewLexer(CharStream stream,
			ANTLRErrorListener errorlistener) {
		return new MyGOALLexer(stream, errorlistener);
	}

	@Override
	protected Test getNewParser(TokenStream stream) {
		return new Test(stream);
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.unitTest();
	}

	@Override
	protected AgentErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new AgentErrorStrategy();
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
	public UnitTest visitUnitTest(UnitTestContext ctx) {
		if (ctx == null) {
			return null;
		} else if (ctx.masFile() == null) {
			// reportError("Missing MAS file declaration", ctx);
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
					// reportError("MAS file %s is not valid: %s",
					// ctx.masFile(),
					// masFile.getPath(), createMas.getErrors());
					return null;
				}
			}
			this.agentPrograms = new HashMap<>(this.masProgram.getAgentFiles()
					.size());
			for (File agentFile : this.masProgram.getAgentFiles()) {
				AgentValidator createAgent = new AgentValidator(
						agentFile.getPath());
				createAgent.validate();
				AgentProgram agent = createAgent.getProgram();
				if (agent.isValid()) {
					this.agentPrograms.put(agentFile, agent);
				} else {
					// reportError("Agent file %s is not valid: %s",
					// ctx.masFile(),
					// agentFile.getPath(), createAgent.getErrors());
				}
			}

			long timeout = 0L;
			if (ctx.timeout() != null) {
				timeout = visitTimeout(ctx.timeout());
			}

			if (ctx.agentTests() == null) {
				return new UnitTest(this.masProgram,
						new ArrayList<AgentTest>(0), timeout,
						getSourceInfo(ctx));
			}

			List<AgentTests> tests = visitAgentTests(ctx.agentTests());
			if (tests == null) { // Error covered by visitor.
				return null;
			} else if (tests.isEmpty() || tests.get(0).isEmpty()) {
				return new UnitTest(this.masProgram,
						new ArrayList<AgentTest>(0), timeout,
						getSourceInfo(ctx));
			}

			// We now have a list of tests for each agent. These have to be
			// transformed into a list of unit tests containing one test for
			// each agent.
			List<UnitTest> unitTests = new ArrayList<>(tests.get(0).size());
			for (int i = 0; i < tests.get(0).size(); i++) {
				List<AgentTest> agentTests = new ArrayList<>(tests.size());
				for (int j = 0; j < tests.size(); j++) {
					agentTests.add(tests.get(j).get(i));
				}
				unitTests.add(new UnitTest(this.masProgram, agentTests,
						timeout, getSourceInfo(ctx)));
			}
			// FIXME: For now only allow 1 unit test.
			// Silently ignores any other unit tests!
			return unitTests.get(0);
		} catch (Exception any) {
			// this.wh.report(new ValidatorError(GOALError.EXTERNAL_OR_UNKNOWN,
			// this.wh.getPosition(ctx), any.getMessage()));
			return null;
		}
	}

	@Override
	public File visitMasFile(MasFileContext ctx) {
		if (ctx.MASFILE() == null) {
			// reportError("Missing MAS file value", ctx);
			return null;
		}

		String masFileName = ctx.MASFILE().getText().replace("\"", "");
		File masFile = resolveFileReference(getSourceInfo(ctx).getSource()
				.getParent(), masFileName);

		if (masFile == null) {
			// reportError("MAS file %s does not exist", ctx, masFileName);
			return null;
		} else {
			return masFile;
		}
	}

	@Override
	public Long visitTimeout(TimeoutContext ctx) {
		if (ctx.FLOAT() == null) {
			// reportError("Expected a number for timeout",ctx);
			return null;
		}
		String number = ctx.FLOAT().getText();
		try {
			return Long.parseLong(number) * 1000;
		} catch (NumberFormatException e) {
			// reportError("Could not parse %s to number", ctx, number);
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
					// reportError("Found duplicate test for agent %s", testCtx,
					// agentTests.getName());
					return null;
				}
				// Check size matches other tests
				if (agentTests.size() != t.size()) {
					// reportError("%s has %s tests while %s has %s", testCtx,
					// agentTests.getName(), agentTests.size(),
					// t.getName(), t.size());
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
			// reportError("Missing agent declaration", ctx);
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
		}
		if (agentFile == null) {
			// Error covered by visitor.
			return null;
		}

		this.agentProgram = this.agentPrograms.get(agentFile);
		if (this.agentProgram == null) {
			// reportError("Agent program %s was invalid", ctx,
			// agentFile.getPath());
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
			// reportError("Could not find a test program for %s", ctx,
			// agentName);
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
			// reportError("Missing name for test", ctx);
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
		if (ctx == null) {
			// reportError("Missing action or module call", ctx);
			return null;
		}
		ActionCombo combo = visitActions(ctx.actions());
		return new DoActionSection(combo);
	}

	@Override
	public ActionCombo visitActions(ActionsContext ctx) {
		// HACK: Checking for main. GOAL doesn't understand this.
		ActionCombo combo;
		if (ctx.getText().equals("main")) {
			combo = insertMainModuleCall(ctx);
		} else {
			GOAL parser = prepareGOALParser(ctx.getText());
			languageTools.parser.GOAL.ActionsContext comboContext = parser
					.actions();
			AgentValidator sub = new AgentValidator("inline");
			combo = sub.visitActions(comboContext);
			for (Message err : sub.getErrors()) {
				reportError((SyntaxError) err.getType(), err.getSource(),
						err.getArguments());
			}
		}
		return combo;
	}

	@Override
	public AssertTest visitAssertTest(AssertTestContext ctx) {
		if (ctx.mentalStateCondition() == null) {
			// reportError("Missing mental state test", ctx);
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
		AgentValidator sub = new AgentValidator("inline");
		MentalStateCondition condition = sub
				.visitMentalStateCondition(conditionContext);
		for (Message err : sub.getErrors()) {
			reportError((SyntaxError) err.getType(), err.getSource(),
					err.getArguments());
		}
		return condition;
	}

	@Override
	public EvaluateIn visitEvaluateIn(EvaluateInContext ctx) {
		List<TestCondition> queries = new ArrayList<>(ctx.testCondition()
				.size());
		for (TestConditionContext subCtx : ctx.testCondition()) {
			TestCondition query = visitTestCondition(subCtx);
			if (query == null) {
				// reportError("Missing or invalid query", subCtx);
			} else {
				queries.add(query);
			}
		}

		if (ctx.doActions() == null) {
			// reportError("Missing action", ctx);
			return null;
		}
		DoActionSection action = visitDoActions(ctx.doActions());

		TestCondition boundary = null;
		if (ctx.testBoundary() != null) {
			boundary = visitTestBoundary(ctx.testBoundary());
		}

		return new EvaluateIn(queries, action, boundary, this.agentProgram);
	}

	@Override
	public TestCondition visitTestCondition(TestConditionContext ctx) {
		TestCondition first = null, previous = null;
		for (TestConditionPartContext part : ctx.testConditionPart()) {
			TestCondition query = visitTestConditionPart(part);
			if (first == null) {
				first = query;
			} else if (previous != null) {
				previous.setNestedCondition(previous);
			}
			previous = query;
		}
		return first;
	}

	@Override
	public TestCondition visitTestConditionPart(TestConditionPartContext ctx) {
		if (ctx.mentalStateCondition() == null) {
			// reportError("Missing mental state test", ctx);
			return null;
		}

		MentalStateCondition condition = visitMentalStateCondition(ctx
				.mentalStateCondition());
		if (condition == null) {
			// reportError("Illegal mental state test",
			// ctx.mentalStateCondition());
			return null;
		}

		Module module = null;
		if (ctx.testModule() != null) {
			String moduleName = visitTestModule(ctx.testModule());
			for (Module check : this.agentProgram.getModules()) {
				if (check.getName().equals(moduleName)) {
					module = check;
					break;
				}
			}
			if (module == null) {
				// reportError("Indicated module could not be found",
				// ctx.testModule());
			}
		}

		if (ctx.ATSTART() != null) {
			return new AtStart(condition, module);
		} else if (ctx.ATEND() != null) {
			return new AtEnd(condition, module);
		} else if (ctx.ALWAYS() != null) {
			return new Always(condition);
		} else if (ctx.NEVER() != null) {
			return new Never(condition);
		} else if (ctx.EVENTUALLY() != null) {
			return new Eventually(condition);
		} else {
			// reportError("No valid temporal operator provided", ctx);
			return null;
		}
	}

	@Override
	public String visitTestModule(TestModuleContext ctx) {
		if (ctx.INIT() != null) {
			return ctx.INIT().getText();
		} else if (ctx.EVENT() != null) {
			return ctx.EVENT().getText();
		} else if (ctx.MAIN() != null) {
			return ctx.MAIN().getText();
		} else {
			return ctx.declaration().getText();
		}
	}

	@Override
	public TestCondition visitTestBoundary(TestBoundaryContext ctx) {
		if (ctx.mentalStateCondition() == null) {
			// reportError("Missing mental state test", ctx);
			return null;
		}

		MentalStateCondition condition = visitMentalStateCondition(ctx
				.mentalStateCondition());
		if (condition == null) {
			// reportError("Illegal mental state test",
			// ctx.mentalStateCondition());
			return null;
		}

		if (ctx.WHILE() != null) {
			return new While(condition);
		} else if (ctx.UNTIL() != null) {
			return new Until(condition);
		} else {
			// reportError("No valid temporal operator provided", ctx);
			return null;
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
	public Void visitDeclarationOrCallWithTerms(
			DeclarationOrCallWithTermsContext ctx) {
		return null;
	}

	@Override
	public Void visitDeclaration(DeclarationContext ctx) {
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
			GOALLexer lexer = new GOALLexer(charstream);
			CommonTokenStream stream = new CommonTokenStream(lexer);
			return new GOAL(stream);
		} catch (IOException e) {
			return null;
		}
	}

	private ActionCombo insertMainModuleCall(ActionsContext ctx) {
		Module main = null;
		for (Module module : this.agentProgram.getModules()) {
			if (module.getType() == TYPE.MAIN) {
				main = module;
				break;
			}
		}
		ActionCombo actions = new ActionCombo();
		ModuleCallAction mainCall = new ModuleCallAction(main,
				getSourceInfo(ctx));
		actions.addAction(mainCall);
		return actions;
	}
}
