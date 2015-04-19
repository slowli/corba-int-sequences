package demo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import demo.impl.seq.FibonacciImpl;
import demo.impl.seq.FibonacciNaiveImpl;
import demo.impl.seq.RandomPrimeImpl;

public class Server {

	/** Working modes of the server application. */
	private static enum Mode {
		/** Run integer sequence implementations. */
		RUN,
		/** List implementations. */
		LIST,
		/** Print help message. */
		HELP
	}
	
	private static final String USAGE = 
		"Usage: server [--list | --help]\n" +
		"\n" +
		"Runs implementations of integer sequences.\n" +
		"\n" +
		"Options:\n" +
		"    --list\n" +
		"        List implementations hosted by this server and exit.\n" +
		"    --help\n" +
		"        Print this help message and exit.";

	private static final String DELIMITER = "--------------------------------------------------";

	/** Directory containing integer sequence services. */
	private final IntegerSequenceDir dir;
	
	/** Working mode of the application. */
	private Mode mode = Mode.RUN;
	
	/** Integer sequence implementations run by this server. */
	private final List<IntegerSequenceImpl> implementations = new ArrayList<IntegerSequenceImpl>();
	
	public Server(String[] args) throws CLIArgumentException {
		this.processArgs(args);
		this.dir = new IntegerSequenceDir(true);
		
		this.implementations.add(new FibonacciImpl());
		this.implementations.add(new FibonacciNaiveImpl());
		this.implementations.add(new RandomPrimeImpl());
		
		// Turn off CORBA logging as it is rather tedious
		final LogManager lman = LogManager.getLogManager();
		lman.reset();
	}
	
	private void processArgs(String[] args) throws CLIArgumentException {
		int argi = 0;
		while (argi < args.length) {
			final String arg = args[argi++];
			
			if (arg.equals("--list")) {
				this.mode = Mode.LIST;
				return;
			} else if (arg.equals("--help")) {
				this.mode = Mode.HELP;
				return;
			} else {
				throw new CLIArgumentException("Invalid argument: " + arg + ".");
			}
		}
	}
	
	/**
	 * Prints information about integer sequence implementations 
	 * that are run by this server program.
	 */
	public void listImplementations() {
		System.out.println("List of implementations hosted by this server:");
		for (IntegerSequenceImpl impl : this.implementations) {
			System.out.println(DELIMITER);
			Client.printInfo(impl, impl.corbaName());
		}
	}
	
	/**
	 * Runs integer sequence implementations.
	 * 
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation 
	 */
	private void runImplementations() throws ServiceException {
		this.dir.unbindAll(new ServiceDirectory.NameFilter() {
			
			public boolean matches(Name name) {
				return name.kind.endsWith("java");
			}
		});
		for (IntegerSequenceImpl impl : this.implementations) {
			this.dir.bind(impl.corbaName(), impl);
		}
		
		System.out.println("Ready for incoming requests...");
		Config.orb().run();
	}
	
	/**
	 * Runs the server program.
	 * 
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation
	 */
	public void run() throws ServiceException {
		switch (this.mode) {
			case RUN:
				this.runImplementations();
				return;
			case LIST:
				this.listImplementations();
				return;
			case HELP:
				System.out.println(USAGE);
				return;
		}
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server(args);
			server.run();
		} catch (CLIArgumentException e) {
			System.err.println(e.getMessage() + "\nInvoke with `--help` option to get help.");
			System.exit(2);
		} catch (ServiceException e) {
			System.err.println("Error: " + e.getMessage());
			if (e.getCause() != null) {
				System.err.println("Cause: " + e.getCause());
			}
			System.exit(1);
		}
	}
}
