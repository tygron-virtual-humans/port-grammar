package languageTools.program.test.testsection;

import languageTools.program.agent.actions.ActionCombo;

/**
 * Action rule for test program. Executes an {@link ActionCombo}.
 *
 * @author mpkorstanje
 */
public class DoActionSection implements TestSection {
	/**
	 * @return the action combo to execute
	 */
	public ActionCombo getAction() {
		return this.action;
	}

	@Override
	public String toString() {
		return "Action [action=" + this.action + "]";
	}

	private final ActionCombo action;

	/**
	 * Constructs a new action invocation.
	 *
	 * @param action
	 *            the action to invoke. Should be closed.
	 */
	public DoActionSection(ActionCombo action) {
		this.action = action;
	}
}
