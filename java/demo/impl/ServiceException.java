package demo.impl;

/**
 * Exception thrown to indicate a CORBA-related problem.
 */
public class ServiceException extends Exception {
	
	/** Required by Java serialization mechanism. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with the specified message.
	 * 
	 * @param message
	 *    error message
	 */
	public ServiceException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with the specified message and cause.
	 * 
	 * @param message
	 *    error message
	 * @param cause
	 *    cause of the exception
	 */
	public ServiceException(String message, Exception cause) {
		super(message, cause);
	}
}
