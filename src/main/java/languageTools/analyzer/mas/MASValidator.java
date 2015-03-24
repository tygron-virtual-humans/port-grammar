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

package languageTools.analyzer.mas;

import goalhub.krTools.KRFactory;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import krTools.KRInterface;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.KRInterfaceNotSupportedException;
import languageTools.analyzer.Validator;
import languageTools.errors.mas.MASError;
import languageTools.errors.mas.MASErrorStrategy;
import languageTools.errors.mas.MASWarning;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.MAS2GParser;
import languageTools.parser.MAS2GParser.AgentFileContext;
import languageTools.parser.MAS2GParser.AgentFileParContext;
import languageTools.parser.MAS2GParser.AgentFilesContext;
import languageTools.parser.MAS2GParser.BasicRuleContext;
import languageTools.parser.MAS2GParser.ConditionalRuleContext;
import languageTools.parser.MAS2GParser.ConstantContext;
import languageTools.parser.MAS2GParser.EntityConstraintContext;
import languageTools.parser.MAS2GParser.EntityDescriptionContext;
import languageTools.parser.MAS2GParser.EnvironmentContext;
import languageTools.parser.MAS2GParser.FunctionContext;
import languageTools.parser.MAS2GParser.InitExprContext;
import languageTools.parser.MAS2GParser.InitKeyValueContext;
import languageTools.parser.MAS2GParser.LaunchPolicyContext;
import languageTools.parser.MAS2GParser.LaunchRuleComponentContext;
import languageTools.parser.MAS2GParser.LaunchRuleContext;
import languageTools.parser.MAS2GParser.ListContext;
import languageTools.parser.MAS2GParser.MasContext;
import languageTools.parser.MAS2GParser.MultiplierContext;
import languageTools.parser.MAS2GParser.StringContext;
import languageTools.parser.MAS2GVisitor;
import languageTools.parser.mas.MyMAS2GLexer;
import languageTools.program.mas.Launch;
import languageTools.program.mas.LaunchRule;
import languageTools.program.mas.MASProgram;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.mas.MASSymbol;
import languageTools.utils.Extension;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;

/**
 * Validates a MAS file and constructs a MAS program.
 */
@SuppressWarnings("rawtypes")
public class MASValidator extends
Validator<MyMAS2GLexer, MAS2GParser, MASErrorStrategy, MASProgram>
implements MAS2GVisitor {

	private MAS2GParser parser;
	private static MASErrorStrategy strategy = null;

	/**
	 * Symbol table with agent file references.
	 */
	private final SymbolTable agentFiles = new SymbolTable();

	/**
	 * Creates a MAS validator for file with given name.
	 *
	 * @param filename
	 *            Name of a file.
	 */
	public MASValidator(String filename) {
		super(filename);
	}

	@Override
	protected MASErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new MASErrorStrategy();
		}
		return strategy;
	}

	/**
	 * @return Symbol table with agent file references.
	 */
	public SymbolTable getSymbolTable() {
		return this.agentFiles;
	}

	@Override
	protected MyMAS2GLexer getNewLexer(CharStream stream,
			ANTLRErrorListener errorlistener) {
		return new MyMAS2GLexer(stream, errorlistener);
	}

	@Override
	protected MAS2GParser getNewParser(TokenStream stream) {
		this.parser = new MAS2GParser(stream);
		return this.parser;
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.mas();
	}

	@Override
	protected MASProgram getNewProgram(File masfile) {
		return new MASProgram(new InputStreamPosition(0, 0, 0, 0, masfile));
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
	public Void visitMas(MasContext ctx) {
		if (ctx.environment() != null) {
			boolean hadEnv = false;
			for (EnvironmentContext envCtx : ctx.environment()) {
				if (hadEnv) {
					reportWarning(MASWarning.SECTION_DUPLICATE, envCtx);
				} else {
					visitEnvironment(envCtx);
					hadEnv = true;
				}
			}
		}
		if (ctx.agentFiles() != null) {
			boolean hadFiles = false;
			for (AgentFilesContext filesCtx : ctx.agentFiles()) {
				if (hadFiles) {
					reportWarning(MASWarning.SECTION_DUPLICATE, filesCtx);
				} else {
					visitAgentFiles(filesCtx);
					hadFiles = true;
				}
			}
		}
		if (ctx.launchPolicy() != null) {
			boolean hadLaunch = false;
			for (LaunchPolicyContext launchCtx : ctx.launchPolicy()) {
				if (hadLaunch) {
					reportWarning(MASWarning.SECTION_DUPLICATE, launchCtx);
				} else {
					visitLaunchPolicy(launchCtx);
					hadLaunch = true;
				}
			}
		}
		return null; // Java says must return something even when Void
	}

	// -------------------------------------------------------------
	// ENVIRONMENT section
	// -------------------------------------------------------------

	@Override
	public Void visitEnvironment(EnvironmentContext ctx) {
		/**
		 * Get the reference to the environment interface file.
		 */
		String filename;
		if (ctx.string() == null) {
			filename = "";
		} else {
			filename = visitString(ctx.string());
		}
		if (filename.isEmpty()) {
			reportWarning(MASWarning.ENVIRONMENT_NO_REFERENCE, ctx);
		}
		File environmentfile = new File(filename);
		if (!environmentfile.isAbsolute()) {
			// relative to path specified for MAS file
			environmentfile = new File(getPathRelativeToSourceFile(filename));
		}

		// If extension is "jar", the file must exist;
		// otherwise it can be a reference to an existing environment.
		String ext = FilenameUtils.getExtension(filename);
		if (ext.equals("jar") && !environmentfile.isFile()) {
			reportError(MASError.ENVIRONMENT_COULDNOT_FIND, ctx,
					environmentfile.getPath());
		} else {
			getProgram().setEnvironmentfile(environmentfile);
		}

		/**
		 * Get list of key-value initialization parameters.
		 */
		for (InitKeyValueContext pair : ctx.initKeyValue()) {
			visitInitKeyValue(pair);
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitInitKeyValue(InitKeyValueContext ctx) {
		boolean problem = (ctx.exception != null);

		// We can't check whether specified environment interface supports
		// parameter key
		// Just check whether it's not used more than once
		String key = null;
		if (ctx.ID() != null) {
			key = ctx.ID().getText();
			if (getProgram().getInitParameters().containsKey(key)) {
				problem = reportWarning(MASWarning.INIT_DUPLICATE_KEY,
						ctx.ID(), key);
			}
		}

		// Get parameter value
		Object value = visitInitExpr(ctx.initExpr());

		// If value equals null, we did not recognize a valid initialization
		// parameter
		if (value == null && ctx.initExpr() != null) {
			problem = reportError(MASError.INIT_UNRECOGNIZED_PARAMETER,
					ctx.initExpr(), ctx.initExpr().getText());
		}

		// Add key-value pair as initialization parameter to MAS program (only
		// if no problems were detected).
		if (!problem && (key != null) && (value != null)) {
			getProgram().addInitParameter(key, value);
		}

		return null; // Java says must return something even when Void
	}

	/**
	 * @return {@code null} if no valid parameter was recognized.
	 */
	@Override
	public Object visitInitExpr(InitExprContext ctx) {
		if (ctx != null) {
			if (ctx.constant() != null) {
				return visitConstant(ctx.constant());
			}
			if (ctx.function() != null) {
				return visitFunction(ctx.function());
			}
			if (ctx.list() != null) {
				return visitList(ctx.list());
			}
		}

		return null;
	}

	@Override
	public Object visitConstant(ConstantContext ctx) {
		if (ctx.ID() != null) {
			return ctx.ID().getText();
		}
		if (ctx.FLOAT() != null) {
			return Double.parseDouble(ctx.FLOAT().getText());
		}
		if (ctx.INT() != null) {
			return Integer.parseInt(ctx.INT().getText());
		}
		if (ctx.string() != null) {
			// TODO: what is the logic here?
			String text = ctx.string().getText();
			String[] parts = text.split("(?<!\\\\)\"", 0);
			return parts[1].replace("\\\"", "\"");
		}
		if (ctx.SingleQuotedStringLiteral() != null) {
			// TODO: what is the logic here?
			String text = ctx.SingleQuotedStringLiteral().getText();
			String[] parts = text.split("(?<!\\\\)'", 0);
			return parts[1].replace("\\'", "'");
		}

		// We did not recognize a valid initialization parameter.
		reportError(MASError.INIT_UNRECOGNIZED_PARAMETER, ctx, ctx.getText());

		return null;
	}

	@Override
	public Object visitFunction(FunctionContext ctx) {
		// Get function name
		String name = ctx.ID().getText();

		// Get function parameters
		int nrOfPars = ctx.initExpr().size();
		Object[] parameters = new Object[nrOfPars];
		for (int i = 0; i < nrOfPars; i++) {
			parameters[i] = visitInitExpr(ctx.initExpr(i));
		}

		return new AbstractMap.SimpleEntry<String, Object[]>(name, parameters);
	}

	@Override
	public Object visitList(ListContext ctx) {
		boolean problem = false;

		List<Object> list = new ArrayList<Object>(ctx.initExpr().size());
		for (InitExprContext expr : ctx.initExpr()) {
			if (expr == null) {
				problem = true;
				continue;
			}
			list.add(visitInitExpr(expr));
		}

		if (!problem) {
			return list;
		} else {
			return null;
		}
	}

	// -------------------------------------------------------------
	// AGENTFILES section
	// -------------------------------------------------------------

	@Override
	public Void visitAgentFiles(AgentFilesContext ctx) {
		boolean problem = (ctx.exception != null);

		for (AgentFileContext agentfile : ctx.agentFile()) {
			visitAgentFile(agentfile);
		}

		// MAS program should have non-empty set of agent files; if not, issue
		// warning
		if (!problem && getProgram().getAgentFiles().isEmpty()) {
			reportWarning(MASWarning.AGENTFILES_NO_AGENTS, ctx);
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitAgentFile(AgentFileContext ctx) {
		boolean problem = false;

		// Get agent file name
		String path = visitString(ctx.string());
		File file = new File(path);
		if (!file.isAbsolute()) {
			// relative to path specified for MAS file
			file = new File(getPathRelativeToSourceFile(path));
		}

		// Check file extension
		String ext = FilenameUtils.getExtension(path);
		if (Extension.getFileExtension(file) != Extension.GOAL) {
			problem = reportError(MASError.AGENTFILE_OTHER_EXTENSION,
					ctx.string(), ext);
		} else if (!file.isFile()) {
			problem = reportError(MASError.AGENTFILE_COULDNOT_FIND,
					ctx.string(), file.getPath());
		}

		// Get (optional) parameters
		Map<String, String> parameters = new HashMap<String, String>();
		for (AgentFileParContext parameter : ctx.agentFilePar()) {
			Map.Entry<String, String> keyValuePair = visitAgentFilePar(parameter);
			String key = keyValuePair.getKey();
			if (parameters.containsKey(key)) {
				reportWarning(MASWarning.AGENTFILE_DUPLICATE_KEY, parameter,
						key);
			} else {
				parameters.put(key, keyValuePair.getValue());
			}
		}

		// Construct agent symbol
		String agentName;
		if (parameters.containsKey("name")) {
			agentName = parameters.get("name");
		} else {
			agentName = FilenameUtils.getBaseName(FilenameUtils.getName(path));
		}

		// Add agent symbol to symbol table for later reference (if key does not
		// yet exist).
		if (!this.agentFiles.define(new MASSymbol(agentName, file,
				getSourceInfo(ctx)))) {
			problem = reportWarning(MASWarning.AGENTFILES_DUPLICATE_NAME,
					ctx.string(), agentName);
		}

		// Get KR language
		String interfaceName = parameters.get("language");
		KRInterface krInterface = null;
		try {
			if (interfaceName == null) { // no parameter set, use default
				krInterface = KRFactory.getDefaultInterface();
			} else {
				krInterface = KRFactory.getInterface(interfaceName);
			}
		} catch (KRInterfaceNotSupportedException | KRInitFailedException e) {
			reportError(MASError.KRINTERFACE_NOT_SUPPORTED, ctx.agentFilePar()
					.get(0), interfaceName);
		}

		// Add agent file to MAS program (only if no problems were detected and
		// file does not yet exist).
		if (!problem && !getProgram().getAgentFiles().contains(file)) {
			getProgram().addAgentFile(file);
			getProgram().setKRInterface(file, krInterface);
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Map.Entry<String, String> visitAgentFilePar(AgentFileParContext ctx) {
		String key = ctx.key.getText().toLowerCase();
		String value;
		value = ctx.ID().getText();

		return new AbstractMap.SimpleEntry<String, String>(key, value);
	}

	// -------------------------------------------------------------
	// LAUNCHPOLICY section
	// -------------------------------------------------------------

	@Override
	public Void visitLaunchPolicy(LaunchPolicyContext ctx) {
		boolean problem = (ctx.exception != null);

		for (LaunchRuleContext rule : ctx.launchRule()) {
			visitLaunchRule(rule);
		}

		// MAS program should have non-empty set of launch rules now; otherwise,
		// issue warning
		if (!problem && getProgram().getLaunchRules().isEmpty()) {
			problem = reportWarning(MASWarning.LAUNCH_NO_RULES, ctx);
		}

		// Check that all agent files specified in agentfiles section have been
		// used
		List<File> filesUsed = new ArrayList<File>();
		for (LaunchRule rule : getProgram().getLaunchRules()) {
			for (Launch launch : rule.getInstructions()) {
				filesUsed.add(launch.getAgentFile());
			}
		}
		if (!problem && !filesUsed.containsAll(getProgram().getAgentFiles())) {
			reportWarning(MASWarning.AGENTFILE_UNUSED, ctx);
		}

		// When environment is specified, launchpolicy section should have
		// conditional rules to connect agents to it
		if (!problem && getProgram().hasEnvironment()) {
			boolean crule = false;
			for (LaunchRule rule : getProgram().getLaunchRules()) {
				crule |= rule.getConditional();
			}
			if (!crule) {
				reportWarning(MASWarning.LAUNCH_NO_CONDITIONAL_RULES, ctx);
			}
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitLaunchRule(LaunchRuleContext ctx) {
		if (ctx.basicRule() != null) {
			List<Launch> launches = visitBasicRule(ctx.basicRule());

			// A basic launch rule should not use wild cards.
			List<Launch> usesWildCard = new ArrayList<Launch>();
			for (Launch launch : launches) {
				if (launch.getGivenName("*", 0).equals("*")) {
					reportWarning(MASWarning.LAUNCH_INVALID_WILDCARD, ctx, ctx
							.getText().substring(0, ctx.getText().length() - 1));
					usesWildCard.add(launch);
				}
			}
			launches.removeAll(usesWildCard);

			// A launch rule cannot have an empty list of launch instructions.
			if (!launches.isEmpty()) {
				getProgram().addLaunchRule(new LaunchRule(launches));
			}
		}
		if (ctx.conditionalRule() != null) {
			LaunchRule rule = visitConditionalRule(ctx.conditionalRule());
			if (rule != null) {
				getProgram().addLaunchRule(rule);
			}
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public List<Launch> visitBasicRule(BasicRuleContext ctx) {
		List<Launch> launches = new ArrayList<Launch>();
		Launch launch;
		for (LaunchRuleComponentContext component : ctx.launchRuleComponent()) {
			launch = visitLaunchRuleComponent(component);
			if (launch != null) {
				launches.add(launch);
			}
		}

		return launches;
	}

	@Override
	public Launch visitLaunchRuleComponent(LaunchRuleComponentContext ctx) {
		boolean problem = false;

		// Get given name, if any
		String givenName = "????";
		String filename = "";
		if (ctx.STAR() != null) {
			givenName = "*";
			// must be a file ref as well
			filename = ctx.ID().get(0).getText();
		} else if (ctx.ID() != null) {
			givenName = ctx.ID().get(0).getText();
			if (ctx.ID().size() > 1) {
				filename = ctx.ID().get(1).getText();
			} else {
				filename = givenName;
			}
		} else {
			problem = true;
		}

		// Construct launch instruction
		Launch launch = null;
		MASSymbol symbol;

		// If agent file has been explicitly specified, use it to identify agent
		// file.
		symbol = (MASSymbol) this.agentFiles.resolve(filename);
		if (symbol != null) {
			launch = new Launch(symbol.getFile());
		} else {
			problem = reportWarning(MASWarning.AGENTFILE_NONEXISTANT_REFERENCE,
					ctx, filename);
		}

		// Add given name and specified number of applications, if any, to
		// launch instruction (only if no problems were detected)
		if (!problem) {
			launch.setGivenName(givenName);

			// Set specified number of applications of the instruction, if any.
			if (ctx.multiplier() != null) {
				launch.setNumberOfAgentsToLaunch(visitMultiplier(ctx
						.multiplier()));
			}
		}

		return launch;
	}

	@Override
	public Integer visitMultiplier(MultiplierContext ctx) {
		return Integer.parseInt(ctx.INT().getText().trim());
	}

	@Override
	public LaunchRule visitConditionalRule(ConditionalRuleContext ctx) {
		LaunchRule rule;
		List<Launch> launches = visitBasicRule(ctx.basicRule());
		if (!launches.isEmpty()) {
			rule = new LaunchRule(launches);
		} else {
			return null;
		}

		// Rule is a conditional rule; set flag accordingly
		rule.setConditional();

		// Add constraints, if any.
		Map<String, TerminalNode> constraints = visitEntityDescription(ctx
				.entityDescription());

		// Process constraints
		if (constraints.containsKey("name")) {
			rule.setRequiredEntityName(constraints.get("name").getText());
		}
		if (constraints.containsKey("type")) {
			rule.setRequiredEntityType(constraints.get("type").getText());
		}
		if (constraints.containsKey("max")) {
			rule.setMaxNumberOfApplications(Integer.parseInt(constraints.get(
					"max").getText()));
		}

		return rule;
	}

	@Override
	public Map<String, TerminalNode> visitEntityDescription(
			EntityDescriptionContext ctx) {
		Map<String, TerminalNode> constraints = new HashMap<String, TerminalNode>();
		for (EntityConstraintContext constraint : ctx.entityConstraint()) {
			Map.Entry<String, TerminalNode> keyValuePair = visitEntityConstraint(constraint);

			boolean problem = false;
			String key = keyValuePair.getKey();
			TerminalNode value = keyValuePair.getValue();

			if (constraints.containsKey(key)) {
				problem = reportWarning(MASWarning.CONSTRAINT_DUPLICATE,
						constraint, key);
			}
			if (!problem && keyValuePair.getValue() != null) {
				constraints.put(key, value);
			}
		}

		return constraints;
	}

	@Override
	public Map.Entry<String, TerminalNode> visitEntityConstraint(
			EntityConstraintContext ctx) {
		String key = "????";
		if (ctx.key != null) {
			key = ctx.key.getText().toLowerCase();
		}

		TerminalNode value = null;
		if (ctx.ID() != null) {
			value = ctx.ID();
		} else {
			value = ctx.INT();
		}

		return new AbstractMap.SimpleEntry<String, TerminalNode>(key, value);
	}

	@Override
	public String visitString(StringContext ctx) {
		String str = "";

		for (TerminalNode node : ctx.StringLiteral()) {
			str += removeLeadTrailCharacters(node.getText());
		}
		return str;
	}

	/**
	 * Validation of MAS program does not require second pass to resolve any
	 * references.
	 */
	@Override
	protected void secondPass(ParseTree tree) {

	}

}
