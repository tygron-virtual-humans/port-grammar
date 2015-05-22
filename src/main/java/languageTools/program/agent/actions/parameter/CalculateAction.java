package languageTools.program.agent.actions.parameter;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.actions.ParameterAction;

import java.util.List;

/**
 * Created by wouter on 21/05/15.
 */
@ActionToken(GOAL.CALCULATE)
public class CalculateAction extends ParameterAction {


    /**
     * @param parameters
     * @param info
     * @param kri
     */
    public CalculateAction(List<Term> parameters, SourceInfo info, KRInterface kri) {
        super(GOAL.CALCULATE, parameters, info, kri);
    }
}
