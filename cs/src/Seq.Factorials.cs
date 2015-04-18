using System.Collections.Generic;
using System.Numerics;
using System;

namespace demo.impl.seq {

	public class FactorialSeq : IntegerSequenceImpl {

		public FactorialSeq() : base() {
			this.name = "Factorials (C#)";
			this.maxIndex = 100000;
			this.description = 
@"Factorials
    n! = 1 * 2 * ... * n;  0! = 1.
This implementation in C# uses recursive splitting technique.";
		}

		public override Name CorbaName {
			get {
				return new Name() { ID = "fac", Kind = "cs" };
			}
		}

		protected override Response Compute(int index) {
			Response result = new Response();
			BigInteger val = this.Factorial(index);
			result.SetstringVal(val.ToString());
			return result;
		}

		/// <summary>
		/// Returns n, if n is odd; else returns n - 1.
		/// </summary>
		private int NearestOdd(int n) {
			return n - (n + 1) % 2;
		}

		public BigInteger Factorial(int n) {
			if (n < 2) return BigInteger.One;

			List<int> bounds = new List<int>();
			bounds.Add(n);
			while (n > 1) {
				bounds.Add(n >>= 1);
			}
			bounds.Reverse();

			int shift = 0;
			BigInteger prod = BigInteger.One, oddProd = BigInteger.One;
			for (int i = 0; i < bounds.Count - 1; i++) {
				oddProd *= OddsProduct(NearestOdd(bounds[i]) + 2, NearestOdd(bounds[i + 1]));
				prod *= oddProd;
				shift += bounds[i];
			}

			return prod << shift;
		}

		/// <summary>
		/// Calculates product of odd numbers in the given range, including the bounds,
		/// i.e. <c>low * (low+2) * ... * (high - 2) * high</c>.
		/// </summary>
		/// <param name="low"></param>
		/// <param name="high"></param>
		/// <returns></returns>
		private BigInteger OddsProduct(int low, int high) {
			if (high < low) return BigInteger.One;
			if (high == low) return low;
			if (high == low + 2) return ((long) low) * high;

			int m = NearestOdd((low + high) / 2);
			return OddsProduct(low, m) * OddsProduct(m + 2, high);
		}
	}

	public class NaiveFactorialSeq : IntegerSequenceImpl {

		public NaiveFactorialSeq() : base() {
			this.name = "Factorials, naive (C#)";
			this.maxIndex = 100000;
			this.description =
@"Factorials
    n! = 1 * 2 * ... * n;  0! = 1.
This implementation in C# uses multiplication and is rather slow for big n."; ;
		}

		public override Name CorbaName {
			get {
				return new Name() { ID = "fac", Kind = "naive-cs" };
			}
		}

		protected override Response Compute(int index) {
			Response result = new Response();
			BigInteger val = this.Factorial(index);
			result.SetstringVal(val.ToString());
			return result;
		}

		
		public BigInteger Factorial(int n) {
			BigInteger prod = BigInteger.One;
			for (int i = 2; i <= n; i++) {
				prod *= i;
			}
			return prod;
		}
	}
}
