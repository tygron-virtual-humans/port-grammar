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

package languageTools.analyzer.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import krTools.errors.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.analyzer.agent.AgentValidatorSecondPass;
import languageTools.errors.agent.AgentError;
import languageTools.errors.agent.AgentWarning;
import languageTools.parser.GOAL;
import languageTools.program.agent.ActionSpecification;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.TYPE;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.SendOnceAction;
import languageTools.program.agent.actions.UserSpecAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.rules.Rule;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;

public class ModuleValidatorSecondPass {

	/**
	 * Reserved keywords.
	 */
	private final static String AGENT = "agent/1";
	private final static String ME = "me/1";
	private final static String PERCEPT = "percept/1";
	private final static String RECEIVED = "received/2";
	private final static String SENT = "sent/2";

	/**
	 * First pass over parse tree.
	 */
	private final ModuleValidator firstPass;

	/**
	 * Program that is outcome of first pass.
	 */
	private final Module program;

	/**
	 * Symbol tables built during first pass.
	 */
	private final SymbolTable actionSymbols;
	private final SymbolTable macroSymbols;

	private final Set<String> actionLabelsUsed = new HashSet<>();
	private final Set<String> macroLabelsUsed = new HashSet<>();

	/**
	 * The knowledge specified in knowledge sections in the agent program.
	 */
	private final Set<DatabaseFormula> knowledge = new HashSet<DatabaseFormula>();
	/**
	 * The beliefs specified in belief sections in the agent program.
	 */
	private final Set<DatabaseFormula> beliefs = new HashSet<DatabaseFormula>();
	/**
	 * The beliefs that are added by insert and delete actions and by
	 * post-conditions.
	 */
	private final Set<DatabaseFormula> dynamicBeliefs = new HashSet<DatabaseFormula>();
	/**
	 * The queries on the belief and knowledge base that occur in the agent
	 * program, including belief, a-goal, and goal-a literals as well as
	 * preconditions.
	 */
	private final Set<Query> beliefQueries = new HashSet<Query>();
	/**
	 * Goals specified in goal sections of the agent program and the goals that
	 * are adopted by means of adopt actions. Both sets below include the same
	 * goals, but the first represents goals as queries and the second as
	 * database formulas.
	 */
	private final Set<Query> goals = new HashSet<Query>();
	private final Set<DatabaseFormula> goalDbfs = new HashSet<DatabaseFormula>();
	/**
	 * The queries on the goal base that occur in the agent program, including
	 * goal, a-goal, and goal-a literals.
	 */
	private final Set<Query> goalQueries = new HashSet<>();

	/**
	 * In the second pass, references in the given agent program are resolved
	 * and related semantic checks are performed.
	 *
	 * <p>
	 * Assumes that the first pass has been performed and the resulting agent
	 * program does not contain any {@code null} references.
	 * </p>
	 * <p>
	 * Any validation errors or warnings are reported.
	 * </p>
	 *
	 * @param firstPass
	 *            The validator object that executed the first pass.
	 */
	protected ModuleValidatorSecondPass(ModuleValidator firstPass) {
		this.firstPass = firstPass;
		this.program = firstPass.getProgram();
		this.actionSymbols = firstPass.getActionSymbols();
		this.macroSymbols = firstPass.getMacroSymbols();
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	protected void validate() {

		// Collect all info needed for validation
		visitModule(this.program);
		checkVariablesBound(this.program, new HashSet<Term>());

		// Report unused action and module definitions
		Set<String> actionsDefined = this.actionSymbols.getNames();
		// Remove labels that are used
		actionsDefined.removeAll(this.actionLabelsUsed);
		// Remove built-in modules
		actionsDefined.remove(new Module(AgentProgram.getTokenName(GOAL.MAIN),
				TYPE.MAIN, null, null).getSignature());
		actionsDefined.remove(new Module(AgentProgram.getTokenName(GOAL.EVENT),
				TYPE.EVENT, null, null).getSignature());
		actionsDefined.remove(new Module(AgentProgram.getTokenName(GOAL.INIT),
				TYPE.INIT, null, null).getSignature());
		// Report unused
		for (String df : actionsDefined) {
			Symbol symbol = this.actionSymbols.resolve(df);
			if (symbol instanceof ModuleSymbol) {
				this.firstPass.reportWarning(AgentWarning.MODULE_NEVER_USED,
						symbol.getSourceInfo(), df);
			} else {
				this.firstPass.reportWarning(AgentWarning.ACTION_NEVER_USED,
						symbol.getSourceInfo(), df);
			}
		}

		// Report unused macro definitions
		Set<String> macrosDefined = this.macroSymbols.getNames();
		// Remove labels that are used
		macrosDefined.removeAll(this.macroLabelsUsed);
		// Report unused
		for (String df : macrosDefined) {
			Symbol symbol = this.macroSymbols.resolve(df);
			this.firstPass.reportWarning(AgentWarning.MACRO_NEVER_USED,
					symbol.getSourceInfo(), df);
		}

		// report unused and undefined KR expressions
		validateKR();
	}

	/**
	 * Identify unused and undefined KR expressions.
	 */
	private void validateKR() {
		// Collect undefined and unused KR expressions
		Set<DatabaseFormula> tempDbfs;
		Set<Query> tempQueries;

		tempDbfs = new HashSet<DatabaseFormula>(this.knowledge);
		tempDbfs.addAll(this.beliefs);

		Set<DatabaseFormula> knowledgeDfNotUsedInKB = this.program
				.getKRInterface().getUnused(this.knowledge,
						new HashSet<Query>());
		Set<DatabaseFormula> knowledgeDfNotUsed;

		tempDbfs.addAll(this.dynamicBeliefs);
		tempQueries = new HashSet<Query>(this.beliefQueries);
		tempQueries.addAll(this.goals);

		Set<DatabaseFormula> unusedKnowledgeOrBeliefs = this.program
				.getKRInterface().getUnused(tempDbfs, tempQueries);
		knowledgeDfNotUsed = new HashSet<DatabaseFormula>(
				unusedKnowledgeOrBeliefs);
		knowledgeDfNotUsed.retainAll(knowledgeDfNotUsedInKB);
		Set<Query> undefinedBeliefQueries = this.program.getKRInterface()
				.getUndefined(tempDbfs, this.beliefQueries);

		Set<Query> unachievableGoals = this.program.getKRInterface()
				.getUndefined(tempDbfs, this.goals);
		// Remove undefined knowledge and belief queries again
		unachievableGoals.removeAll(undefinedBeliefQueries);

		tempDbfs = new HashSet<DatabaseFormula>(this.knowledge);
		tempDbfs.addAll(this.goalDbfs);
		tempQueries = new HashSet<Query>(this.goalQueries);
		tempQueries.addAll(this.goals);
		Set<DatabaseFormula> unusedGoals = this.program.getKRInterface()
				.getUnused(tempDbfs, tempQueries);
		knowledgeDfNotUsed.retainAll(unusedGoals);
		// Remove knowledge definitions (are not considered to be goals)
		unusedGoals.removeAll(knowledgeDfNotUsedInKB);

		Set<Query> undefinedGoalQueries = this.program.getKRInterface()
				.getUndefined(tempDbfs, this.goalQueries);
		// Remove queries that are undefined knowledge queries
		undefinedGoalQueries.removeAll(this.program.getKRInterface()
				.getUndefined(this.knowledge, new HashSet<Query>()));

		// Reserved keywords that should not be reported, e.g., percept/1,
		// received/2
		Set<String> reserved = new HashSet<>();
		reserved.add(AGENT);
		reserved.add(ME);
		reserved.add(PERCEPT);
		reserved.add(RECEIVED);
		reserved.add(SENT);

		// Report undefined and unused KR expressions
		for (DatabaseFormula dbf : unusedKnowledgeOrBeliefs) {
			// do not report knowledge definitions that are used by goals or
			// goal queries
			if (knowledgeDfNotUsedInKB.contains(dbf)
					&& !knowledgeDfNotUsed.contains(dbf)) {
				continue;
			}
			if (!reserved.contains(dbf.getSignature())) {
				this.firstPass.reportWarning(
						AgentWarning.KR_KNOWLEDGE_OR_BELIEF_NEVER_USED,
						dbf.getSourceInfo(), dbf.getSignature());
			}
		}
		for (DatabaseFormula dbf : unusedGoals) {
			// do not report knowledge definitions that are used by beliefs or
			// belief queries
			if (knowledgeDfNotUsedInKB.contains(dbf)
					&& !knowledgeDfNotUsed.contains(dbf)) {
				continue;
			}
			if (!reserved.contains(dbf.getSignature())) {
				this.firstPass.reportWarning(AgentWarning.KR_GOAL_NEVER_USED,
						dbf.getSourceInfo(), dbf.getSignature());
			}
		}
		for (Query query : undefinedBeliefQueries) {
			if (!reserved.contains(query.getSignature())) {
				this.firstPass.reportError(
						AgentError.KR_BELIEF_QUERIED_NEVER_DEFINED,
						query.getSourceInfo(), query.getSignature());
			}
		}
		for (Query query : undefinedGoalQueries) {
			if (!reserved.contains(query.getSignature())) {
				this.firstPass.reportError(
						AgentError.KR_GOAL_QUERIED_NEVER_DEFINED,
						query.getSourceInfo(), query.getSignature());
			}
		}
		for (Query query : unachievableGoals) {
			if (!reserved.contains(query.getSignature())) {
				this.firstPass.reportWarning(
						AgentWarning.GOAL_DOES_NOT_MATCH_BELIEF,
						query.getSourceInfo(), query.getSignature());
			}
		}
	}

	/**
	 * Retrieves all action labels, macros, knowledge, belief and goal queries,
	 * and belief and goal dbfs,
	 *
	 * @param module
	 *            Module from which to retrieve information.
	 */
	private void visitModule(Module module) {
		this.actionLabelsUsed.addAll(resolveModuleActionRefs(module));
		this.macroLabelsUsed.addAll(resolveModuleMacroRefs(module));

		// extract relevant sets of database formulas and queries from module
		this.knowledge.addAll(module.getKnowledge()); // exploits fact that we
		// don't allow module
		// declarations within
		// modules
		this.beliefs.addAll(module.getBeliefs());
		this.dynamicBeliefs.addAll(getInsertedBeliefs(module));
		this.beliefQueries.addAll(getBeliefQueries(module));
		this.goals.addAll(module.getGoals());
		this.goals.addAll(getAdoptedGoals(module));
		this.goalDbfs.addAll(getGoalDfs(module));
		this.goalQueries.addAll(getGoalQueries(module));
	}

	/**
	 * Resolve references to action specifications or modules.
	 *
	 * @param module
	 *            Module in which action and module references are resolved.
	 * @return Set of action and module labels that were used in module.
	 */
	private Set<String> resolveModuleActionRefs(Module module) {
		Set<String> actionLabelsUsed = new HashSet<>();
		for (Rule rule : module.getRules()) {
			actionLabelsUsed.addAll(resolveActionReferences(rule.getAction()));
		}
		return actionLabelsUsed;
	}

	/**
	 * Resolve action labels used in the program.
	 *
	 * @param actions
	 *            {@link ActionCombo} with list of actions used.
	 * @return Set of string signatures of used actions and modules.
	 */
	private Set<String> resolveActionReferences(ActionCombo actions) {
		Set<String> actionLabelsUsed = new HashSet<>();
		List<Action<?>> resolved = new ArrayList<>();
		for (Action<?> action : actions.getActions()) {
			if (action instanceof UserSpecOrModuleCall) {
				UserSpecOrModuleCall call = (UserSpecOrModuleCall) action;
				actionLabelsUsed.add(action.getSignature());
				// resolve reference
				Symbol symbol = this.actionSymbols.resolve(action
						.getSignature());
				if (symbol != null) {
					if (symbol instanceof ModuleSymbol) {
						Module target = ((ModuleSymbol) symbol).getModule();
						resolved.add(new ModuleCallAction(target, call
								.getParameters(), action.getSourceInfo(),
								this.program.getKRInterface()));
					} else { // must be ActionSymbol
						ActionSpecification spec = ((ActionSymbol) symbol)
								.getActionSpecification();
						Substitution uniqueSub = AgentValidatorSecondPass
								.makeTermVarsUnique(AgentValidatorSecondPass
										.getFreeVars(spec.getAction()
												.getParameters()), call
												.getFreeVar(), this.program
												.getKRInterface());
						spec = spec.applySubst(uniqueSub);
						// find the substi that matches the call to the spec.
						// This substi is now used to bring the spec into the
						// form fitting the call.
						Substitution unifier = spec.getAction().mgu(action);
						if (unifier != null) {
							UserSpecAction specAction = spec
									.applySubst(unifier).getAction();
							UserSpecAction fixed = new UserSpecAction(
									specAction.getName(),
									specAction.getParameters(),
									specAction.isExternal(),
									((MentalLiteral) specAction
											.getPrecondition().getSubFormulas()
											.get(0)).getFormula(),
											specAction.getPostcondition(),
											call.getSourceInfo(),
											specAction.getKRInterface());
							resolved.add(fixed);
						} else {
							this.firstPass.reportError(
									AgentError.ACTION_DOES_NOT_MATCH,
									action.getSourceInfo(),
									action.getSignature());
						}
					}
				} else {
					this.firstPass.reportError(
							AgentError.ACTION_USED_NEVER_DEFINED,
							action.getSourceInfo(), action.getSignature());
				}
			} else if (action instanceof ModuleCallAction) {
				// must be anonymous module
				actionLabelsUsed
				.addAll(resolveModuleActionRefs(((ModuleCallAction) action)
						.getTarget()));
				resolved.add(action);
			} else {
				resolved.add(action);
			}
		}

		// Substitute resolved references
		actions.setActions(resolved);

		return actionLabelsUsed;
	}

	/**
	 * Resolves references to macros. Returns set of macro signatures.
	 *
	 * @param module
	 *            Module in which macro references are resolved.
	 * @return Set of macro signatures that are used in module.
	 */
	private Set<String> resolveModuleMacroRefs(Module module) {
		Set<String> macroLabels = new HashSet<>();
		for (Rule rule : module.getRules()) {
			try {
				macroLabels.addAll(module.resolve(rule.getCondition()));
			} catch (ParserException e) {
				/*
				 * HACK how to get the correct source info here? We can't attach
				 * the original Macro to the exception.
				 */
				this.firstPass.reportError(AgentError.MACRO_NOT_DEFINED,
						e.getSourceInfo(), e.getMessage());

			}

			// Resolve references to macros
			if (!rule.getAction().getActions().isEmpty()
					&& rule.getAction().getActions().get(0) instanceof ModuleCallAction) {
				if (((ModuleCallAction) rule.getAction().getActions().get(0))
						.getTarget().getType() == TYPE.ANONYMOUS) {
					macroLabels
					.addAll(resolveModuleMacroRefs(((ModuleCallAction) rule
							.getAction().getActions().get(0))
							.getTarget()));
				}
			}
		}
		return macroLabels;
	}

	/**
	 * Reports error if variables in a rule have not been bound. FIXME Duplicate
	 * code, #3434
	 *
	 * @param module
	 *            Module with rules to be checked.
	 */
	private void checkVariablesBound(Module module, Set<Term> scope) {
		Set<Term> localScope;
		if (module.getType() != TYPE.ANONYMOUS) {
			// reset scope
			localScope = new HashSet<Term>();
			for (Term term : module.getParameters()) {
				localScope.add(term);
			}
		} else {
			localScope = new HashSet<Term>(scope);
		}
		for (Rule rule : module.getRules()) {
			// Set up new scope that also includes variables bound by rule
			// condition
			Set<Term> newscope = new HashSet<Term>(localScope);
			newscope.addAll(rule.getCondition().getFreeVar());
			for (Action<?> action : rule.getAction().getActions()) {
				Set<Var> unbound = new HashSet<>();
				Set<Var> free = new HashSet<Var>();
				if (action instanceof ModuleCallAction) {
					checkVariablesBound(
							((ModuleCallAction) action).getTarget(), newscope);
				} else if (action instanceof SendAction
						|| action instanceof SendOnceAction) {
					if (AgentValidatorSecondPass.getSendMood(action) != SentenceMood.INTERROGATIVE) {
						free = action.getFreeVar();
					}
				} else {
					free = action.getFreeVar();
				}
				unbound.addAll(free);
				unbound.removeAll(newscope);
				if (!unbound.isEmpty()) {
					this.firstPass.reportError(
							AgentError.RULE_VARIABLE_NOT_BOUND,
							action.getSourceInfo(),
							this.firstPass.prettyPrintSet(unbound));
				}
			}
		}
		for (DatabaseFormula formula : module.getBeliefs()) {
			Set<Var> unbound = new HashSet<>(formula.getFreeVar());
			unbound.removeAll(localScope);
			if (!unbound.isEmpty()) {
				this.firstPass.reportError(
						AgentError.BELIEF_UNINSTANTIATED_VARIABLE,
						formula.getSourceInfo(),
						this.firstPass.prettyPrintSet(unbound),
						formula.toString());
			}
		}
		for (Query formula : module.getGoals()) {
			Set<Var> unbound = new HashSet<>(formula.getFreeVar());
			unbound.removeAll(localScope);
			if (!unbound.isEmpty()) {
				this.firstPass.reportError(
						AgentError.GOAL_UNINSTANTIATED_VARIABLE,
						formula.getSourceInfo(),
						this.firstPass.prettyPrintSet(unbound),
						formula.toString());
			}
		}
	}

	/**
	 * Extracts beliefs that are dynamically inserted into the belief base from
	 * the module. Collects these from insert and delete actions and from
	 * post-conditions.
	 *
	 * @param module
	 *            Module from which beliefs are extracted.
	 * @return Set of {@link DatabaseFormula} extracted from module.
	 */
	private Set<DatabaseFormula> getInsertedBeliefs(Module module) {
		Set<DatabaseFormula> dbfs = new HashSet<>();
		// Add beliefs inserted by inserts and deletes
		for (Rule rule : module.getRules()) {
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof InsertAction) {
					dbfs.addAll(((InsertAction) action).getUpdate()
							.getAddList());
				}
				if (action instanceof DeleteAction) {
					dbfs.addAll(((DeleteAction) action).getUpdate()
							.getDeleteList());
				}
				if (action instanceof ModuleCallAction) {
					dbfs.addAll(getInsertedBeliefs(((ModuleCallAction) action)
							.getTarget()));
				}
			}
		}
		// Add beliefs inserted by post-conditions
		for (ActionSpecification spec : module.getActionSpecifications()) {
			dbfs.addAll(spec.getPostCondition().getAddList());
		}
		return dbfs;
	}

	/**
	 * Extracts all queries on the belief base, including belief, a-goal, and
	 * goal-a literals as well as preconditions.
	 *
	 * @param module
	 *            Module from which all queries are extracted.
	 * @return Set of {@link Query} extracted from module.
	 */
	private Set<Query> getBeliefQueries(Module module) {
		Set<Query> queries = new HashSet<>();
		// Add queries used in rule conditions
		// First collect all literals as well as queries from submodules
		Set<MentalLiteral> literals = new HashSet<MentalLiteral>();
		for (Rule rule : module.getRules()) {
			literals.addAll(AgentValidatorSecondPass.getBeliefLiterals(rule
					.getCondition()));
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof ModuleCallAction) {
					queries.addAll(getBeliefQueries(((ModuleCallAction) action)
							.getTarget()));
				}
			}
		}
		for (MentalLiteral literal : literals) {
			queries.add(literal.getFormula());
		}
		// Add pre-conditions
		for (ActionSpecification spec : module.getActionSpecifications()) {
			for (MentalLiteral literal : AgentValidatorSecondPass
					.getBeliefLiterals(spec.getPreCondition())) {
				queries.add(literal.getFormula());
			}
		}
		return queries;
	}

	/**
	 * Retrieves all goals that are adopted in the module by adopt actions.
	 *
	 * @param module
	 *            Module from which adopted goals are extracted.
	 * @return Set of {@link Query}s extracted from module.
	 */
	private Set<Query> getAdoptedGoals(Module module) {
		Set<Query> queries = new HashSet<Query>();
		// Add updates in adopts
		for (Rule rule : module.getRules()) {
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof AdoptAction) {
					for (DatabaseFormula dbf : ((AdoptAction) action)
							.getUpdate().getAddList()) {
						queries.add(dbf.toQuery());
					}
				}
				if (action instanceof ModuleCallAction) {
					queries.addAll(getAdoptedGoals(((ModuleCallAction) action)
							.getTarget()));
				}
			}
		}
		return queries;
	}

	/**
	 * @param module
	 *            Module from which all DBFs are extracted.
	 * @return DBFs extracted from module.
	 */
	private Set<DatabaseFormula> getGoalDfs(Module module) {
		Set<DatabaseFormula> dbfs = new HashSet<>();
		// Add goals in goal section
		for (Query query : module.getGoals()) {
			dbfs.addAll(query.toUpdate().getAddList());
		}
		// Add updates in adopts
		for (Rule rule : module.getRules()) {
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof AdoptAction) {
					dbfs.addAll(((AdoptAction) action).getUpdate().getAddList());
				}
				if (action instanceof ModuleCallAction) {
					dbfs.addAll(getGoalDfs(((ModuleCallAction) action)
							.getTarget()));
				}
			}
		}
		return dbfs;
	}

	/**
	 * Extracts all queries on the goal base from the module.
	 *
	 * @param module
	 *            Module from which all queries are extracted.
	 * @return Set of {@link Query}s extracted from module.
	 */
	private Set<Query> getGoalQueries(Module module) {
		Set<Query> queries = new HashSet<>();
		// Add queries used in rule conditions
		// First collect all literals as well as queries from submodules
		Set<MentalLiteral> literals = new HashSet<>();
		for (Rule rule : module.getRules()) {
			literals.addAll(AgentValidatorSecondPass.getGoalLiterals(rule
					.getCondition()));
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof ModuleCallAction) {
					queries.addAll(getGoalQueries(((ModuleCallAction) action)
							.getTarget()));
				}
			}
		}
		for (MentalLiteral literal : literals) {
			queries.add(literal.getFormula());
		}
		return queries;
	}
}
