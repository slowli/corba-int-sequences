package demo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import demo.impl.seq.FibonacciImpl;
import demo.impl.seq.FibonacciNaiveImpl;
import demo.impl.seq.RandomPrimeImpl;

public class Server {

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
	
	public Server(String[] args) throws CLIArgumentException {
		this.processArgs(args);
		this.dir = new IntegerSequenceDir(true);
		
		// Turn off CORBA logging as it is rather tedious
		final LogManager lman = LogManager.getLogManager();
		lman.reset();
	}
	
	private void processArgs(String[] args) throws CLIArgumentException {
		int argi = 0;
		while (argi < args.length) {
			final String arg = args[argi++];
			
			if (arg.equals("--list")) {
				this.listImplementations();
				System.exit(0);
			} else if (arg.equals("--help")) {
				System.out.println(USAGE);
				System.exit(0);
			} else {
				throw new CLIArgumentException("Invalid argument: " + arg + ".");
			}
		}
	}
	
	private List<IntegerSequenceImpl> getImplementations() {
		List<IntegerSequenceImpl> implementations = new ArrayList<IntegerSequenceImpl>();
		implementations.add(new FibonacciImpl());
		implementations.add(new FibonacciNaiveImpl());
		implementations.add(new RandomPrimeImpl());
		return implementations;
	}
	
	public void listImplementations() {
		System.out.println("List of implementations hosted by this server:");
		for (IntegerSequenceImpl impl : this.getImplementations()) {
			System.out.println(DELIMITER);
			Client.printInfo(impl, impl.corbaName());
		}
	}
	
	public void run() throws ServiceException {
		this.dir.unbindAll(new ServiceDirectory.NameFilter() {
		
			public boolean matches(Name name) {
				return name.kind.endsWith("java");
			}
		});
		for (IntegerSequenceImpl impl : this.getImplementations()) {
			this.dir.bind(impl.corbaName(), impl);
		}
		
		System.out.println("Ready for incoming requests...");
		Config.orb().run();
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
