/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.naming.NamingException;

import krTools.KRInterface;
import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.agent.AgentValidator;
import languageTools.errors.agent.AgentError;
import languageTools.parser.GOAL;
import languageTools.program.Program;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.Rule;
import languageTools.program.agent.selector.Selector.SelectorType;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.MacroSymbol;

/**
 * <p>
 * A GOAL agent program.
 * </p>
 *
 * <p>
 * An agent program consists of a set of {@link Module}s.
 * </p>
 * <p>
 * Use {@link AgentValidator} to create an agent program from a text file with
 * extension 'goal'.
 * </p>
 *
 * @author Koen Hindriks
 */
public class AgentProgram extends Program {
	/**
	 * An agent program is a set of modules, either imported or defined in the
	 * agent program file itself.
	 */
	private final List<Module> modules = new ArrayList<Module>();
	/**
	 * <p>
	 * List of imported module files.
	 * </p>
	 *
	 * <p>
	 * It should be possible to resolve the location of module files that are
	 * imported relative to the source file used to construct this agent program
	 * (alternatively, the path should be absolute).
	 * </p>
	 */
	private final List<File> importedFiles = new ArrayList<File>();

	/**
	 * The knowledge representation language used in this agent program to
	 * represent, a.o., the knowledge, beliefs, goals, and action pre- and
	 * post-conditions.
	 */
	private KRInterface krInterface = null;

	/**
	 * This info is filled in by the validator and may stay empty
	 */
	private final SymbolTable macroSymbols = new SymbolTable();

	/**
	 * Creates a new (empty) agent program.
	 *
	 * @param agentfile
	 *            The source file used to construct this agent program.
	 */
	public AgentProgram(SourceInfo info) {
		super(info);
	}

	/**
	 * @return List of the modules defined in the program file
	 */
	public List<Module> getModules() {
		return this.modules;
	}

	/**
	 * Adds a module to this agent program.
	 *
	 * @param module
	 *            The {@link Module} that is added.
	 */
	public void addModule(Module module) {
		this.modules.add(module);
	}

	/**
	 * @return A list of module files that are imported in this agent program.
	 */
	public List<File> getImportedModules() {
		return this.importedFiles;
	}

	/**
	 * Adds a module file to the list of imported module files.
	 *
	 * @param file
	 *            The module file that is added.
	 */
	public void addImportedModule(File file) {
		this.importedFiles.add(file);
	}

	/**
	 * @return The {@link KRInterface} used in the agent program.
	 */
	public KRInterface getKRInterface() {
		return this.krInterface;
	}

	/**
	 * @param krInterface
	 *            The {@link KRInterface} that is used in the agent program.
	 */
	public void setKRInterface(KRInterface krInterface) {
		this.krInterface = krInterface;
	}

	/**
	 * Checks whether the agent program performs mental model queries.
	 *
	 * <p>
	 * An agent program uses mental model queries if it has a mental state
	 * condition with an agent expression type other than self or this (i.e.,
	 * variable, quantor, or constant).
	 * </p>
	 *
	 * @return {@code true} iff the agent program performs mental model queries.
	 */
	public boolean usesMentalModels() {
		for (Module m : getModules()) {
			if (m.getRules() == null) {
				continue;
			}
			for (Rule rule : m.getRules()) {
				for (MentalFormula formula : rule.getCondition()
						.getSubFormulas()) {
					if (formula instanceof MentalLiteral) {
						MentalLiteral literal = (MentalLiteral) formula;
						if (literal.getSelector().getType() != SelectorType.SELF
								&& literal.getSelector().getType() != SelectorType.THIS) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param tokenIndex
	 *            Integer index of parser token.
	 * @return String with the name of the token.
	 */
	public static String getTokenName(int tokenIndex) {
		return GOAL.tokenNames[tokenIndex].replaceAll("'", "");
	}

	/**
	 * @return A string with the imported file names, module names, and KR
	 *         interface name.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		for (File file : this.importedFiles) {
			str.append("import: " + file + "\n");
		}
		for (Module module : this.modules) {
			str.append("module: " + module + "\n");
		}
		str.append("KR interface: " + this.krInterface);

		return str.toString();
	}

	/**
	 * Builds a string representation of this {@link AgentProgram}.
	 *
	 * @param linePrefix
	 *            A prefix used to indent parts of a program, e.g., a single
	 *            space or tab.
	 * @param indent
	 *            A unit to increase indentation with, e.g., a single space or
	 *            tab.
	 * @return A string-representation of this agent program.
	 */
	@Override
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();

		str.append(linePrefix + "<agent program:\n");

		// Imports
		str.append(linePrefix + indent + "<imports:\n");
		Iterator<File> imports = this.importedFiles.iterator();
		while (imports.hasNext()) {
			str.append(linePrefix + indent + indent + imports.next());
			str.append((imports.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + indent + ">,\n");

		// Modules
		str.append(linePrefix + indent + "<modules:\n");
		Iterator<Module> mods = this.modules.iterator();
		while (mods.hasNext()) {
			str.append(mods.next().toString(linePrefix + indent + indent,
					indent));
			str.append((mods.hasNext() ? ",\n" : "\n"));
		}
		str.append(linePrefix + ">,\n");

		// KR interface
		str.append(linePrefix + "<KR interface: " + this.krInterface + ">\n");

		str.append(">");

		return str.toString();
	}

	/**
	 * @return All knowledge specified in all modules.
	 */
	public List<DatabaseFormula> getAllKnowledge() {
		List<DatabaseFormula> knowledge = new LinkedList<>();
		for (Module module : this.modules) {
			knowledge.addAll(module.getKnowledge());
		}
		return knowledge;
	}

	/**
	 * @return All beliefs specified in all modules.
	 */
	public List<DatabaseFormula> getAllBeliefs() {
		List<DatabaseFormula> beliefs = new LinkedList<>();
		for (Module module : this.modules) {
			beliefs.addAll(module.getBeliefs());
		}
		return beliefs;
	}

	/**
	 * @return All goals specified in all modules.
	 */
	public List<Query> getAllGoals() {
		List<Query> goals = new LinkedList<>();
		for (Module module : this.modules) {
			goals.addAll(module.getGoals());
		}
		return goals;
	}

	/**
	 * @return All action specifications in all modules.
	 */
	public List<ActionSpecification> getAllActionSpecs() {
		List<ActionSpecification> specs = new LinkedList<>();
		for (Module module : this.modules) {
			specs.addAll(module.getActionSpecifications());
		}
		return specs;
	}

	/**
	 * @return All macros in all modules.
	 */
	public List<Macro> getAllMacros() {
		List<Macro> specs = new LinkedList<>();
		for (Module module : this.modules) {
			specs.addAll(module.getMacros());
		}
		return specs;
	}

	/**
	 * Resolves all the macros in given {@link MentalStateCondition}.
	 * 
	 * @param msc
	 *            the {@link MentalStateCondition} to resolve.
	 * @return set of macro labels occuring in the msc.
	 * @throws NamingException
	 * @throws ParserException 
	 *             if the given macro can not be resolved (it's undefined) The
	 *             message then will contain the signature of the undefined
	 *             macro, and the sourceinfo will be set properly.
	 */
	public Set<String> resolve(MentalStateCondition msc) throws ParserException {
		Set<String> macroLabels = new HashSet<>();

		for (MentalFormula formula : msc.getSubFormulas()) {
			if (formula instanceof Macro) {
				macroLabels.add(resolve((Macro) formula));
			}
		}

		return macroLabels;

	}

	/**
	 * the reference to the definition of the macro.
	 * 
	 * @param {@link Macro}.
	 * @return signature of the macro
	 * @throws ParserException
	 *             if the given macro can not be resolved (it's undefined) The
	 *             message then will contain the signature of the undefined
	 *             macro, and the sourceinfo will be set properly.
	 */
	public String resolve(Macro formula) throws ParserException {
		String signature = ((Macro) formula).getSignature();
		MacroSymbol symbol = (MacroSymbol) this.macroSymbols.resolve(signature);
		if (symbol == null) {
			throw new ParserException(signature, formula.getSourceInfo());
		} else {
			// Assumes that formal parameters are all variables
			// TODO: standardize variables in definition apart from
			// other variables that occur in rule condition
			// TODO #3430. This is quick fix.
			MacroExpression spec = new MacroExpression(symbol.getMacro(),
					krInterface);
			MacroExpression call = new MacroExpression(((Macro) formula),
					krInterface);

			Substitution uniqueSub = makeTermVarsUnique(spec.getFreeVar(),
					call.getFreeVar());
			MacroExpression fixedSpec = (MacroExpression) spec
					.applySubst(uniqueSub);

			Substitution substitution = fixedSpec.mgu(call);
			MacroExpression fixedMacroExp = (MacroExpression) fixedSpec
					.applySubst(substitution);
			MentalStateCondition instantiatedDf = fixedMacroExp.getMacro()
					.getDefinition();
			((Macro) formula).setDefinition(instantiatedDf);
		}

		return signature;
	}

	/**
	 * Get a substitution that we can apply to term, such that it will not use
	 * given varsInUse.
	 * 
	 * @param varsToBeMadeUnique
	 *            vars that have to be renamed if occuring iin the varsInUse
	 *            list.
	 * @param varsInUse
	 *            set of {@link Var}s that are in use
	 * @return
	 */
	public Substitution makeTermVarsUnique(Set<Var> varsToBeMadeUnique,
			Set<Var> varsInUse) {
		Set<Var> varsToRename = sharedVariables(varsToBeMadeUnique, varsInUse);
		Map<Var, Term> subst = new HashMap<Var, Term>();
		for (Var var : varsToRename) {
			subst.put(var, var.getVariant(varsInUse));
		}
		return krInterface.getSubstitution(subst);
	}

	private Set<Var> sharedVariables(Set<Var> t1, Set<Var> varsInUse) {
		Set<Var> sharedVars = new HashSet<Var>();
		sharedVars.addAll(t1);
		sharedVars.retainAll(varsInUse);
		return sharedVars;
	}

	/**
	 * get the macros {@link SymbolTable}.
	 * 
	 * @return the macros {@link SymbolTable}
	 */
	public SymbolTable getMacros() {
		return macroSymbols;
	}

}