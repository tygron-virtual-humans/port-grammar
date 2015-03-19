package languageTools.program.agent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.msc.Macro;

/**
 * Mechanism to make a Macro a standard expression. TODO proposal: Macro
 * implements Expression
 */
public class MacroExpression implements Expression {

	final protected Macro macro;
	private KRInterface krInterface;

	public MacroExpression(Macro m, KRInterface kri) {
		macro = m;
		krInterface =kri;
	}

	public Macro getMacro() {
		return macro;
	}

	@Override
	public String getSignature() {
		return macro.getSignature();
	}

	@Override
	public boolean isVar() {
		return false;
	}

	@Override
	public boolean isClosed() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public Set<Var> getFreeVar() {
		return macro.getFreeVar();
	}

	@Override
	public Expression applySubst(Substitution substitution) {
		return new MacroExpression(macro.applySubst(substitution), krInterface);
	}

	@Override
	public Substitution mgu(Expression expr) {
		Substitution substitution;
		MacroExpression other = (MacroExpression) expr;

		if (!macro.getParameters().isEmpty()
				&& macro.getParameters().size() == other.getParameters()
						.size()) {
			// Get mgu for first parameter
			substitution = macro.getParameters().get(0)
					.mgu(other.getParameters().get(0));
			// Get mgu's for remaining parameters
			for (int i = 1; i < macro.getParameters().size()
					&& substitution != null; i++) {
				Substitution mgu = macro.getParameters().get(i)
						.mgu(other.getParameters().get(i));
				substitution = substitution.combine(mgu);
			}

		} else {
			substitution = krInterface.getSubstitution(
					new LinkedHashMap<Var, Term>());
		}

		return substitution;
	}

	private List<Term> getParameters() {
		return macro.getParameters();
	}

	@Override
	public SourceInfo getSourceInfo() {
		return macro.getSourceInfo();
	}

}
