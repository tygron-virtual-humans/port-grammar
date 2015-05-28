package languageTools.program.agent.actions.parameter;

import krTools.KRInterface;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.actions.ParameterAction;

import java.util.List;

/**
 * Created by wouter on 29/05/15.
 */
@ActionToken(GOAL.GA_APPRAISE)
public class GaAppraiseAction extends ParameterAction {

    public GaAppraiseAction(List<Term> parameters, SourceInfo info, KRInterface kri) {
        super(GOAL.GA_APPRAISE, parameters, info, kri);
    }
}
