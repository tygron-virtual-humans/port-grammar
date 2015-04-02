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

package languageTools.analyzer.agent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import krTools.KRInterface;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import languageTools.analyzer.Validator;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.agent.AgentError;
import languageTools.errors.agent.AgentErrorStrategy;
import languageTools.errors.agent.AgentWarning;
import languageTools.parser.GOAL;
import languageTools.parser.GOAL.ActionContext;
import languageTools.parser.GOAL.ActionOperatorContext;
import languageTools.parser.GOAL.ActionSpecContext;
import languageTools.parser.GOAL.ActionSpecsContext;
import languageTools.parser.GOAL.ActionsContext;
import languageTools.parser.GOAL.BasicConditionContext;
import languageTools.parser.GOAL.BeliefsContext;
import languageTools.parser.GOAL.DeclarationContext;
import languageTools.parser.GOAL.DeclarationOrCallWithTermsContext;
import languageTools.parser.GOAL.GoalsContext;
import languageTools.parser.GOAL.KnowledgeContext;
import languageTools.parser.GOAL.KrImportContext;
import languageTools.parser.GOAL.MacroDefContext;
import languageTools.parser.GOAL.MentalAtomContext;
import languageTools.parser.GOAL.MentalOperatorContext;
import languageTools.parser.GOAL.MentalStateConditionContext;
import languageTools.parser.GOAL.ModuleContext;
import languageTools.parser.GOAL.ModuleDefContext;
import languageTools.parser.GOAL.ModuleImportContext;
import languageTools.parser.GOAL.ModuleOptionContext;
import languageTools.parser.GOAL.ModulesContext;
import languageTools.parser.GOAL.NestedRulesContext;
import languageTools.parser.GOAL.PostconditionContext;
import languageTools.parser.GOAL.PreconditionContext;
import languageTools.parser.GOAL.ProgramContext;
import languageTools.parser.GOAL.ProgramRuleContext;
import languageTools.parser.GOAL.RuleEvaluationOrderContext;
import languageTools.parser.GOAL.SelectorContext;
import languageTools.parser.GOALVisitor;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.agent.MyGOALLexer;
import languageTools.program.agent.ActionSpecification;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.ExitCondition;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.Module.TYPE;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.DropAction;
import languageTools.program.agent.actions.ExitModuleAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.LogAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.PrintAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.SendOnceAction;
import languageTools.program.agent.actions.UserSpecAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalALiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.rules.ForallDoRule;
import languageTools.program.agent.rules.IfThenRule;
import languageTools.program.agent.rules.ListallDoRule;
import languageTools.program.agent.rules.Rule;
import languageTools.program.agent.selector.Selector;
import languageTools.symbolTable.Scope;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.agent.MacroSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;
import languageTools.symbolTable.agent.VarSymbol;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Validates an agent or module file and constructs an agent program or module.
 * DOC explain what is validated. How to use. What happens if filename does not
 * validate. How to validate just one term instead of a whole program.
 * Apparently it does not validate everything as there also is
 * {@link AgentValidatorSecondPass}.
 */
@SuppressWarnings("rawtypes")
public class AgentValidator extends
		Validator<MyGOALLexer, GOAL, AgentErrorStrategy, AgentProgram>
		implements GOALVisitor {

	private GOAL parser;
	private static AgentErrorStrategy strategy = null;

	/**
	 * Knowledge representation interface used for parsing the contents of
	 * beliefs, etc.
	 */
	private KRInterface kri;

	/**
	 * For agent validation, we use three symbol tables. The first is used for
	 * actions and modules. Action labels and module names cannot have the same
	 * signature because a call cannot be resolved in that case; this motivates
	 * introducing a single table for both. The second is used for predicate
	 * symbols and the third for macros.
	 */
	private final SymbolTable actionSymbols = new SymbolTable();
	private Scope varSymbols = new SymbolTable();

	public AgentValidator(String filename) {
		super(filename);
	}

	public AgentValidator(String filename, AgentProgram program) {
		super(filename, program);
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.modules();
	}

	@Override
	protected AgentErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new AgentErrorStrategy();
		}
		return strategy;
	}

	/**
	 * Sets the KR interface that should be used for parsing KR fragments.
	 *
	 * @param kri
	 *            The KR interface that should be used.
	 */
	public void setKRInterface(KRInterface kri) {
		this.kri = kri;
	}

	/**
	 * @return Symbol table with action and module symbols.
	 */
	public SymbolTable getActionSymbols() {
		return this.actionSymbols;
	}

	/**
	 * @return Symbol table with variable symbols.
	 */
	public Scope getVarSymbols() {
		return this.varSymbols;
	}

	/**
	 * Validation of agent program resolves references to action, macro, and
	 * module symbols, and checks whether all predicates used have been defined.
	 */
	@Override
	protected void secondPass(ParseTree tree) {
		AgentValidatorSecondPass pass = new AgentValidatorSecondPass(this);
		pass.validate();
	}

	@Override
	protected MyGOALLexer getNewLexer(CharStream stream,
			ANTLRErrorListener errorlistener) {
		return new MyGOALLexer(stream, errorlistener);
	}

	@Override
	protected GOAL getNewParser(TokenStream stream) {
		this.parser = new GOAL(stream);
		return this.parser;
	}

	@Override
	protected AgentProgram getNewProgram(File file) {
		return new AgentProgram(new InputStreamPosition(0, 0, 0, 0, file));
	}

	/**
	 * Calls {@link ParseTree#accept} on the specified tree.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Void visit(@NotNull ParseTree tree) {
		tree.accept(this);

		return null; // Java says must return something even when Void
	}

	// -------------------------------------------------------------
	// Modules
	// -------------------------------------------------------------

	@Override
	public Void visitModules(ModulesContext ctx) {
		// Set KR interface
		getProgram().setKRInterface(this.kri);

		// Get imported module files
		for (ModuleImportContext imported : ctx.moduleImport()) {
			visitModuleImport(imported);
		}

		// Get modules defined in agent file
		for (ModuleContext module : ctx.module()) {
			visitModule(module);
		}

		// Check for main or event module
		Module mainOrEvent = null;
		for (Module module : getProgram().getModules()) {
			if (module.getType() == TYPE.MAIN || module.getType() == TYPE.EVENT) {
				mainOrEvent = module;
				break;
			}
		}

		// Report error if no main or event module
		if (mainOrEvent == null) {
			reportError(AgentError.PROGRAM_NO_MAIN_NOR_EVENT, getProgram()
					.getSourceInfo());
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitModuleImport(ModuleImportContext ctx) {
		if (ctx.MODULEFILE() != null) {
			String path = removeLeadTrailCharacters(ctx.MODULEFILE().getText());
			File file = new File(getPathRelativeToSourceFile(path));
			// Check existence of file. Extension check handled in grammar.
			if (file.exists()) {
				getProgram().addImportedModule(file);
			} else {
				reportError(AgentError.IMPORT_MISSING_FILE, ctx, file.getPath());
			}
		}
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitModule(ModuleContext ctx) {
		Module module = null;

		// Process module declaration
		module = visitModuleDef(ctx.moduleDef());

		// Process module options
		visitModuleOptions(ctx, module);

		List<DatabaseFormula> knowledge = new LinkedList<>();
		// Imported knowledge
		if (ctx.krImport() != null) {
			boolean hadImport = false;
			for (KrImportContext kriCtx : ctx.krImport()) {
				if (hadImport) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION, kriCtx);
				} else {
					knowledge.addAll(visitKrImport(kriCtx));
					hadImport = true;
				}
			}
		}
		// Knowledge
		if (ctx.knowledge() != null) {
			boolean hadKnowledge = false;
			for (KnowledgeContext knowCtx : ctx.knowledge()) {
				if (hadKnowledge) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION,
							knowCtx);
				} else {
					knowledge.addAll(visitKnowledge(knowCtx));
					hadKnowledge = true;
				}
			}
		}
		module.setKnowledge(knowledge);

		// Beliefs
		if (ctx.beliefs() != null) {
			boolean hadBeliefs = false;
			for (BeliefsContext belCtx : ctx.beliefs()) {
				if (hadBeliefs) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION, belCtx);
				} else {
					module.setBeliefs(visitBeliefs(belCtx));
					hadBeliefs = true;
				}
			}
		}

		// Goals
		if (ctx.goals() != null) {
			boolean hadGoals = false;
			for (GoalsContext goalCtx : ctx.goals()) {
				if (hadGoals) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION,
							goalCtx);
				} else {
					module.setGoals(visitGoals(goalCtx));
					hadGoals = true;
				}
			}
		}

		// Program
		boolean hadProgram = false;
		if (ctx.program() != null) {
			// Process rule evaluation order
			for (ProgramContext progCtx : ctx.program()) {
				if (hadProgram) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION,
							progCtx);
				} else {
					RuleEvaluationOrder order = visitRuleEvaluationOrder(progCtx
							.ruleEvaluationOrder());
					if (order == null) {
						order = getDefaultRuleEvaluationOrder(module.getType());
					}
					module.setRuleEvaluationOrder(order);

					// Process content of program section
					Map.Entry<List<Macro>, List<Rule>> program = visitProgram(progCtx);
					module.setMacros(program.getKey());
					module.setRules(program.getValue());

					// Check if program section is empty
					if (module.getRules().isEmpty()) {
						reportWarning(AgentWarning.MODULE_EMPTY_PROGRAMSECTION,
								progCtx, module.getNamePhrase());
					}

					hadProgram = true;
				}
			}
		}
		if (!hadProgram && module.getType() != TYPE.INIT) {
			reportError(AgentError.MODULE_MISSING_PROGRAM_SECTION, ctx,
					module.getNamePhrase());
		}

		// Action specifications
		if (ctx.actionSpecs() != null) {
			boolean hadSpecs = false;
			for (ActionSpecsContext specCtx : ctx.actionSpecs()) {
				if (hadSpecs) {
					reportWarning(AgentWarning.MODULE_DUPLICATE_SECTION,
							specCtx);
				} else {
					module.setActionSpecifications(visitActionSpecs(specCtx));
					hadSpecs = true;
				}
			}
		}

		// Add module to program
		getProgram().addModule(module);

		// Define symbol
		ModuleSymbol msym = new ModuleSymbol(module.getSignature(), module,
				getSourceInfo(ctx));
		if (!this.actionSymbols.define(msym)) {
			// Report duplicate action label
			Symbol symbol = this.actionSymbols.resolve(module.getSignature());
			String specifiedAs = null;
			if (symbol instanceof ActionSymbol) {
				specifiedAs = "action";
			} else if (symbol instanceof ModuleSymbol) {
				specifiedAs = "module";
			}
			if (specifiedAs != null) {
				reportError(AgentError.ACTION_LABEL_ALREADY_DEFINED, ctx,
						"Module " + module.getSignature(), specifiedAs);
			}
		}

		// Remove variable scope for this module again
		this.varSymbols = this.varSymbols.getEnclosingScope();

		return null;
	}

	@Override
	public Module visitModuleDef(ModuleDefContext ctx) {
		Module module = null;

		if (ctx.declaration() != null) {
			Map.Entry<String, List<Term>> function = visitDeclaration(ctx
					.declaration());
			module = new Module(function.getKey(), TYPE.USERDEF, this.kri,
					getSourceInfo(ctx));
			module.setParameters(function.getValue());
		} else if (ctx.INIT() != null) {
			module = new Module(TYPE.INIT.getDisplayName(), TYPE.INIT,
					this.kri, getSourceInfo(ctx));
		} else if (ctx.EVENT() != null) {
			module = new Module(TYPE.EVENT.getDisplayName(), TYPE.EVENT,
					this.kri, getSourceInfo(ctx));
		} else if (ctx.MAIN() != null) {
			module = new Module(TYPE.MAIN.getDisplayName(), TYPE.MAIN,
					this.kri, getSourceInfo(ctx));
		}

		// Add variable parameters of module to new scope
		this.varSymbols = this.varSymbols.getNewScope(module.getName());
		for (Term term : module.getParameters()) {
			this.varSymbols.define(new VarSymbol(term.getSignature(),
					getSourceInfo(ctx)));
		}

		return module;
	}

	public void visitModuleOptions(ModuleContext ctx, Module module) {
		// Set default exit option, overwrite below if option is explicitly set
		module.setExitCondition(getDefaultExitCondition(module.getType()));

		Map<String, String> keyValuePairs = new HashMap<String, String>();
		for (ModuleOptionContext option : ctx.moduleOption()) {
			Map.Entry<String, String> keyValuePair = visitModuleOption(option);
			String key = keyValuePair.getKey();
			String value = keyValuePair.getValue();

			// Check for duplicates
			if (keyValuePairs.containsKey(keyValuePair.getKey())) {
				reportWarning(AgentWarning.MODULE_DUPLICATE_OPTION, option,
						keyValuePair.getKey());
				continue;
			} else {
				keyValuePairs.put(keyValuePair.getKey(),
						keyValuePair.getValue());
			}

			// Process option
			try {
				if (key.equals(getTokenName(GOAL.EXIT))) {
					if (module.getType() == TYPE.INIT
							|| module.getType() == TYPE.EVENT) {
						reportWarning(AgentWarning.MODULE_USELESS_EXIT, option,
								module.getType().toString());
					} else {
						module.setExitCondition(ExitCondition.valueOf(value
								.toUpperCase()));
					}
					continue;
				} else if (key.equals(getTokenName(GOAL.FOCUS))) {
					if (module.getType() != TYPE.USERDEF) {
						reportWarning(AgentWarning.MODULE_ILLEGAL_FOCUS, option);
					} else {
						module.setFocusMethod(FocusMethod.valueOf(value
								.toUpperCase()));
					}
					continue;
				} else {
					reportWarning(AgentWarning.MODULE_UNKNOWN_OPTION, option,
							key);
				}
			} catch (IllegalArgumentException e) {
				reportWarning(AgentWarning.MODULE_UNKNOWN_OPTION, option, value);
			}
		}
	}

	@Override
	public Map.Entry<String, String> visitModuleOption(ModuleOptionContext ctx) {
		return new AbstractMap.SimpleEntry<String, String>(ctx.key.getText(),
				ctx.value.getText());
	}

	// -------------------------------------------------------------
	// Module sections
	// -------------------------------------------------------------

	@Override
	public List<DatabaseFormula> visitKrImport(KrImportContext ctx) {
		List<DatabaseFormula> imported = new ArrayList<>(0);
		String path = null;
		if (ctx.StringLiteral() != null) {
			// TODO: what is the logic here?
			String text = ctx.StringLiteral().getText();
			String[] parts = text.split("(?<!\\\\)\"", 0);
			path = parts[1].replace("\\\"", "\"");
		}
		if (ctx.SingleQuotedStringLiteral() != null) {
			// TODO: what is the logic here?
			String text = ctx.SingleQuotedStringLiteral().getText();
			String[] parts = text.split("(?<!\\\\)'", 0);
			path = parts[1].replace("\\'", "'");
		}
		File file = (path == null) ? null : new File(
				getPathRelativeToSourceFile(path));
		// Check existence of file. Extension check handled in grammar.
		if (file != null && file.exists()) {
			try {
				String content = new String(Files.readAllBytes(Paths.get(file
						.getPath())));
				imported = visit_KR_DBFs(content, new InputStreamPosition(0, 0,
						0, 0, file));
			} catch (Exception e) {
				// Convert stack trace to string
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				reportError(SyntaxError.FATAL, getSourceInfo(ctx),
						e.getMessage() + "\n" + sw.toString());
			}
		} else {
			reportError(AgentError.IMPORT_MISSING_FILE, ctx, file.getPath());
		}
		return imported;
	}

	@Override
	public List<DatabaseFormula> visitKnowledge(KnowledgeContext ctx) {
		return visit_KR_DBFs(
				removeLeadTrailCharacters(ctx.KR_BLOCK().getText()),
				getSourceInfo(ctx.KR_BLOCK()));
	}

	@Override
	public List<DatabaseFormula> visitBeliefs(BeliefsContext ctx) {
		return visit_KR_DBFs(
				removeLeadTrailCharacters(ctx.KR_BLOCK().getText()),
				getSourceInfo(ctx.KR_BLOCK()));
	}

	@Override
	public List<Query> visitGoals(GoalsContext ctx) {
		List<Query> dbfs = visit_KR_Queries(removeLeadTrailCharacters(ctx
				.KR_BLOCK().getText()), getSourceInfo(ctx.KR_BLOCK()));

		// Check that goals (queries) are closed and can be used as updates, if
		// not remove them
		List<Query> errors = new LinkedList<Query>();
		for (Query dbf : dbfs) {
			/*
			 * if (!dbf.isClosed()) {
			 * reportError(AgentError.GOAL_UNINSTANTIATED_VARIABLE,
			 * dbf.getSourceInfo(), dbf.getFreeVar().toString(),
			 * dbf.toString()); errors.add(dbf); }
			 */
			if (!dbf.isUpdate()) {
				reportError(AgentError.GOALSECTION_NOT_AN_UPDATE,
						dbf.getSourceInfo(), dbf.toString());
				errors.add(dbf);
			}
		}
		dbfs.removeAll(errors);

		return dbfs;
	}

	@Override
	public Map.Entry<List<Macro>, List<Rule>> visitProgram(ProgramContext ctx) {
		// Process macro definitions
		List<Macro> macros = new ArrayList<Macro>(ctx.macroDef().size());
		for (MacroDefContext macrodf : ctx.macroDef()) {
			Macro macro = visitMacroDef(macrodf);
			if (macro != null) {
				macros.add(macro);
			}
		}

		// Process program rules
		List<Rule> rules = new ArrayList<Rule>(ctx.programRule().size());
		for (ProgramRuleContext programRule : ctx.programRule()) {
			Rule rule = visitProgramRule(programRule);
			if (rule != null) {
				rules.add(rule);
			}
		}

		return new AbstractMap.SimpleEntry<List<Macro>, List<Rule>>(macros,
				rules);
	}

	@Override
	public RuleEvaluationOrder visitRuleEvaluationOrder(
			RuleEvaluationOrderContext ctx) {
		if (ctx != null && ctx.value != null) {
			try {
				return RuleEvaluationOrder.valueOf(ctx.value.getText()
						.toUpperCase());
			} catch (IllegalArgumentException e) {
				// simply ignore, parser will report problem
			}
		}

		return null;
	}

	@Override
	public Macro visitMacroDef(MacroDefContext ctx) {
		Map.Entry<String, List<Term>> declaration = visitDeclarationOrCallWithTerms(ctx
				.declarationOrCallWithTerms());
		MentalStateCondition msc = visitMentalStateCondition(ctx
				.mentalStateCondition());

		Macro macro = new Macro(declaration.getKey(), declaration.getValue(),
				msc, getSourceInfo(ctx));

		// Check whether macro parameters have been used in definitions
		if (!msc.getFreeVar().containsAll(declaration.getValue())) {
			reportError(AgentError.MACRO_PARAMETERS_NOT_IN_DEFINITION,
					macro.getSourceInfo(), prettyPrintSet(new HashSet<>(
							declaration.getValue())), msc.toString());
		}

		// Add macro to symbol table
		if (!getProgram().getMacros()
				.define(new MacroSymbol(macro.getSignature(), macro,
						getSourceInfo(ctx)))) {
			// report duplicate use of macro symbol
			reportError(AgentError.MACRO_DUPLICATE_NAME, ctx,
					macro.getSignature());
		}

		return macro;
	}

	@Override
	public Rule visitProgramRule(ProgramRuleContext ctx) {
		Rule rule = null;

		// Get mental state condition
		MentalStateCondition msc = visitMentalStateCondition(ctx
				.mentalStateCondition());

		// Check that variables used in selectors are bound
		isSelectorVarBound(msc.getSubFormulas(), new HashSet<Var>());

		// Get action part of rule
		ActionCombo actions = null;
		if (ctx.actions() != null) {
			actions = visitActions(ctx.actions());
		}
		// Check for type of rule
		if (ctx.nestedRules() != null) {

			// Add variable parameters of anonymous module to a new scope
			this.varSymbols = this.varSymbols.getNewScope("anonymous");
			for (Term term : msc.getFreeVar()) {
				this.varSymbols.define(new VarSymbol(term.getSignature(),
						getSourceInfo(ctx)));
			}

			Module module = visitNestedRules(ctx.nestedRules());
			ModuleCallAction action = new ModuleCallAction(module,
					new ArrayList<Term>(0), getSourceInfo(ctx), this.kri);
			actions = new ActionCombo();
			actions.addAction(action);
		}

		// Create rule of right type
		if (ctx.IF() != null) {
			rule = new IfThenRule(msc, actions);
		}
		if (ctx.FORALL() != null) {
			rule = new ForallDoRule(msc, actions);
		}
		if (ctx.LISTALL() != null) {
			Var var = null;
			try {
				// Check if there is a parser problem with variable; if so,
				// don't do anything (parser will report error)
				if (!(ctx.VAR() instanceof ErrorNodeImpl)) {
					String name = ctx.VAR().getText();
					var = visit_KR_Var(name, getSourceInfo(ctx));

					// Check for Prolog anonymous variable
					/*
					 * if (this.kri.getName().equals(KRFactory.SWI_PROLOG) &&
					 * ((PrologVar) var).isAnonymous()) { FIXME CANNOT USE
					 * PROLOGVAR HERE reportError(
					 * AgentError.PROLOG_LISTALL_ANONYMOUS_VARIABLE, ctx.VAR(),
					 * name); var = null; }
					 */
				}

				rule = new ListallDoRule(msc, var, actions);
			} catch (ParserException e) {
				// Report problem, return null, and try to continue with parsing
				// the rest of the source.
				reportParsingException(e, getSourceInfo(ctx));
			}
		}

		// Report issue if no actions were found
		if (rule != null && actions == null) {
			reportError(AgentError.RULE_MISSING_BODY, ctx, rule.prettyPrint());
			rule = null;
		}

		return rule;
	}

	@Override
	public MentalStateCondition visitMentalStateCondition(
			MentalStateConditionContext ctx) {
		List<MentalFormula> formulas = new LinkedList<MentalFormula>();

		// Check if something bad happened, and if so, whether we still can
		// report anything sensible
		if (ctx == null) {
			// can't do anything sensible here, job of parser to report back
		} else if (ctx.exception != null && ctx.basicCondition() == null) {
			if (ctx.getText().replaceAll("[^\\(]", "").length() != ctx
					.getText().replaceAll("[^\\)]", "").length()) {
				// bracket imbalance
				reportError(AgentError.MSC_BRACKET_DO_NOT_MATCH, ctx,
						ctx.getText());
			} else if (!ctx.getText().startsWith(getTokenName(GOAL.GOAL_OP))
					&& !ctx.getText().startsWith(getTokenName(GOAL.GOALA_OP))
					&& !ctx.getText().startsWith(getTokenName(GOAL.AGOAL_OP))
					&& !ctx.getText().startsWith(getTokenName(GOAL.BELIEF_OP))
					&& !ctx.getText().startsWith(getTokenName(GOAL.NOT))) {
				String found = "no operator";
				if (ctx.children != null) {
					found = ctx.getChild(0).toString();
				}
				reportError(AgentError.MSC_INVALID_OPERATOR, ctx, found);
			} else if (ctx.getText().startsWith(getTokenName(GOAL.NOT))) {
				// starts with negation, perhaps not applied to mental atom?
				reportError(AgentError.MSC_INVALID_NOT, ctx, ctx.getText());
			}
		} else {
			MentalFormula formula = visitBasicCondition(ctx.basicCondition());
			// do not add atoms that could not be validated
			if (formula != null) {
				formulas.add(formula);
			}
			if (ctx.mentalStateCondition() != null) {
				formulas.addAll(visitMentalStateCondition(
						ctx.mentalStateCondition()).getSubFormulas());
			}
		}

		return new MentalStateCondition(formulas);
	}

	@Override
	public MentalFormula visitBasicCondition(BasicConditionContext ctx) {
		if (ctx.declarationOrCallWithTerms() != null) {
			Map.Entry<String, List<Term>> macro = visitDeclarationOrCallWithTerms(ctx
					.declarationOrCallWithTerms());
			return new Macro(macro.getKey(), macro.getValue(), null,
					getSourceInfo(ctx));
		}
		if (ctx.NOT() != null) {
			MentalLiteral atom = visitMentalAtom(ctx.mentalAtom());
			atom.setPolarity(false);
			return atom;
		}
		if (ctx.mentalAtom() != null) {
			return visitMentalAtom(ctx.mentalAtom());
		}
		if (ctx.TRUE() != null) {
			// nothing to do
			return null;
		}
		// Report issue
		reportError(AgentError.RULE_MISSING_CONDITION, ctx, ctx.getText());

		return null;
	}

	@Override
	public MentalLiteral visitMentalAtom(MentalAtomContext ctx) {
		// Get selector and operator
		Selector selector = visitSelector(ctx.selector());
		String op = visitMentalOperator(ctx.mentalOperator());

		// Get condition
		String krFragment = removeLeadTrailCharacters(ctx.PARLIST().getText());
		Query query = visit_KR_Query(krFragment, getSourceInfo(ctx));

		// If no query was returned, we cannot return a literal that we can use
		// later for
		// validation purposes; return null
		if (query == null) {
			return null;
		}

		if (op.equals(getTokenName(GOAL.BELIEF_OP))) {
			return new BelLiteral(true, selector, query, getSourceInfo(ctx));
		} else if (op.equals(getTokenName(GOAL.GOAL_OP))) {
			return new GoalLiteral(true, selector, query, getSourceInfo(ctx));
		} else {
			// Check for Prolog anonymous variable
			/*
			 * for (Var var : query.getFreeVar()) { FIXME CANNOT USE PROLOGVAR
			 * HERE if (this.kri.getName().equals(KRFactory.SWI_PROLOG) &&
			 * ((PrologVar) var).isAnonymous()) { reportError(
			 * AgentError.PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE, ctx,
			 * var.toString(), ctx.toString()); } }
			 */
			if (op.equals(getTokenName(GOAL.AGOAL_OP))) {
				return new AGoalLiteral(true, selector, query,
						getSourceInfo(ctx));
			} else if (op.equals(getTokenName(GOAL.GOALA_OP))) {
				return new GoalALiteral(true, selector, query,
						getSourceInfo(ctx));
			}
		}

		return null;
	}

	@Override
	public String visitMentalOperator(MentalOperatorContext ctx) {
		return ctx.op.getText();
	}

	@Override
	public ActionCombo visitActions(ActionsContext ctx) {
		ActionCombo actions = new ActionCombo();

		for (ActionContext actionCtx : ctx.action()) {
			Action<?> action = visitAction(actionCtx);
			if (action == null) {
				continue;
			}
			if (actions.size() > 0
					&& actions.getActions().get(actions.size() - 1) instanceof ExitModuleAction) {
				reportWarning(AgentWarning.EXITMODULE_CANNOT_REACH, actionCtx,
						action.toString());
			}
			actions.addAction(action);
		}

		return actions;
	}

	@Override
	public Action visitAction(ActionContext ctx) {
		if (ctx.actionOperator() != null) { // Must be action that has KR
			// content
			// Get selector
			Selector selector = visitSelector(ctx.selector());

			// Get action operator
			String op = visitActionOperator(ctx.actionOperator());
			if (op == null) {
				// Can't figure out which action but don't return null.
				return new UserSpecOrModuleCall("<missing name>",
						new ArrayList<Term>(0), getSourceInfo(ctx), this.kri);
			}

			TerminalNode parlistctx = ctx.PARLIST();
			String argument = removeLeadTrailCharacters(parlistctx.getText());

			// Handle cases
			if (op.equals(AgentProgram.getTokenName(GOAL.PRINT))) {
				Term parameter = visit_KR_Term(argument,
						getSourceInfo(parlistctx));
				return new PrintAction(parameter, getSourceInfo(parlistctx),
						this.kri);
			} else if (op.equals(AgentProgram.getTokenName(GOAL.LOG))) {
				return new LogAction(
						removeLeadTrailCharacters(parlistctx.getText()),
						getSourceInfo(parlistctx), this.kri);
			} else {
				// send actions may have initial mood operator; check
				SentenceMood mood = getMood(argument);
				if (mood == null) { // set default mood
					mood = SentenceMood.INDICATIVE;
				} else { // remove mood operator from content
					argument = argument.replaceFirst(// keep indexes
							Pattern.quote(mood.toString()), " ");
				}
				// Parse content using KR parser
				Update content = visit_KR_Update(argument,
						getSourceInfo(parlistctx));
				if (content != null) {
					if (op.equals(AgentProgram.getTokenName(GOAL.ADOPT))) {
						checkEmpty(content, getSourceInfo(parlistctx), false);
						return new AdoptAction(selector, content,
								getSourceInfo(parlistctx), this.kri);
					} else if (op.equals(AgentProgram.getTokenName(GOAL.DROP))) {
						checkEmpty(content, getSourceInfo(parlistctx), false);
						return new DropAction(selector, content,
								getSourceInfo(parlistctx), this.kri);
					} else if (op
							.equals(AgentProgram.getTokenName(GOAL.INSERT))) {
						checkEmpty(content, getSourceInfo(parlistctx), true);
						return new InsertAction(selector, content,
								getSourceInfo(parlistctx), this.kri);
					} else if (op
							.equals(AgentProgram.getTokenName(GOAL.DELETE))) {
						checkEmpty(content, getSourceInfo(parlistctx), false);
						return new DeleteAction(selector, content,
								getSourceInfo(parlistctx), this.kri);
					} else if (op.equals(AgentProgram.getTokenName(GOAL.SEND))) {
						checkEmpty(content, getSourceInfo(parlistctx), true);
						checkSendSelector(selector, ctx);
						return new SendAction(selector, mood, content,
								getSourceInfo(parlistctx), this.kri);
					} else if (op.equals(AgentProgram
							.getTokenName(GOAL.SENDONCE))) {
						checkEmpty(content, getSourceInfo(parlistctx), true);
						checkSendSelector(selector, ctx);
						return new SendOnceAction(selector, mood, content,
								getSourceInfo(parlistctx), this.kri);
					}
				}
				return null;
			}
		} else if (ctx.declarationOrCallWithTerms() != null) {
			Map.Entry<String, List<Term>> action = visitDeclarationOrCallWithTerms(ctx
					.declarationOrCallWithTerms());
			return new UserSpecOrModuleCall(action.getKey(), action.getValue(),
					getSourceInfo(ctx), this.kri);
		} else if (ctx.op.getType() == GOAL.EXITMODULE) {
			return new ExitModuleAction(getSourceInfo(ctx), this.kri);
		} else {
			return new UserSpecOrModuleCall(ctx.op.getText(),
					new ArrayList<Term>(0), getSourceInfo(ctx), this.kri);
		}
	}

	private void checkEmpty(Update content, SourceInfo info,
			boolean allowNegative) {
		boolean addEmpty = content.getAddList().isEmpty();
		boolean deleteEmpty = content.getDeleteList().isEmpty();
		if (addEmpty && deleteEmpty) {
			reportError(AgentError.UPDATE_EMPTY, info);
		} else if (!allowNegative && !deleteEmpty) {
			reportError(AgentError.UPDATE_NEGATIVE, info);
		}
	}

	@Override
	public String visitActionOperator(ActionOperatorContext ctx) {
		if (ctx.op != null) {
			return ctx.op.getText();
		} else {
			return null;
		}
	}

	@Override
	public Selector visitSelector(SelectorContext ctx) {
		if (ctx == null) {
			// return default
			return Selector.getDefault();
		} else if (ctx.PARLIST() != null) {
			List<Term> terms = visitPARLIST(ctx.PARLIST().getText(), ctx);
			return new Selector(terms);
		} else {
			String op = ctx.op.getText();
			return new Selector(Selector.SelectorType.valueOf(op.toUpperCase()));
		}
	}

	@Override
	public Module visitNestedRules(NestedRulesContext ctx) {
		List<Rule> rules = new ArrayList<Rule>(ctx.programRule().size());
		for (ProgramRuleContext programRule : ctx.programRule()) {
			Rule rule = visitProgramRule(programRule);
			if (rule != null) {
				rules.add(rule);
			}
		}
		Module module = new Module("", TYPE.ANONYMOUS, this.kri,
				getSourceInfo(ctx));
		module.setParameters(new ArrayList<Term>(0));
		module.setRuleEvaluationOrder(getDefaultRuleEvaluationOrder(module
				.getType()));
		module.setRules(rules);

		// Remove variable scope for this module again.
		this.varSymbols = this.varSymbols.getEnclosingScope();

		return module;
	}

	@Override
	public List<ActionSpecification> visitActionSpecs(ActionSpecsContext ctx) {
		List<ActionSpecification> specs = new ArrayList<ActionSpecification>(
				ctx.actionSpec().size());
		for (ActionSpecContext context : ctx.actionSpec()) {
			ActionSpecification spec = visitActionSpec(context);
			if (spec != null) { // ignore if not OK
				specs.add(spec);
			}
		}
		return specs;
	}

	@Override
	public ActionSpecification visitActionSpec(ActionSpecContext ctx) {
		boolean problem = false;

		// Get internal/external annotation (external by default)
		boolean external = true;
		if (ctx.INTERNAL() != null) {
			external = false;
		}
		// Get action
		Map.Entry<String, List<Term>> declaration = visitDeclarationOrCallWithTerms(ctx
				.declarationOrCallWithTerms());
		// Check for duplicate parameters
		Set<Term> checkDuplicates = new HashSet<Term>();
		for (Term term : declaration.getValue()) {
			if (!checkDuplicates.add(term)) {
				reportError(AgentError.ACTIONSPEC_DUPLICATE_PARAMETER,
						ctx.declarationOrCallWithTerms(), term.toString());
			}
		}

		// Get precondition
		Query precondition = null;
		if (ctx.precondition() != null) {
			precondition = visitPrecondition(ctx.precondition());
		}
		problem = (precondition == null);

		// Get postcondition
		Update postcondition = null;
		if (ctx.postcondition() != null) {
			postcondition = visitPostcondition(ctx.postcondition());
		}
		problem |= (postcondition == null);

		if (!problem) {
			// Create action
			UserSpecAction action = new UserSpecAction(declaration.getKey(),
					declaration.getValue(), external, precondition,
					postcondition, getSourceInfo(ctx), this.kri);

			// Check use of action parameters and variables in postcondition
			Set<Var> actionParsNotUsed = action.getFreeVar();
			actionParsNotUsed.removeAll(postcondition.getFreeVar());
			actionParsNotUsed.removeAll(precondition.getFreeVar());
			if (!actionParsNotUsed.isEmpty()) {
				reportWarning(AgentWarning.ACTIONSPEC_PARAMETER_NOT_USED,
						ctx.declarationOrCallWithTerms(),
						prettyPrintSet(actionParsNotUsed));
			}
			/*
			 * Set<Var> postVarNotBound = postcondition.getFreeVar();
			 * postVarNotBound.removeAll(action.getFreeVar());
			 * postVarNotBound.removeAll(precondition.getFreeVar()); if
			 * (!postVarNotBound.isEmpty()) {
			 * reportError(AgentError.POSTCONDITION_UNBOUND_VARIABLE,
			 * ctx.postcondition(), prettyPrintSet(postVarNotBound)); }
			 */

			// Create action specification
			ActionSpecification spec = new ActionSpecification(action);

			// Define symbol
			if (!this.actionSymbols.define(new ActionSymbol(action
					.getSignature(), spec, getSourceInfo(ctx)))) {
				// Report duplicate action label
				Symbol symbol = this.actionSymbols.resolve(action
						.getSignature());
				String specifiedAs = null;
				if (symbol instanceof ActionSymbol) {
					specifiedAs = "action";
				} else if (symbol instanceof ModuleSymbol) {
					specifiedAs = "module";
				}
				if (specifiedAs != null) {
					reportError(AgentError.ACTION_LABEL_ALREADY_DEFINED, ctx,
							"Action " + action.getSignature(), specifiedAs);
				}
			}
			return spec;
		} else {
			return null;
		}
	}

	@Override
	public Query visitPrecondition(PreconditionContext ctx) {
		String krFragment = removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		if (krFragment.isEmpty()) {
			reportWarning(AgentWarning.ACTIONSPEC_MISSING_PRE, ctx);
		}
		return visit_KR_Query(krFragment, getSourceInfo(ctx));
	}

	@Override
	public Update visitPostcondition(PostconditionContext ctx) {
		String krFragment = removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		if (krFragment.isEmpty()) {
			reportWarning(AgentWarning.ACTIONSPEC_MISSING_POST, ctx);
		}

		return visit_KR_Update(krFragment, getSourceInfo(ctx));
	}

	@Override
	public Map.Entry<String, List<Term>> visitDeclaration(DeclarationContext ctx) {
		String name = null;
		List<Term> parameters;

		// Get functor name
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		// Get parameters
		if (ctx.PARLIST() != null) {
			parameters = visitVARLIST(ctx.PARLIST().getText(), ctx);
		} else {
			parameters = new ArrayList<Term>(0);
		}

		return new AbstractMap.SimpleEntry<String, List<Term>>(name, parameters);
	}

	@Override
	public Map.Entry<String, List<Term>> visitDeclarationOrCallWithTerms(
			DeclarationOrCallWithTermsContext ctx) {
		String name = null;
		List<Term> parameters = new ArrayList<Term>(0);

		// Get functor name
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		if (ctx.PARLIST() != null) {
			parameters = visitPARLIST(ctx.PARLIST().getText(), ctx);
		}

		return new AbstractMap.SimpleEntry<String, List<Term>>(name, parameters);
	}

	/**
	 * Delegate parsing of PARLIST terminal node to KR parser and checks whether
	 * terms are variables and reports errors if this is not the case.
	 *
	 * @param pars
	 *            String text from PARLIST terminal.
	 * @param ctx
	 *            Parser context where PARLIST was found.
	 * @return List of terms.
	 */
	public List<Term> visitVARLIST(String pars, ParserRuleContext ctx) {
		List<Term> parameters = visitPARLIST(pars, ctx);

		for (Term term : parameters) {
			if (!term.isVar()) {
				reportError(AgentError.PARAMETER_NOT_A_VARIABLE, ctx,
						strategy.prettyPrintRuleContext(ctx.getParent()
								.getRuleIndex()), term.toString());
			}
		}

		return parameters;
	}

	/**
	 * Delegate parsing of PARLIST terminal node to KR parser.
	 *
	 * @param pars
	 *            String text from PARLIST terminal.
	 * @param ctx
	 *            Parser context where PARLIST was found.
	 * @return List of terms.
	 */
	public List<Term> visitPARLIST(String pars, ParserRuleContext ctx) {
		List<Term> parameters = visit_KR_Terms(removeLeadTrailCharacters(pars),
				getSourceInfo(ctx));

		// If no parameters were returned, return the empty list to avoid a
		// cascade of errors.
		if (parameters == null) {
			parameters = new ArrayList<Term>(0);
		}

		/*
		 * for (Term node : parameters) { FIXME cannot use PrologTerm here // KR
		 * specific check: cannot use Prolog anonymous variable as // parameter
		 * if (this.kri.getName().equals(KRFactory.SWI_PROLOG) && ((PrologTerm)
		 * node).isAnonymousVar()) {
		 * reportError(AgentError.PROLOG_ANONYMOUS_VARIABLE, ctx,
		 * node.toString()); } }
		 */

		return parameters;
	}

	// -------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------

	/**
	 * Check whether send(once) action has valid selector type.
	 *
	 * @param selector
	 *            Selector found.
	 * @param ctx
	 *            Parser context.
	 */
	private void checkSendSelector(Selector selector, ParserRuleContext ctx) {
		switch (selector.getType()) {
		case ALL:
		case ALLOTHER:
		case PARAMETERLIST:
		case SELF:
			break;
		case SOME:
		case SOMEOTHER:
		case THIS:
			reportError(AgentError.SEND_INVALID_SELECTOR, ctx,
					selector.toString());
			break;
		}
	}

	/**
	 * @param type
	 *            Type of module.
	 * @return The default exit condition associated with the module type.
	 */
	public ExitCondition getDefaultExitCondition(TYPE type) {
		if (type == TYPE.MAIN) {
			return ExitCondition.NEVER;
		} else {
			return ExitCondition.ALWAYS;
		}
	}

	/**
	 * @param type
	 *            Type of module.
	 * @return The default rule evaluation order associated with the module
	 *         type.
	 */
	public RuleEvaluationOrder getDefaultRuleEvaluationOrder(TYPE type) {
		if (type == TYPE.ANONYMOUS || type == TYPE.EVENT || type == TYPE.INIT) {
			return RuleEvaluationOrder.LINEARALL;
		} else {
			return RuleEvaluationOrder.LINEAR;
		}
	}

	/**
	 * Extracts sentence mood from message string, if any.
	 *
	 * @param msg
	 *            Message that is part of send action.
	 * @return Mood operator, if message starts with operator, {@code null}
	 *         otherwise.
	 */
	public SentenceMood getMood(String msg) {
		String trimmed = msg.trim();
		if (trimmed.startsWith("!")) {
			return SentenceMood.IMPERATIVE;
		} else if (trimmed.startsWith("?")) {
			return SentenceMood.INTERROGATIVE;
		} else if (trimmed.startsWith(":")) {
			return SentenceMood.INDICATIVE;
		} else {
			return null;
		}
	}

	/**
	 * @param token
	 *            A token index (can be found in GOAL grammar)
	 * @return The name of the token.
	 */
	private String getTokenName(int token) {
		return GOAL.tokenNames[token].replaceAll("'", "");
	}

	/**
	 * Checks whether all variables used in selectors in the list of mental
	 * formulas are bound. Variables in the given set of variables are
	 * considered bound. Reports variable(s) that are not bound as a validation
	 * error.
	 *
	 * @param formulas
	 *            A list of formulas to check.
	 * @param boundVars
	 *            A set of variables that may bound variables in selectors in
	 *            the formulas.
	 * @return {@code true} if all variables that occur in selectors are bound,
	 *         {@code false} otherwise.
	 */
	private boolean isSelectorVarBound(List<MentalFormula> formulas,
			Set<Var> boundVars) {
		boolean bound = true;
		Set<Var> selectorVars;

		for (MentalFormula formula : formulas) {
			if (formula instanceof MentalLiteral) {
				selectorVars = ((MentalLiteral) formula).getSelector()
						.getFreeVar();
				selectorVars.removeAll(boundVars);
				selectorVars = outOfScope(selectorVars);
				bound &= selectorVars.isEmpty();
				if (!bound) { // report that some variables are not bound
					reportError(AgentError.SELECTOR_VAR_NOT_BOUND,
							formula.getSourceInfo(),
							prettyPrintSet(selectorVars));
				}
				// add variables bound by this formula to boundVars
				boundVars.addAll(formula.getFreeVar());
			} else if (formula instanceof Macro) {
				Macro macro = (Macro) formula;
				if (macro.getDefinition() != null) {
					bound &= isSelectorVarBound(macro.getDefinition()
							.getSubFormulas(), boundVars);
				}
			}
		}

		return bound;
	}

	/**
	 * Checks whether all variables in a set are accessible within the current
	 * variable scope.
	 *
	 * @param vars
	 *            Set of variables to check.
	 * @return Set of variables that are out of scope.
	 */
	private Set<Var> outOfScope(Set<Var> vars) {
		Set<Var> outOfScope = new HashSet<Var>();

		for (Var var : vars) {
			if (this.varSymbols.resolve(var.getSignature()) == null) {
				outOfScope.add(var);
			}
		}

		return outOfScope;
	}

	/**
	 * Reports a parsing exception that occurred while parsing embedded language
	 * fragments.
	 *
	 * @param e
	 *            The exception generated by the embedded language parser.
	 * @param ctx
	 *            The context of the agent parser where the embedded language
	 *            fragment is located.
	 */
	private void reportParsingException(ParserException e, SourceInfo info) {
		String msg = e.getMessage();
		if (e.getCause() != null) {
			msg += " because " + e.getCause().getMessage();
		}
		reportError(SyntaxError.EMBEDDED_LANGUAGE_ERROR, info, msg);
	}

	/**
	 * Reports parsing errors that occurred while parsing embedded language
	 * fragments.
	 *
	 * @param parser
	 *            The parser that generated the errors.
	 */
	private void reportEmbeddedLanguageErrors(Parser parser, SourceInfo info) {
		for (SourceInfo error : parser.getErrors()) {
			// ignore null errors; we cannot make anything out of those...
			if (error != null) {
				InputStreamPosition pos = new InputStreamPosition(
						error.getLineNumber(), error.getCharacterPosition(),
						info.getStartIndex() + error.getStartIndex(),
						info.getStopIndex() + error.getStopIndex(),
						error.getSource());
				reportError(SyntaxError.EMBEDDED_LANGUAGE_ERROR, pos,
						error.getMessage());
			}
		}
	}

	// -------------------------------------------------------------
	// Helper methods - embedded KR language fragments
	// -------------------------------------------------------------

	/**
	 * Processes embedded KR language fragments (used for knowledge and belief
	 * sections). Assumes that these fragments represent {@link DatabaseFormula}
	 * s.
	 *
	 * @param krFragments
	 *            List of KR fragments.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return List of {@link DatabaseFormula}s.
	 */
	public List<DatabaseFormula> visit_KR_DBFs(String krFragment,
			SourceInfo info) {
		List<DatabaseFormula> formulas = new ArrayList<DatabaseFormula>(0);

		// Get the formulas
		try {
			Parser parser = this.kri.getParser(new StringReader(krFragment),
					info);
			formulas = parser.parseDBFs();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, and try to continue with parsing the rest of the
			// source.
			reportParsingException(e, info);
		}

		if (formulas == null) {
			return new ArrayList<DatabaseFormula>(0);
		}

		return formulas;
	}

	/**
	 * Processes embedded KR language fragments (used for built-in actions).
	 * Assumes that these fragments represent an {@link Update}.
	 *
	 * @param krFragment
	 *            String with KR fragment.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return {@link Update}.
	 */
	public Update visit_KR_Update(String krFragment, SourceInfo info) {
		Update update = null;

		// Get the update
		try {
			Parser parser = this.kri.getParser(new StringReader(krFragment),
					info);
			update = parser.parseUpdate();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, and try to continue with parsing the rest of the
			// source.
			reportParsingException(e, info);
		}

		return update;
	}

	/**
	 * Processes embedded KR language fragments (used for goals section).
	 * Assumes that these fragments represent a {@link List<Query>}.
	 *
	 * @param krFragments
	 *            List of KR fragments.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return A {@link List<Query>}.
	 */
	public List<Query> visit_KR_Queries(String krFragment, SourceInfo info) {
		List<Query> queries = new ArrayList<Query>(0);

		if (krFragment.isEmpty()) {
			return queries;
		}

		// Get the queries
		try {
			Parser parser = this.kri.getParser(new StringReader(krFragment),
					info);
			queries = parser.parseQueries();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, return, and try to continue with parsing the rest
			// of the source.
			reportParsingException(e, info);
		}

		return queries;
	}

	/**
	 * Processes embedded KR language fragments (used for mental literals and
	 * preconditions). Assumes that these fragments represent a {@link Query}.
	 *
	 * @param krFragments
	 *            List of KR fragments.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return A {@link Query}.
	 */
	public Query visit_KR_Query(String krFragment, SourceInfo info) {
		Query query = null;

		// Get the query
		Parser parser;
		try {
			parser = this.kri.getParser(new StringReader(krFragment), info);
			query = parser.parseQuery();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, return, and try to continue with parsing the rest
			// of the source.
			reportParsingException(e, info);
		}

		return query;
	}

	/**
	 * Processes embedded KR language fragments (used for print action and
	 * selector expressions). Assumes that these fragments represent a
	 * {@link Term}.
	 *
	 * @param krFragment
	 *            KR fragment string.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return A {@link Term}.
	 */
	public Term visit_KR_Term(String krFragment, SourceInfo info) {
		Term term = null;

		// Get the term
		try {
			Parser parser = this.kri.getParser(new StringReader(krFragment),
					info);
			term = parser.parseTerm();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, return, and try to continue with parsing the rest
			// of the source.
			reportParsingException(e, info);
		}

		return term;
	}

	/**
	 * Processes embedded KR language fragment (used for all parameter lists of
	 * actions, macros, modules, and also for content of mental literals and
	 * mental actions). Assumes that these fragments represent a {@link Term}.
	 *
	 * @param krFragment
	 *            KR fragment string.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return A {@link Term}.
	 */
	public List<Term> visit_KR_Terms(String krFragment, SourceInfo info) {
		List<Term> parameters = null;

		try {
			Parser parser = this.kri.getParser(new StringReader(krFragment),
					info);
			parameters = parser.parseTerms();

			// Add errors from parser for embedded language to our own
			reportEmbeddedLanguageErrors(parser, info);
		} catch (ParserException e) {
			// Report problem, return, and try to continue with parsing the rest
			// of the source.
			reportParsingException(e, info);
		}

		return parameters;
	}

	/**
	 * Parses a terminal node that should contain the text (name) of a variable.
	 * In other words, assumes that the text associated with the node represents
	 * a {@link Var}.
	 *
	 * @param name
	 *            the variable text to be parsed.
	 * @param info
	 *            the source info where in the source is the first char of this
	 *            text fragment
	 * @return The variable we got from parsing the node.
	 * @throws ParserException
	 *             See {@link ParserException}.
	 */
	public Var visit_KR_Var(String name, SourceInfo info)
			throws ParserException {
		Parser parser = this.kri.getParser(new StringReader(name), info);
		Var var = parser.parseVar();

		// Add errors from parser for embedded language to our own
		reportEmbeddedLanguageErrors(parser, info);

		return var;
	}

	// TODO: this code implements a rather naive approach to getting the
	// action specification(s) of the action. In particular, it does not take
	// scope into account. If an action is specified e.g. at top level and again
	// within a module, then this approach is not able to determine which action
	// specification should be associated with the action.
	// If the user intends to execute the action in the module, we maybe should
	// allow for expressions of the form: module.action(parameters) so that the
	// user is able to refer to the right scope.
	public static Action<?> resolve(UserSpecOrModuleCall call,
			AgentProgram program) {
		if (call == null || program == null) {
			return null;
		}
		for (Module module : program.getModules()) {
			if (call.getName().equals(module.getName())
					&& call.getParameters().size() == module.getParameters()
							.size()) {
				return new ModuleCallAction(module, call.getParameters(),
						call.getSourceInfo(), program.getKRInterface());
			}
			for (ActionSpecification specification : module
					.getActionSpecifications()) {
				UserSpecAction spec = specification.getAction();
				if (call.getName().equals(spec.getName())
						&& call.getParameters().size() == spec.getParameters()
								.size()) {
					return new UserSpecAction(call.getName(),
							call.getParameters(), spec.getExernal(),
							((MentalLiteral) spec.getPrecondition()
									.getSubFormulas().get(0)).getFormula(),
							spec.getPostcondition(), call.getSourceInfo(),
							program.getKRInterface());
				}
			}
		}
		return null;
	}
}