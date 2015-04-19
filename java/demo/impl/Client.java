package demo.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.LogManager;

import org.omg.CORBA.SystemException;


public final class Client {

	/** Working modes of the client application. */
	private static enum Mode {
		/** Query an integer sequence implementation. */
		QUERY,
		/** List implementations. */
		LIST,
		/** Print help message. */
		HELP
	}
	
	private static final String USAGE =
		"Usage: client [option...] (sequence-ID | service-ID) index index...\n" +
		"       client (--list | --help)\n" +
		"\n" +
		"Retrieves members of integer sequences using CORBA remoting.\n" +
		"\n" +
		"Sequence ID is the identifier of an integer sequence, e.g. 'fib' (Fibonacci numbers).\n" +
		"Service ID is the identifier of a particular implementation of a sequence,\n" +
		"which consists of a sequence ID, dot '.', and a kind, e.g. 'pow3.naive-py'.\n" +
		"\n" +
		"Indices are non-negative integers. Indexing starts with zero; e.g.,\n" +
		"fib(0) = 0 and fib(1) = 1.\n" +
		"\n" +
		"Options:\n" +
		"    --seq\n" +
		"         Perform a separate request for each index.\n" +
		"    --batch\n" +
		"         Perform a batch request for all indices (default).\n" +
		"    --short\n" +
		"         Print only 20 first and 20 last digits of received integers.\n" +
		"    --list\n" +
		"         Print the list of registered implementations and exit.\n" +
		"    --help\n" +
		"         Print this help message and exit.\n" +
		"\n" +
		"Examples:\n" +
		"    client fib 5 6 7\n" +
		"    client --seq --short primes.py 10000 20000";
	
	private static final String DELIMITER = "--------------------------------------------------";

	public static void printInfo(demo.IntegerSequenceOperations sequence, Name corbaName) {
		System.out.format("Sequence ID: %s, kind: %s\n", corbaName.id, corbaName.kind);
		System.out.println("Name: " + sequence.name());
		System.out.println("Maximal supported index: " + sequence.maxIndex());
		System.out.println("Description:\n" + sequence.description());
	}
	
	public static void printErroneousInfo(ServiceException e, Name corbaName) {
		System.out.format("Sequence ID: %s, kind: %s\n", corbaName.id, corbaName.kind);
		System.out.println(e.getMessage());
	}
	
	/** Working mode of the application. */
	private Mode mode = Mode.QUERY;

	/** Name of the sequence or a particular implementation to work with. */
	private String sequenceName = "";
	
	/** Indices of sequence members to get. */
	private int[] indices = null;

	/** Use one batch call or separate calls for every index? */
	private boolean batch = true;
	
	/** Shorten long numbers in output? */
	private boolean shortenNumbers = false;
	
	/** Directory containing integer sequence services. */
	private final IntegerSequenceDir dir;
	
	/**
	 * Creates a client program and parses supplied command line arguments to determine
	 * the course of action. 
	 * 
	 * @param args
	 *    CLI arguments
	 * @throws CLIArgumentException
	 *    if an error occurs parsing arguments
	 */
	public Client(String[] args) throws CLIArgumentException {
		this.dir = new IntegerSequenceDir(false);
		
		// Turn off CORBA logging as it is rather tedious
		final LogManager lman = LogManager.getLogManager();
		lman.reset();
		
		processArgs(args);
	}
	
	/**
	 * Processes arguments supplied to the program.
	 */
	public void processArgs(String[] args) throws CLIArgumentException {
		if (args.length == 0) {
			throw new CLIArgumentException("No arguments specified.");
		}
		
		int argi = 0;
		while ((argi < args.length) && args[argi].startsWith("--")) {
			final String arg = args[argi];
			
			if (arg.equals("--seq")) {
				this.batch = false;
			} else if (arg.equals("--batch")) {
				this.batch = true;
			} else if (arg.equals("--short")) {
				this.shortenNumbers = true;
			} else if (arg.equals("--list")) {
				this.mode = Mode.LIST;
				return;
			} else if (arg.equals("--help")) {
				this.mode = Mode.HELP;
				return;
			} else {
				throw new CLIArgumentException("Unknown argument: " + arg + ".");
			}
			
			argi++;
		}
		
		if (argi == args.length) {
			throw new CLIArgumentException("Sequence name not specified");
		}
		this.sequenceName = args[argi++];
		
		indices = new int[args.length - argi];
		
		if (indices.length > demo.MAX_QUERY_SIZE.value) {
			throw new CLIArgumentException(String.format(
					"Too many indices specified. Specify no more than %d", 
					demo.MAX_QUERY_SIZE.value));
		}
		
		try {
			int i = 0;
			while (argi < args.length) {
				indices[i++] = Integer.parseInt(args[argi]);
				argi++;
			}
		} catch (NumberFormatException e) {
			throw new CLIArgumentException("Invalid sequence index: " + args[argi] + ".");
		}
	}
	
	/** 
	 * Lists summary for all integer sequence implementations registered in the system. 
	 */
	private void listServices() throws ServiceException {
		Collection<Name> names = dir.serviceNames();
		System.out.println("Registered sequence implementations:");
		
		for (Name name : names) {
			System.out.println(DELIMITER);
			try {
				demo.IntegerSequence reference = dir.resolve(name);
				printInfo(reference, name);
			} catch (ServiceException e) {
				printErroneousInfo(e, name);
			}
		}
	}
	
	/**
	 * Checks if the CORBA name of an integer sequence matches the one specified by the user.
	 * 
	 * @param name
	 *    CORBA name to check
	 * @return
	 */
	private boolean matches(Name name) {
		return name.id.equals(this.sequenceName) || name.toString().equals(this.sequenceName);
	}
	
	/**
	 * Requests members from a remote integer sequence service.
	 * 
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation
	 */
	private void querySequence() throws ServiceException {
		System.out.format("Getting service by sequence name '%s'...\n", this.sequenceName);
		
		IntegerSequenceProxy proxy = null;
		for (Name name : dir.serviceNames()) {
			if (this.matches(name)) {
				try {
					proxy = new IntegerSequenceProxy(dir.resolve(name), name, shortenNumbers);
					break;
				} catch (ServiceException e) {
					System.err.format("Error accessing service %s\n", name);
					proxy = null;
				}
			}
		}

		if (proxy == null) {
			throw new ServiceException(String.format("No available services that match the name '%s'", 
					this.sequenceName));
		}
		System.out.format("Connected to service '%s' (CORBA name: %s)\n", proxy.name(), proxy.corbaName);

		try {
			if (batch) {
				proxy.numbers(this.indices);
			} else {
				for (int idx : this.indices) proxy.number(idx);
			}
		} catch (SystemException e) {
			throw new ServiceException("Error processing response", e);
		}
	}
	
	/**
	 * Runs the server program.
	 * 
	 * @throws ServiceException
	 *    if a CORBA-related error occurs during operation
	 */
	public void run() throws ServiceException {
		switch (this.mode) {
			case QUERY:
				this.querySequence();
				return;
			case LIST:
				this.listServices();
				return;
			case HELP:
				System.out.println(USAGE);
				return;
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			Client client = new Client(args);
			client.run();
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
