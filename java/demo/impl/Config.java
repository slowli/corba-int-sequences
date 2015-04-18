package demo.impl;

import org.omg.CORBA.ORB;

public final class Config {

	public static final String DIR_NAME = "integer-seq";
	
	public static final String SUFFIX = "java";
	
	private static final String[] ORB_ARGS = new String[] {
		"-ORBInitialPort", "2809", 
		"-ORBInitialHost", "localhost"
	};
	
	private static ORB orb;
	
	public static ORB orb() {
		if (orb == null) {
			orb = ORB.init(Config.ORB_ARGS, null);
		}
		return orb;
	}
}
