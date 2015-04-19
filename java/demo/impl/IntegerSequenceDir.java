package demo.impl;

/**
 * Concrete implementation of abstract service directory for dealing with integer sequences.
 */
final class IntegerSequenceDir extends ServiceDirectory<demo.IntegerSequence> {

	public IntegerSequenceDir(boolean createIfAbsent) {
		super(Config.orb(), 
				Config.DIR_NAME, 
				demo.IntegerSequenceHelper.class, 
				createIfAbsent);
	}
}
