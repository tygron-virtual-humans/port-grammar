package languageTools.program.agent.actions;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentProgram;

/**
 * Created by svenpopping on 29/04/15.
 */
public class CalculateAction extends Action<Update> {

    /**
     * The argument that determines what will be logged.
     */
    private final String argument;

    /**
     *
     */
    public CalculateAction(String argument, SourceInfo info, KRInterface kri) {
        super(AgentProgram.getTokenName(GOAL.LOG), info, kri);
        this.argument = argument;
    }

    /**
     *
     * @param substitution
     * @return
     */
    @Override
    public Expression applySubst(Substitution substitution) {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "CalculateAction{" +
                "argument='" + argument + '\'' +
                '}';
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

        if (argument != null ? !argument.equals(that.argument) : that.argument != null) return false;

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (argument != null ? argument.hashCode() : 0);
        return result;
    }
}
