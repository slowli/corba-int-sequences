package demo.impl;

import org.omg.CORBA.ORB;

/**
 * Program configuration.
 */
public final class Config {

	/** Name of the directory containing integer sequence services. */
	public static final String DIR_NAME = "integer-seq";
	
	/** Arguments for initializing the ORB. */
	private static final String[] ORB_ARGS = new String[] {
		"-ORBInitialPort", "2809", 
		"-ORBInitialHost", "localhost"
	};
	
	private static ORB orb;
	
	/**
	 * Returns an ORB singleton for the Java application.
	 * 
	 * @return
	 */
	public static ORB orb() {
		if (orb == null) {
			orb = ORB.init(Config.ORB_ARGS, null);
		}
		return orb;
	}
}
