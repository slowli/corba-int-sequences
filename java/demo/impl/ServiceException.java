package demo.impl;

public class ServiceException extends Exception {
	
	/** Required by Java serialization mechanism. */
	private static final long serialVersionUID = 1L;

	public ServiceException(String message) {
		super(message);
	}
	
	public ServiceException(String message, Exception e) {
		super(message, e);
	}
}
