using System;
using System.Collections.Generic;


namespace demo.impl {

	public class ClientProgram {

		private const string _Usage =
@"Usage: client [option...] (sequence-ID | service-ID) index index...
       client (--list | --help)

Retrieves members of integer sequences using CORBA remoting.

Sequence ID is the identifier of an integer sequence, e.g. 'fib' (Fibonacci numbers).
Service ID is the identifier of a particular implementation of a sequence,
which consists of a sequence ID, dot '.', and a kind, e.g. 'pow3.naive-py'.

Indices are non-negative integers. Indexing starts with zero; e.g., 
fib(0) = 0 and fib(1) = 1. 

Options:
    --seq
         Perform a separate request for each index.
    --batch
         Perform a batch request for all indices (default).
    --short
         Print only 20 first and 20 last digits of received integers.
    --list
         Print the list of registered implementations and exit.
    --help
         Print this help message and exit.

Examples:
    client fib 5 6 7
    client --seq --short primes.py 10000 20000";

		/// <summary>
		/// Name of the sequence or a particular implementation to work with.
		/// </summary>
		private string SequenceName = null;

		/// <summary>
		/// Indices of sequence members to get.
		/// </summary>
		private int[] Indices;

		/// <summary>
		/// Use one batch call or separate calls for every index?
		/// </summary>
		private bool Batch = true;

		/// <summary>
		/// Shorten long numbers in output?
		/// </summary>
		private bool ShortenNumbers = false;

		/// <summary>
		/// Directory containing integer sequence services.
		/// </summary>
		private readonly IntegerSequenceDir dir;

		/// <summary>
		/// Creates a new client program and parses arguments to determine a course of actions.
		/// </summary>
		/// <param name="args">command line arguments</param>
		public ClientProgram(string[] args) {
			this.dir = new IntegerSequenceDir(false);
			this.ProcessArgs(args);
		}

		/// <summary>
		/// Processes arguments supplied to the program.
		/// </summary>
		/// <exception cref="CLIArgumentException">if arguments are invalid</exception>
		/// <exception cref="ServiceException">if an error occurs performing the task specified by arguments 
		/// (e.g., listing registered integer sequences)</exception>
		private void ProcessArgs(string[] args) {
			if (args.Length == 0) {
				throw new CLIArgumentException("No arguments specified.");
			}
		
			int argi = 0;
			while ((argi < args.Length) && args[argi].StartsWith("--")) {
				string arg = args[argi++];

				if (arg.Equals("--seq")) {
					this.Batch = false;
				} else if (arg.Equals("--batch")) {
					this.Batch = true;
				} else if (arg.Equals("--short")) {
					this.ShortenNumbers = true;
				} else if (arg.Equals("--help")) {
					Console.WriteLine(_Usage);
					Environment.Exit(0);
				} else if (arg.Equals("--list")) {
					this.ListServices();
					Environment.Exit(0);
				} else {
					throw new CLIArgumentException("Invalid argument: " + arg + ".");
				}
			}

			if (argi == args.Length) {
				throw new CLIArgumentException("No sequence specified.");
			} else {
				this.SequenceName = args[argi++];
			}
			
			int[] indices = new int[args.Length - argi];

			if (indices.Length > demo.MAX_QUERY_SIZE.ConstVal) {
				throw new CLIArgumentException(string.Format(
					"Too many indices specified. Specify no more than {0}",
					demo.MAX_QUERY_SIZE.ConstVal));
			}

			try {
				int i = 0;
				while (argi < args.Length) {
					indices[i++] = int.Parse(args[argi]);
					argi++;
				}
			} catch (FormatException) {
				throw new CLIArgumentException("Invalid sequence index: " + args[argi] + ".");
			}
			
			this.Indices = indices;
		}

		/// <summary>
		/// Lists summary for all integer sequence implementations registered in the system.
		/// </summary>
		public void ListServices() {
			List<Name> names = this.dir.ServiceNames;

			Console.WriteLine("Registered sequence implementations:");
			foreach (Name name in names) {
				Console.WriteLine(StringUtils.Delimiter);
				try {
					IntegerSequence reference = dir.Resolve(name);
					StringUtils.PrintInfo(reference, name);
				} catch (ServiceException ex) {
					StringUtils.PrintErroneousInfo(ex, name);
				}
			}
		}

		/// <summary>
		/// Retrieves members from an integer sequence. Both sequence and members are determined
		/// by parsing the arguments supplied to the client.
		/// </summary>
		public void Run() {
			Console.WriteLine("Getting service by sequence name '{0}'...", this.SequenceName);

			IntegerSequenceProxy proxy = null;

			Predicate<Name> matches = name => (name.ToString() == this.SequenceName) 
				|| (name.ID == this.SequenceName);

			foreach (Name name in dir.ServiceNames) {
				if (matches(name)) {
					try {
						proxy = new IntegerSequenceProxy(dir.Resolve(name), name);
						proxy.ShortenNumbers = this.ShortenNumbers;
						break;
					} catch (ServiceException) {
						Console.Error.WriteLine("Error accessing service {0}", name);
						proxy = null;
					}
				}
			}

			if (proxy == null) {
				throw new ServiceException(string.Format("No available services that match the name '{0}'", 
					this.SequenceName));
			}
			Console.WriteLine("Connected to service '{0}' (CORBA name: {1})", proxy.name, proxy.CorbaName);

			if (this.Batch) {
				proxy.numbers(this.Indices);
			} else {
				foreach (int idx in this.Indices) proxy.number(idx);
			}
		}

		private static int Main(string[] args) {
			try {
				ClientProgram client = new ClientProgram(args);
				client.Run();
				return 0;
			} catch (CLIArgumentException e) {
				Console.Error.WriteLine(e.Message + "\nInvoke with `--help` option to get help.");
				return 2;
			} catch (ServiceException e) {
				Console.Error.WriteLine("Error: " + e.Message);
				if (e.InnerException != null) {
					Console.Error.WriteLine("Cause: " + e.InnerException.Message);
				}
				return 1;
			}
		}
	}
}
