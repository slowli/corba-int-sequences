using System;
using System.Collections.Generic;
using System.Threading;
using demo.impl.seq;


namespace demo.impl {

	public class ServerProgram {

		private const string _Usage =
@"Usage: server [--list | --help]

Runs implementations of integer sequences.

Options:
    --list
        List implementations hosted by this server and exit.
    --help
        Print this help message and exit.";

		/// <summary>
		/// Directory containing integer sequence services.
		/// </summary>
		private readonly IntegerSequenceDir dir;

		/// <summary>
		/// Creates a new server program and parses arguments to determine a course of actions.
		/// </summary>
		/// <param name="args">command line arguments</param>
		public ServerProgram(string[] args) {
			this.dir = new IntegerSequenceDir(true);
			this.ProcessArgs(args);
		}

		private void ProcessArgs(string[] args) {
			int argi = 0;
			while (argi < args.Length) {
				string arg = args[argi++];

				if (arg == "--list") {
					this.ListImplementations();
					Environment.Exit(0);
				} else if (arg == "--help") {
					Console.WriteLine(_Usage);
					Environment.Exit(0);
				} else {
					throw new CLIArgumentException("Invalid argument: " + arg + ".");
				}
			}
		}

		private List<IntegerSequenceImpl> _Implementations;

		public List<IntegerSequenceImpl> Implementations {
			get {
				if (this._Implementations == null) {
					this._Implementations = new List<IntegerSequenceImpl>();
					_Implementations.Add(new FibonacciSeq());
					_Implementations.Add(new FactorialSeq());
					_Implementations.Add(new NaiveFactorialSeq());
				}

				return this._Implementations;
			}
		}

		private void ListImplementations() {
			Console.WriteLine("List of implementations hosted by this server:");
			foreach (IntegerSequenceImpl impl in this.Implementations) {
				Console.WriteLine(StringUtils.Delimiter);
				StringUtils.PrintInfo(impl, impl.CorbaName);
			}
		}

		/// <summary>
		/// Binds C# implementations of integer sequences. 
		/// All services, previously bound by this server, are unbound.
		/// </summary>
		public void Run() {
			this.dir.UnbindAll(name => name.Kind.EndsWith("cs"));

			foreach (IntegerSequenceImpl impl in this.Implementations) {
				this.dir.Bind(impl.CorbaName, impl);
			}

			Console.WriteLine("Ready for incoming requests...");
			Thread.Sleep(Timeout.Infinite);
		}

		private static int Main(string[] args) {
			try {
				ServerProgram server = new ServerProgram(args);
				server.Run();
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
