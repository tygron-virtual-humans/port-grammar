package languageTools.program.agent.actions;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;

import java.util.List;

/**
 * Created by svenpopping on 29/04/15.
 */
public class CalculateAction extends Action<Term> {

    /**
     *
     */
    public CalculateAction(List<Term> parameters, SourceInfo info, KRInterface kri) {
        super(AgentProgram.getTokenName(GOAL.CALCULATE), info, kri);

        for(Term parameter : parameters){
            addParameter(parameter);
        }
    }

    /**
     *
     * @param substitution
     * @return
     */
    @Override
    public CalculateAction applySubst(Substitution substitution) {
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "CalculateAction";
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CalculateAction)) return false;
        if (!super.equals(o)) return false;

        CalculateAction that = (CalculateAction) o;

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result;
        return result;
    }
}
