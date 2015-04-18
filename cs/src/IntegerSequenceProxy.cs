using System;
using omg.org.CosNaming;
using Ch.Elca.Iiop;
using System.Runtime.Remoting.Channels;
using Ch.Elca.Iiop.Services;
using omg.org.CORBA;
using System.Reflection;
using System.Collections.Generic;

namespace demo.impl {
	
	/// <summary>
	/// Proxy for integer sequence service. When accessing the service,
	/// proxy prints information about requests, received data, and timings.
	/// </summary>
	public class IntegerSequenceProxy : demo.IntegerSequence {

		private readonly demo.IntegerSequence _reference;

		/// <summary>
		/// Creates a proxy for a named Fibonacci numbers service.
		/// </summary>
		/// <param name="name">full name of the service</param>
		public IntegerSequenceProxy(demo.IntegerSequence reference, Name name) {
			this._reference = reference;
			this.CorbaName = name;
		}

		/// <summary>
		/// Name of the sequence in the CORBA system.
		/// </summary>
		public Name CorbaName {
			get;
			private set;
		}

		/// <summary>
		/// Do we need to shorten long numbers in output?
		/// </summary>
		public bool ShortenNumbers {
			get;
			set;
		}

		public string name {
			get {
				return this._reference.name;
			}
		}

		public string description {
			get {
				return this._reference.description;
			}
		}

		public int maxIndex {
			get {
				return this._reference.maxIndex;
			}
		}

		public Response number(int index) {
			Console.Error.WriteLine("Performing request {0}({1})", this, index);
			DateTime tStart = DateTime.Now;
			Response val = this._reference.number(index);
			Console.WriteLine("Request completed in {0} ms", (DateTime.Now - tStart).TotalMilliseconds);

			this.PrintValue(index, val);
			return val;
		}

		public Response[] numbers(int[] indices) {
			Console.WriteLine("Performing batch request {0}([{1}])",
					this, string.Join(", ", indices));
			DateTime tStart = DateTime.Now;
			Response[] values = this._reference.numbers(indices);
			Console.WriteLine("Request completed in {0} ms", (DateTime.Now - tStart).TotalMilliseconds);

			int i = 0;
			foreach (Response val in values) this.PrintValue(indices[i++], val);
			return values;
		}

		/// <summary>
		/// Prints information about a single requested member of a sequence.
		/// </summary>
		/// <param name="index">index of the member</param>
		/// <param name="val">server response</param>
		public void PrintValue(int index, Response val) {
			switch (val.Discriminator) {
				case ResponseType.t_error:
					Console.Error.WriteLine("Error getting {0}({1}): {2}", this, index, val.Getmessage());
					break;
				case ResponseType.t_int:
					Console.WriteLine("{0}({1}) = {2}", this, index, val.GetintVal());
					break;
				case ResponseType.t_string:
					string str = val.GetstringVal();
					if (this.ShortenNumbers && (str.Length > 50)) {
						str = str.Substring(0, 20) + string.Format("...[{0} digits skipped]...", str.Length - 40) 
							+ str.Substring(str.Length - 20);
					}
					Console.WriteLine("{0}({1}) = {2}", this, index, str);
					break;
			}
		}

		public override string ToString() {
			return this.CorbaName.ID;
		}
	}
}
