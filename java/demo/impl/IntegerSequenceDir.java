package demo.impl;

/**
 * Concrete implementation of abstract service directory for dealing with integer sequences.
 */
final class IntegerSequenceDir extends ServiceDirectory<demo.IntegerSequence> {

	/*private static final Converter<demo.IntegerSequence> CONVERTER = new Converter<demo.IntegerSequence>() {
		
		public demo.IntegerSequence narrow(org.omg.CORBA.Object obj) {
			return demo.IntegerSequenceHelper.narrow(obj);
		}
	};*/

	public IntegerSequenceDir(boolean createIfNeeded) {
		super(Config.orb(), 
				Config.DIR_NAME, 
				demo.IntegerSequenceHelper.class, 
				createIfNeeded);
	}
}
