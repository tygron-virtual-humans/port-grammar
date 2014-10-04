package languageTools.errors;

import java.io.File;

import krTools.parser.SourceInfo;

import org.antlr.v4.runtime.tree.TerminalNode;

import languageTools.parser.InputStreamPosition;

public abstract class Message {
	
	private SourceInfo source = null;
	
	public interface ValidatorMessageType {
		public String toReadableString(String... args);
	}

	protected final ValidatorMessageType type;
	protected String[] args;

	public Message(ValidatorMessageType type, SourceInfo source, String... args) {
		this.type = type;
		this.source = source;
		this.args = args;
	}

	/**
	 * Collects details about the exact position in the input stream from an
	 * ANTLR TerminalNode object.
	 *  
	 * @param context The ANTLR TerminalNode object
	 * @return An {@link #InputStreamPosition} object
	 */
	public Message(ValidatorMessageType type, TerminalNode context, File file, String... args) {
		this.type = type;
		this.source = new InputStreamPosition(context.getSymbol(),
				context.getSymbol(), file);
		this.args = args;
	}

	public ValidatorMessageType getType() {
		return this.type;
	}

	public SourceInfo getSource() {
		return this.source;
	}

	public void setSource(SourceInfo source) {
		this.source = source;
	}

	public String[] getArguments() {
		return this.args;
	}

	public void setArguments(String... args) {
		this.args = args;
	}

	public String toShortString() {
		final String[] shortargs = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.length() > 1500) {
				shortargs[i] = arg.substring(0, 1500) + "...";
			} else {
				shortargs[i] = arg;
			}
		}
		return this.type.toReadableString(shortargs);
	}

	@Override
	public abstract String toString();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof Message)) {
			return false;
		} else {
			Message other = (Message) obj;
			if (this.getSource() == null) {
				if (other.getSource() != null) {
					return false;
				}
			} else if (!this.getSource().equals(other.getSource())) {
				return false;
			}
			if (this.type == null) {
				if (other.getType() != null) {
					return false;
				}
			} else if (!this.type.equals(other.getType())) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// TODO: How to map getSource() to int??? Replace 17 with that expression!
		result = prime * result + ((getSource() == null) ? 0 : 17);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

}
