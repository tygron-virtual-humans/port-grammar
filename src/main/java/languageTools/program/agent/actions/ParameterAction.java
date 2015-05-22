package languageTools.program.agent.actions;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.actions.parameter.ActionToken;
import sun.security.krb5.KrbApRep;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by svenpopping on 29/04/15.
 */
public abstract class ParameterAction extends Action<Term> {

    /**
     *
     */
    public ParameterAction(int token, List<Term> parameters, SourceInfo info, KRInterface kri) {
        super(AgentProgram.getTokenName(token), info, kri);

        for(Term parameter : parameters){
            addParameter(parameter);
        }
    }

    /**
     *
     * For use with in the applySubst function.
     * @param substitution the Substitution that needs to be applied
     * @return the parameters, with the substitution applied to them.
     */
    public List<Term> applySubstToParams(Substitution substitution) {
        ArrayList<Term> parameters = new ArrayList<Term>();

        // Apply substitution to action parameters, pre- and post-condition.
        for (Term parameter : getParameters()) {
            parameters.add(parameter.applySubst(substitution));
        }

        return parameters;
    }

    @Override
    public ParameterAction applySubst(Substitution substitution) {
        List<Term> newParameters = applySubstToParams(substitution);
        try {
            return this.getClass().getDeclaredConstructor(List.class,SourceInfo.class, KRInterface.class).newInstance(
                    newParameters,getSourceInfo(),getKRInterface());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        String res = this.getClass().getName() + "(";
        for(Term term : getParameters()){
            res += term.toString() + ", ";
        }
        return res.substring(0,res.length()-2) + ")";
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o.getClass().equals(this.getClass()))) return false;
        if (!super.equals(o)) return false;

        ParameterAction that = (ParameterAction) o;

        if (!this.getParameters().equals(that.getParameters())) return false;

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
