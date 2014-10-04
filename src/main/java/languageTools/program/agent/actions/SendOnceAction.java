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

package languageTools.program.agent.actions;

import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.parser.GOAL;
import languageTools.program.agent.AgentProgram;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.selector.Selector;

/**
 * The {@link SendOnceAction} is a special kind of send action that requires
 * that the message to be sent has not been sent before yet (to the intended
 * recipients). It checks this by inspecting the agent's mail box for
 * corresponding {@code sent} records.
 * <p>
 * If only some of the intended recipients have not yet received the message, it
 * will be sent to those agents only.
 * </p>
 */
public class SendOnceAction extends MentalAction {
	
	/**
	 * Mood of the message to be sent.
	 */
	private final SentenceMood mood;

	/**
	 * Creates a {@link SendOnceAction} that sends a message (content) to one or
	 * more agents if the message has not yet been sent before.
	 * 
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param mood
	 *            The {@link SentenceMood} of the message.
	 * @param content
	 *            The content of the message.
	 */
	public SendOnceAction(Selector selector, SentenceMood mood, Update content, SourceInfo info) {
		super(AgentProgram.getTokenName(GOAL.SENDONCE), selector, info);
		addParameter(content);
		this.mood = mood;
	}
	
	/**
	 * @return The sentence mood of the message.
	 */
	public SentenceMood getMood() {
		return mood;
	}

	/**
	 * Returns the message of this send action.
	 * 
	 * @return The message of this send action.
	 */
	public Message getMessage() {
		return new Message(getParameters().get(0), mood);
	}

	// We cannot use applySubst in SendAction because we need a result of type
	// SendOnceAction
	// to ensure we call SendOnceAction#determineReceivers when
	// evaluatePrecondition is called.
	// We do not need to override evaluatePrecondition in SendAction as
	// everything after evaluating
	// the precondition of both actions is the same.
	@Override
	public SendOnceAction applySubst(Substitution substitution) {
		return new SendOnceAction(getSelector().applySubst(substitution),
				getMood(), getParameters().get(0).applySubst(substitution), getSourceInfo());
	}

	/**
	 * The precondition of a {@link SendOnceAction} is that there is a non-empty
	 * set of receivers of the message to be sent that should receive this
	 * message but did not receive it yet; otherwise the precondition fails.
	 * 
	 * @param language
	 *            The KR language used for representing the precondition.
	 * @return The {@link MentalStateCondition} TODO ... .
	 */
// TODO:
//	@Override
//	public MentalStateCondition getPrecondition() {
//		Query query = null;
//		try {
//			query = getKRInterface().parseUpdate("true").toQuery();
//		} catch (ParserException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		List<MentalFormula> formulaList = new ArrayList<MentalFormula>();
//		Selector selector = new Selector(null);
//		formulaList.add(new BelLiteral(true, query, selector));
//		return new MentalStateCondition(formulaList);
//	}
	
	@Override
	public String toString() {
		return String.format("%1$s.%2$s(%3$s%4$s)", getSelector().toString(), getName(), 
				mood, getParameters().get(0).toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mood == null) ? 0 : mood.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SendOnceAction other = (SendOnceAction) obj;
		if (mood != other.mood)
			return false;
		return true;
	}

}
