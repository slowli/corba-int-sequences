using System.Collections.Generic;
using System.Numerics;

namespace demo.impl.seq {

	public class FibonacciSeq : IntegerSequenceImpl {

		private readonly mat2 _BaseM = new mat2(1, 1, 1, 0);

		public FibonacciSeq() : base() {
			this.name = "Fibonacci numbers (C#)";
			this.maxIndex = 1000000;
			this.description = 
@"Fibonacci numbers, defined by equalities
    fib(i) = fib(i-1) + fib(i-2), fib(0) = 0, fib(1) = 1.
This implementation in C# uses 2x2 matrices and fast exponentation for calculations.

See http://en.wikipedia.org/wiki/Fibonacci_number, http://oeis.org/A000045";
		}

		public override Name CorbaName {
			get {
				return new Name() { ID = "fib", Kind = "cs" };
			}
		}

		protected override Response Compute(int index) {
			Response result = new Response();
			if (index == 0) {
				result.SetintVal(0);
			} else {
				mat2 pow = _BaseM.Pow(index - 1);
				result.SetstringVal(pow.a00.ToString());
			}
			return result;
		}
	}

	internal class mat2 {

		public static mat2 Identity() { return new mat2(1, 0, 0, 1); }

		public readonly BigInteger a00, a01, a10, a11;

		private Dictionary<int, mat2> _Powers2;

		public mat2(int a00, int a01, int a10, int a11) {
			this.a00 = new BigInteger(a00);
			this.a01 = new BigInteger(a01);
			this.a10 = new BigInteger(a10);
			this.a11 = new BigInteger(a11);
		}

		public mat2(BigInteger a00, BigInteger a01, BigInteger a10, BigInteger a11) {
			this.a00 = a00;
			this.a01 = a01;
			this.a10 = a10;
			this.a11 = a11;
		}

		public static mat2 operator *(mat2 mat, mat2 other) {
			return new mat2(mat.a00 * other.a00 + mat.a01 * other.a10,
				mat.a00 * other.a01 + mat.a01 * other.a11,
				mat.a10 * other.a00 + mat.a11 * other.a10,
				mat.a10 * other.a01 + mat.a11 * other.a11);
		}

		private mat2 Pow2Matrix(int pow2) {
			if (this._Powers2 == null) {
				this._Powers2 = new Dictionary<int, mat2>();
				this._Powers2.Add(1, this);
			}

			int i = pow2;
			while (!this._Powers2.ContainsKey(i)) i >>= 1;

			while (i < pow2) {
				i <<= 1;
				this._Powers2[i] = this._Powers2[i / 2] * this._Powers2[i / 2];
			}

			return this._Powers2[pow2];
		}

		public mat2 Pow(int exponent) {
			int pow2 = 1;

			mat2 m = Identity();
			while (exponent > 0) {
				if (exponent % 2 == 1) {
					m *= this.Pow2Matrix(pow2);
				}

				pow2 <<= 1;
				exponent >>= 1;
			}

			return m;
		}

		public override string ToString() {
			return string.Format("[{0}, {1}, {2}, {3}]", a00, a01, a10, a11);
		}
	}
}
