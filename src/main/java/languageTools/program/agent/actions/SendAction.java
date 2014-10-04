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
 * Sends a message to one or more {@link Agent}s.
 * <p>
 * A {@link Selector} is used to indicate to which agent(s) the message should
 * be sent.
 * </p>
 * <p>
 * A message can have a {@link SentenceMood} and has content. The content of a
 * message is represented as a {@link DatabaseFormula} as it should be possible
 * to store a message in a database (i.e., the agent's mail box).
 * </p>
 */
public class SendAction extends MentalAction {

	/**
	 * Mood of the message to be sent.
	 */
	private final SentenceMood mood;

	/**
	 * Creates a {@link SendAction} that sends a message (content) to one or
	 * more agents.
	 * 
	 * @param selector
	 *            The {@link Selector} of this action, indicating where the
	 *            message should be send to.
	 * @param mood
	 *            The {@link SentenceMood} of the message.
	 * @param content
	 *            The content of the message.
	 */
	public SendAction(Selector selector, SentenceMood mood, Update content, SourceInfo info) {
		super(AgentProgram.getTokenName(GOAL.SEND), selector, info);
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

	@Override
	public SendAction applySubst(Substitution substitution) {
		return new SendAction(getSelector().applySubst(substitution),
				mood, getParameters().get(0).applySubst(substitution), getSourceInfo());
	}

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
		SendAction other = (SendAction) obj;
		if (mood != other.mood)
			return false;
		return true;
	}

}
