package demo.impl.seq;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import demo.Response;
import demo.impl.IntegerSequenceImpl;
import demo.impl.Name;

public class FibonacciImpl extends IntegerSequenceImpl {

	public static final String NAME = "Fibonacci numbers (Java)";
	public static final String DESCRIPTION = 
		"Fibonacci numbers, defined by equalities\n"+
		"    fib(i) = fib(i-1) + fib(i-2), fib(0) = 0, fib(1) = 1.\n" + 
		"This implementation in Java uses 2x2 matrices and fast exponentation for calculations.\n" +
		"\n" +
		"See http://en.wikipedia.org/wiki/Fibonacci_number, http://oeis.org/A000045";
	public static final int MAX_INDEX = 5000000;
	
	private final Mat2 base = new Mat2(1, 1, 1, 0);

	public FibonacciImpl() {
		super(NAME, DESCRIPTION, MAX_INDEX);
	}
	
	@Override
	protected Response compute(int index) {
		String str = (index == 0) ? "0" : this.base.pow(index - 1).a00.toString();
		Response response = new Response();
		response.stringVal(str);
		return response;
	}

	@Override
	public Name nameForCORBA() {
		return new Name("fib", "java");
	}
}

class Mat2 {

	public static final Mat2 IDENTITY = new Mat2(1, 0, 0, 1);

	public final BigInteger a00;
	public final BigInteger a01;
	public final BigInteger a10;
	public final BigInteger a11;

	public Mat2(int a00, int a01, int a10, int a11) {
		this.a00 = BigInteger.valueOf(a00);
		this.a01 = BigInteger.valueOf(a01);
		this.a10 = BigInteger.valueOf(a10);
		this.a11 = BigInteger.valueOf(a11);
	}

	public Mat2(BigInteger a00, BigInteger a01, BigInteger a10, BigInteger a11) {
		this.a00 = a00;
		this.a01 = a01;
		this.a10 = a10;
		this.a11 = a11;
	}

	public Mat2 multiply(Mat2 other) {
		return new Mat2(
			this.a00.multiply(other.a00).add(this.a01.multiply(other.a10)),
			this.a00.multiply(other.a01).add(this.a01.multiply(other.a11)),
			this.a10.multiply(other.a00).add(this.a11.multiply(other.a10)),
			this.a10.multiply(other.a01).add(this.a11.multiply(other.a11))
		);
	}

	private Map<Integer, Mat2> powers2;

	private Mat2 pow2matrix(int pow2) {
		if (this.powers2 == null) {
			this.powers2 = new HashMap<Integer, Mat2>();
			this.powers2.put(1, this);
		}

        int i = pow2;
        while (!powers2.containsKey(i)) i >>= 1;

        while (i < pow2) {
            i <<= 1;
			Mat2 matrix = this.powers2.get(i / 2);
            this.powers2.put(i, matrix.multiply(matrix));
		}

        return this.powers2.get(pow2);
	}

	public Mat2 pow(int exponent) {
		Mat2 matrix = Mat2.IDENTITY;

        int pow2 = 1; 
        while (exponent > 0) {
            if (exponent % 2 == 1) {
                matrix = matrix.multiply(this.pow2matrix(pow2));
			}

            exponent >>= 1;
            pow2 <<= 1;
		}

        return matrix;
	}

	public String toString() {
		return String.format("[%d, %d, %d, %d]", a00, a01, a10, a11);
	}
}
