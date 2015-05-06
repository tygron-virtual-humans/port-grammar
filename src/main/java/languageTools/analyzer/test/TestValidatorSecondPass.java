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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import languageTools.analyzer.agent.AgentValidatorSecondPass;
import languageTools.errors.agent.AgentError;
import languageTools.program.agent.ActionSpecification;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.Rule;
import languageTools.program.test.AgentTest;
import languageTools.program.test.UnitTest;
import languageTools.program.test.testsection.TestSection;

/**
 * DOC
 *
 * Macros are connected to their definitions.
 */
public class TestValidatorSecondPass {
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
	private final TestValidator firstPass;
	/**
	 * Program that is outcome of first pass.
	 */
	private final UnitTest program;

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
	 * Modules that have been processed by getInsertedBeliefs
	 */
	private final Set<Module> processed = new HashSet<>();

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
	public TestValidatorSecondPass(TestValidator firstPass) {
		this.firstPass = firstPass;
		this.program = firstPass.getProgram();
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	protected void validate() {
		List<Module> modules = new LinkedList<>();
		KRInterface kri = null;
		for (final AgentProgram agent : this.program.getAgents().values()) {
			if (kri == null) {
				kri = agent.getKRInterface();
			}
			modules.addAll(agent.getModules());
		}

		// Collect all info needed for validation
		this.processed.clear();
		for (Module module : modules) {
			visitModule(module);
		}

		// Process the test queries
		for (AgentTest test : this.program.getTests()) {
			visitTest(test);
		}

		// report undefined KR expressions
		validateKR(kri);
	}

	/**
	 * Identify unused and undefined KR expressions.
	 */
	private void validateKR(KRInterface kri) {
		// Collect undefined and unused KR expressions
		Set<DatabaseFormula> tempDbfs;
		Set<Query> tempQueries;

		tempDbfs = new HashSet<DatabaseFormula>(this.knowledge);
		tempDbfs.addAll(this.beliefs);
		tempDbfs.addAll(this.dynamicBeliefs);
		tempQueries = new HashSet<Query>(this.beliefQueries);
		tempQueries.addAll(this.goals);
		Set<Query> undefinedBeliefQueries = kri.getUndefined(tempDbfs,
				this.beliefQueries);

		tempDbfs = new HashSet<DatabaseFormula>(this.knowledge);
		tempDbfs.addAll(this.goalDbfs);
		tempQueries = new HashSet<Query>(this.goalQueries);
		tempQueries.addAll(this.goals);
		Set<Query> undefinedGoalQueries = kri.getUndefined(tempDbfs,
				this.goalQueries);
		undefinedGoalQueries.removeAll(kri.getUndefined(this.knowledge,
				new HashSet<Query>()));

		// Reserved keywords that should not be reported
		Set<String> reserved = new HashSet<>();
		reserved.add(AGENT);
		reserved.add(ME);
		reserved.add(PERCEPT);
		reserved.add(RECEIVED);
		reserved.add(SENT);

		// Report undefined KR expressions
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
	}

	private void visitTest(AgentTest test) {
		for (final TestSection section : test.getTests().getTestSections()) {
			this.beliefQueries.addAll(getBeliefQueries(section));
			this.goalQueries.addAll(getGoalQueries(section));
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
		// extract relevant sets of database formulas and queries from module
		this.knowledge.addAll(module.getKnowledge());
		// exploits fact that we don't allow module declarations within modules
		this.beliefs.addAll(module.getBeliefs());
		this.processed.clear();
		this.dynamicBeliefs.addAll(getInsertedBeliefs(module));
		this.processed.clear();
		this.goals.addAll(module.getGoals());
		this.processed.clear();
		this.goals.addAll(getAdoptedGoals(module));
		this.processed.clear();
		this.goalDbfs.addAll(getGoalDfs(module));
		this.processed.clear();
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
		if (this.processed.add(module)) {
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
	private static Set<Query> getBeliefQueries(TestSection test) {
		Set<MentalLiteral> literals = new HashSet<MentalLiteral>();
		for (MentalStateCondition msc : test.getQueries()) {
			literals.addAll(AgentValidatorSecondPass.getBeliefLiterals(msc));
		}
		Set<Query> queries = new HashSet<>(literals.size());
		for (MentalLiteral literal : literals) {
			queries.add(literal.getFormula());
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
		if (this.processed.add(module)) {
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
		if (this.processed.add(module)) {
			// Add goals in goal section
			for (Query query : module.getGoals()) {
				dbfs.addAll(query.toUpdate().getAddList());
			}
			// Add updates in adopts
			for (Rule rule : module.getRules()) {
				for (Action<?> action : rule.getAction().getActions()) {
					if (action instanceof AdoptAction) {
						dbfs.addAll(((AdoptAction) action).getUpdate()
								.getAddList());
					}
					if (action instanceof ModuleCallAction) {
						dbfs.addAll(getGoalDfs(((ModuleCallAction) action)
								.getTarget()));
					}
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
	private static Set<Query> getGoalQueries(TestSection test) {
		Set<MentalLiteral> literals = new HashSet<MentalLiteral>();
		for (MentalStateCondition msc : test.getQueries()) {
			literals.addAll(AgentValidatorSecondPass.getGoalLiterals(msc));
		}
		Set<Query> queries = new HashSet<>(literals.size());
		for (MentalLiteral literal : literals) {
			queries.add(literal.getFormula());
		}
		return queries;
	}
}
