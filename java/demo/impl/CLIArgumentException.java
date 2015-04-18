package demo.impl;

/**
 * Exception related to the incorrect use of program arguments.
 */
public class CLIArgumentException extends Exception {

	/**
	 * Required by java.io.Serializable interface.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with the specified message.
	 * 
	 * @param message
	 *    error message
	 */
	public CLIArgumentException(String message) {
		super(message);
	}
}
