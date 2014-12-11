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

package languageTools.program.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.agent.AgentValidator;
import languageTools.program.Program;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.rules.Rule;

/**
 * A module consist of:
 * <ul>
 * <li>a knowledge base</li>
 * <li>a belief base</li>
 * <li>a goal base</li>
 * <li>a program section (a set of action rules)</li>
 * <li>an action specification section</li>
 * </ul>
 * All sections except for the program section are optional; only the
 * <code>init</code> module does not need to have a program section. </p>
 * <p>
 * Use {@link AgentValidator} to create a module from a text file with extension
 * 'mod2g'.
 * </p>
 *
 * @author Koen Hindriks
 */
public class Module extends Program {

	// -------------------------------------------------------------
	// Module declaration section
	// -------------------------------------------------------------

	/**
	 * Identifier for this {@link Module}.
	 *
	 * <p>
	 * The following names are reserved and used for built-in module types:
	 * <ul>
	 * <li>init</li>
	 * <li>main</li>
	 * <li>event</li>
	 * </ul>
	 */
	private String name;
	/**
	 * The (possibly empty) list of parameters of this module.
	 *
	 * <p>
	 * For anonymous modules, the parameters are derived by the validator from
	 * the variables present in the rule that calls/triggers it.
	 * </p>
	 */
	private List<Term> parameters = new ArrayList<Term>();

	/**
	 * The focus method that is used when entering the module.
	 */
	private FocusMethod focusMethod = FocusMethod.NONE;
	/**
	 * Condition that specifies when to exit the module.
	 */
	private ExitCondition exitCondition = ExitCondition.ALWAYS;
	/**
	 * <p>
	 * List of imported files.
	 * </p>
	 *
	 * <p>
	 * It should be possible to resolve the location of files that are imported
	 * relative to the source file used to construct this module (alternatively,
	 * the path should be absolute).
	 * </p>
	 */
	private final List<File> importedFiles = new ArrayList<File>();

	// -------------------------------------------------------------
	// Mental state sections
	// -------------------------------------------------------------

	/**
	 * The knowledge specified in the knowledge section of the module.
	 */
	private List<DatabaseFormula> knowledge = new ArrayList<DatabaseFormula>();
	/**
	 * The beliefs specified in the beliefs section of the module.
	 */
	private List<DatabaseFormula> beliefs = new ArrayList<DatabaseFormula>();
	/**
	 * The goals specified in the goals section of the module.
	 */
	private List<Query> goals = new ArrayList<Query>();

	// -------------------------------------------------------------
	// Program section
	// -------------------------------------------------------------

	/**
	 * Rule evaluation order option. By default, order is linear.
	 */
	private RuleEvaluationOrder order = RuleEvaluationOrder.LINEAR;
	/**
	 * The macros specified at the beginning of the program section.
	 */
	List<Macro> macros = new ArrayList<Macro>();
	/**
	 * List of rules in the program section of the module.
	 */
	List<Rule> rules = new ArrayList<Rule>();
	/**
	 * List of action specifications of the module.
	 */
	List<ActionSpecification> specs = new ArrayList<ActionSpecification>();

	// -------------------------------------------------------------
	// Module type and KR language used.
	// -------------------------------------------------------------

	/**
	 * The {@link TYPE} of this module.
	 */
	private TYPE type;
	/**
	 * The KR language used in this module.
	 */
	private final KRInterface kri;

	/**
	 * Creates an (empty) module.
	 *
	 * @param info
	 *            A source info object.
	 */
	public Module(String name, TYPE type, KRInterface kri, SourceInfo info) {
		super(info);
		this.name = name;
		this.type = type;
		this.kri = kri;
	}

	/**
	 * Creates an (empty) module without name or type.
	 *
	 * @param kri
	 *            A KR interface.
	 * @param info
	 *            A source info object.
	 */
	public Module(KRInterface kri, SourceInfo info) {
		super(info);
		this.kri = kri;
	}

	/**
	 * @return The name of this module.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            A name for this {@link Module}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The list of parameters of this module.
	 */
	public List<Term> getParameters() {
		return this.parameters;
	}

	/**
	 * @param parameter
	 *            The parameter that is added to the module.
	 */
	public void setParameters(List<Term> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return The signature of this {@link Module}, i.e., [name]/[nrOfPars]
	 */
	public String getSignature() {
		return this.name + "/" + this.parameters.size();
	}

	/**
	 * @return The focus method that is used when entering this module.
	 */
	public FocusMethod getFocusMethod() {
		return this.focusMethod;
	}

	/**
	 * @param focusMethod
	 *            The focus method that is used when entering this module.
	 */
	public void setFocusMethod(FocusMethod focusMethod) {
		this.focusMethod = focusMethod;
	}

	/**
	 * @return The exit condition of this module.
	 */
	public ExitCondition getExitCondition() {
		return this.exitCondition;
	}

	/**
	 * @param exitCondition
	 *            The exit condition used to check when to exit this module.
	 */
	public void setExitCondition(ExitCondition exitCondition) {
		this.exitCondition = exitCondition;
	}

	/**
	 * @return The files imported by this {@link Module}.
	 */
	public List<File> getImportedFiles() {
		return this.importedFiles;
	}

	/**
	 * @param file
	 *            A file that is imported by this {@link Module}.
	 */
	public void addImportedFile(File file) {
		this.importedFiles.add(file);
	}

	/**
	 * @return The knowledge defined in this {@link Module}.
	 */
	public List<DatabaseFormula> getKnowledge() {
		return this.knowledge;
	}

	/**
	 * @param knowledge
	 *            The knowledge used within this {@link Module}.
	 */
	public void setKnowledge(List<DatabaseFormula> knowledge) {
		this.knowledge = knowledge;
	}

	/**
	 * @return The initial beliefs defined in this {@link Module}.
	 */
	public List<DatabaseFormula> getBeliefs() {
		return this.beliefs;
	}

	/**
	 * @param beliefs
	 *            The beliefs used within this {@link Module}.
	 */
	public void setBeliefs(List<DatabaseFormula> beliefs) {
		this.beliefs = beliefs;
	}

	/**
	 * @return The initial goals defined in this {@link Module}.
	 */
	public List<Query> getGoals() {
		return this.goals;
	}

	/**
	 * @param goals
	 *            The goals used within this {@link Module}.
	 */
	public void setGoals(List<Query> goals) {
		this.goals = goals;
	}

	/**
	 * @return TODO
	 */
	public RuleEvaluationOrder getRuleEvaluationOrder() {
		return this.order;
	}

	/**
	 * @param order
	 *            TODO
	 */
	public void setRuleEvaluationOrder(RuleEvaluationOrder order) {
		this.order = order;
	}

	/**
	 * @return The macros specified at the beginning of the program section.
	 */
	public List<Macro> getMacros() {
		return this.macros;
	}

	/**
	 * @param macros
	 *            The macros specified at the beginning of the program section.
	 */
	public void setMacros(List<Macro> macros) {
		this.macros = macros;
	}

	/**
	 * @return The rules in the program section of this module.
	 */
	public List<Rule> getRules() {
		return this.rules;
	}

	/**
	 * @param rules
	 *            The rules in the program section of this module.
	 */
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	/**
	 * @return The action specifications locally defined within this
	 *         {@link Module}.
	 */
	public List<ActionSpecification> getActionSpecifications() {
		return this.specs;
	}

	/**
	 * @param spec
	 *            The action specifications in the action specification section
	 *            of this module.
	 */
	public void setActionSpecifications(List<ActionSpecification> specs) {
		this.specs = specs;
	}

	/**
	 * @return The {@link TYPE} of this module.
	 */
	public TYPE getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            A type for this {@link Module}.
	 */
	public void setType(TYPE type) {
		this.type = type;
	}

	/**
	 * @return The {@link KRInterface} used in this module.
	 */
	public KRInterface getKRInterface() {
		return this.kri;
	}

	/**
	 * Different from other situations where substitutions are applied, applying
	 * a substitution to a macro means applying it only to the parameters of the
	 * macro and not to any other variables that may occur in the macro's
	 * definition.
	 *
	 * There are two issues here: 1. The variables hidden (not part of the
	 * macro's parameters) should not be instantiated by the substitution; 2.
	 * Any (new) variables introduced by applying the substitution should not be
	 * identical to any of the hidden variables (which would otherwise become
	 * 'visible' again).
	 *
	 * TODO (See also {@link Macro})
	 */
	public Module applySubst(Substitution substitution) {
		List<Term> parameters = new ArrayList<Term>();
		for (Term term : this.parameters) {
			parameters.add(term.applySubst(substitution));
		}
		// TODO instantiate everything else...?? Perhaps 'lazy' application.
		// But how to prevent variable name clashes then?
		// Of course, only applying ground terms simplifies a lot... A module
		// should be closed when executed!

		return new Module(this.name, this.type, this.kri, getSourceInfo());
	}

	/**
	 * @return A string with the name and parameters of this {@link Module}.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append(this.name);

		if (!this.parameters.isEmpty()) {
			str.append("(");
			Iterator<Term> pars = this.parameters.iterator();
			while (pars.hasNext()) {
				str.append(pars.next());
				str.append(pars.hasNext() ? ", " : "");
			}
			str.append(")");
		}

		return str.toString();
	}

	/**
	 * Builds a string representation of this {@link Module}.
	 *
	 * @param linePrefix
	 *            A prefix used to indent parts of a program, e.g., a single
	 *            space or tab.
	 * @param indent
	 *            A unit to increase indentation with, e.g., a single space or
	 *            tab.
	 * @return A string-representation of this module.
	 */
	@Override
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();

		str.append(linePrefix + "<module: " + this + "[focus="
				+ this.focusMethod + ", exit=" + this.exitCondition + "]\n");
		str.append(linePrefix + indent + "<type: " + this.type + ">,\n");

		// Imports
		str.append(linePrefix + indent + "<imports:\n");
		Iterator<File> imports = this.importedFiles.iterator();
		while (imports.hasNext()) {
			str.append(linePrefix + indent + indent + imports.next());
			str.append((imports.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Knowledge
		str.append(linePrefix + indent + "<knowledge:\n");
		Iterator<DatabaseFormula> knows = this.knowledge.iterator();
		while (knows.hasNext()) {
			str.append(linePrefix + indent + indent + knows.next());
			str.append((knows.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Beliefs
		str.append(linePrefix + indent + "<beliefs:\n");
		Iterator<DatabaseFormula> believes = this.beliefs.iterator();
		while (believes.hasNext()) {
			str.append(linePrefix + indent + indent + believes.next());
			str.append((believes.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Goals
		str.append(linePrefix + indent + "<goals:\n");
		Iterator<Query> wants = this.goals.iterator();
		while (wants.hasNext()) {
			str.append(linePrefix + indent + indent + wants.next());
			str.append((wants.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Rule evaluation order
		str.append(linePrefix + indent + "<rule evaluation order: "
				+ this.order + ">,\n");

		// Macros
		str.append(linePrefix + indent + "<macros:\n");
		Iterator<Macro> macro = this.macros.iterator();
		while (macro.hasNext()) {
			str.append(macro.next().toString(linePrefix + indent + indent,
					indent));
			str.append((macro.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Rules
		str.append(linePrefix + indent + "<program rules:\n");
		Iterator<Rule> rule = this.rules.iterator();
		while (rule.hasNext()) {
			str.append(linePrefix + indent + indent + rule.next());
			str.append((rule.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Action specs
		str.append(linePrefix + indent + "<action specifications:\n");
		Iterator<ActionSpecification> spec = this.specs.iterator();
		while (spec.hasNext()) {
			str.append(spec.next().toString(linePrefix + indent + indent,
					indent));
			str.append((spec.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">\n");

		str.append(linePrefix + ">");

		return str.toString();
	}

	/**
	 * Pre- or post-fixes 'module' to name and parameters.
	 *
	 * @return "module <name(parlist)>" or "<name(parlist)> module".
	 */
	public String getNamePhrase() {
		switch (this.type) {
		case EVENT:
		case INIT:
		case MAIN:
			return this.name + " module";
		case PROGRAM:
			return this.name + " program";
		case USERDEF:
			return "module " + this.name;
		default:
			return "";
		}
	}

	// -------------------------------------------------------------
	// Enum classes for TYPE, FocusMethod, and ExitCondition
	// -------------------------------------------------------------

	/**
	 * Types for distinguishing built-in modules from user-defined modules.
	 * <p>
	 * The options are:
	 * <ul>
	 * <li>{@link #INIT}: The <code>init</code> module.</li>
	 * <li>{@link #MAIN}: The <code>main</code> module.</li>
	 * <li>{@link #EVENT}: The <code>event</code> module.</li>
	 * <li>{@link #ANONYMOUS}: An anonymous module, derived from
	 * <code>{ ... }</code> blocks.</li>
	 * <li>{@link #USERDEF}: A user-defined module.</li>
	 * </ul>
	 * </p>
	 */
	public enum TYPE {
		PROGRAM("program"), INIT("init"), MAIN("main"), EVENT("event"), ANONYMOUS(
				"anonymous"), USERDEF("user-defined");

		private String displayName;

		private TYPE(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return this.displayName;
		}

		@Override
		public String toString() {
			return this.displayName;
		}
	}

	/**
	 * The available options for creating an attention set associated with a
	 * module.
	 *
	 * <p>
	 * The options are:
	 * <ul>
	 * <li>{@link #FILTER}: goal from rule condition that acts like filter is
	 * inserted into the module's attention set.</li>
	 * <li>{@link #SELECT}: one of the agent's current goals that satisfies the
	 * rule condition is inserted into the module's attention set.</li>
	 * <li>{@link #NONE}: no new attention set associated with the module is
	 * created.</li>
	 * <li>{@link #NEW}: creates a new and empty attention set.</li>
	 * </ul>
	 * </p>
	 */
	public enum FocusMethod {
		/**
		 * After focusing, the agents gets a single goal for each of the
		 * positive {@link GoalLiteral} and {@link AGoalLiteral}s in the
		 * instantiated precondition of the rule that focuses on the module.
		 */
		FILTER,
		/**
		 * After focusing, the agent gets a single goal from the current
		 * attention set, which validates the 'context' of the module.
		 */
		SELECT,
		/**
		 * After focusing, the agent will have the same attention set as before.
		 * The same goal base is re-used. Goals in the <code>goals { }</code>
		 * section are simply added to that attention set. This is the default
		 * value.
		 */
		NONE,
		/**
		 * After focusing, the agent will have no goals in its attention set,
		 * aside from those defined in the Module's <code>goals { }</code>
		 * -section.
		 */
		NEW;
	}

	/**
	 * The available options for the exit condition of a module.
	 *
	 * <p>
	 * This condition is evaluated each time that the rules of the module have
	 * been evaluated (according to the rule evaluation order associated with
	 * the module). That is, <i>after</i> the rules have been evaluated, it is
	 * checked whether the module should be exited.
	 * </p>
	 *
	 * <p>
	 * Note that modules can also be exited using the <code>exit-module</code>
	 * action.
	 * </p>
	 */
	public static enum ExitCondition {
		/**
		 * The module should be exited once there are no goals left in the
		 * current attention set at the end of an evaluation step.
		 */
		NOGOALS,
		/**
		 * The module should be exited once an evaluation step produced no
		 * executed actions.
		 */
		NOACTION,
		/**
		 * The module should always be exited after an evaluation step.<br>
		 * This is the default value.
		 */
		ALWAYS,
		/**
		 * The module never exits. This is the default for the main program. If
		 * the main program exits, the agent dies.
		 */
		NEVER;
	}

	/**
	 * The different orders in which the rules of an {@link RuleSet} can be
	 * evaluated.
	 */
	public enum RuleEvaluationOrder {
		/**
		 * Of the first rule with a viable instance, one random instance will be
		 * applied. A forall-do rule counts as one instance.
		 */
		LINEAR,
		/**
		 * Of all viable rule instances, one will be applied. A forall-do rule
		 * counts as one instance.
		 */
		RANDOM,
		/**
		 * All rules will be applied, from top to bottom. Instances from within
		 * a single rule are applied in random order. Only one instance from
		 * each if-then rule will be applied.
		 */
		LINEARALL,
		/**
		 * All viable rule instances will be applied, but in random order. Only
		 * one instance from each if-then rule will be applied.
		 */
		RANDOMALL,
		/**
		 * The first rule (in linear order) that is applicable will be applied
		 * and only the options of this rule are generated. The option that is
		 * selected depends on ongoing learning.
		 */
		LINEARADAPTIVE,
		/**
		 * All rules will be applied in linear order and all options generated.
		 * The option that is selected depends on ongoing learning.
		 */
		ADAPTIVE;

		/**
		 * @return {@code true} iff this {@link RuleEvaluationOrder} is
		 *         {@link #LINEAR} or {@link #LINEAR}.
		 */
		public boolean isLinear() {
			return this == LINEAR || this == LINEARALL || this == ADAPTIVE;
		}

		/**
		 * @return {@code true} iff this {@link RuleEvaluationOrder} is
		 *         {@link #LINEARALL} or {@link #RANDOMALL}.
		 */
		public boolean applyAll() {
			return this == LINEARALL || this == RANDOMALL || this == ADAPTIVE;
		}

		/**
		 * @return {@code true} iff this {@link RuleEvaluationOrder} is
		 *         {@link #ADAPTIVE}
		 */
		public boolean isAdaptive() {
			return this == ADAPTIVE;
		}
	}

}