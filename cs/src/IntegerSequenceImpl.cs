using System;
using Ch.Elca.Iiop.Idl;

namespace demo.impl {

	/// <summary>
	/// The generic implementation of the integer sequence service.
	/// </summary>
	[SupportedInterface(typeof(demo.IntegerSequence))]
	public abstract class IntegerSequenceImpl : MarshalByRefObject, demo.IntegerSequence {

		protected IntegerSequenceImpl() {
		}

		public override object InitializeLifetimeService() {
			// Live forever
			return null;
		}

		public string name {
			get;
			protected set;
		}

		public int maxIndex {
			get;
			protected set;
		}

		public string description {
			get;
			protected set;
		}

		private Response GetNumber(int index) {
			try {
				if (index < 0) throw new IndexOutOfRangeException("Index cannot be negative");
				if (index > this.maxIndex) throw new IndexOutOfRangeException("Index is too big");

				return this.Compute(index);
			} catch (Exception e) {
				Console.WriteLine(e.Message);
				Response res = new Response();
				res.Setmessage(e.Message);
				return res;
			}
		}

		protected abstract Response Compute(int index);

		protected void Log(string message, params object[] args) {
			Console.WriteLine("[{0}] {1}", this, string.Format(message, args));
		}

		public Response number(int index) {
			Console.WriteLine("Requested number #{1} from sequence '{0}'", this, index);
			DateTime tStart = DateTime.Now;
			Response val = this.GetNumber(index);
			Console.WriteLine("Responded in {0} ms", (DateTime.Now - tStart).TotalMilliseconds);
			return val;
		}

		public Response[] numbers(int[] indices) {
			Console.WriteLine("Requested number(s) #[{1}] from sequence '{0}'", this, string.Join(", ", indices));

			DateTime tStart = DateTime.Now;
			Response[] res = new Response[indices.Length];
			for (int i = 0; i < indices.Length; i++) {
				res[i] = this.GetNumber(indices[i]);
			}
			Console.WriteLine("Responded in {0} ms", (DateTime.Now - tStart).TotalMilliseconds);

			return res;
		}

		public abstract Name CorbaName {
			get;
		}

		public override string ToString() {
			return this.name;
		}
	}
}
