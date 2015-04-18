package demo.impl.seq;

import java.math.BigInteger;

import demo.Response;
import demo.impl.IntegerSequenceImpl;
import demo.impl.Name;

/**
 * Fibonacci numbers implemented using the definition.
 */
public class FibonacciNaiveImpl extends IntegerSequenceImpl {

	public static final String NAME = "Fibonacci numbers, naive (Java)";
	public static final String DESCRIPTION = 
		"Fibonacci numbers, defined by equalities\n"+
		"    fib(i) = fib(i-1) + fib(i-2), fib(0) = 0, fib(1) = 1.\n" +
		"This implementation in Java uses the definition for calculations, which is rather ineffective.\n" +
		"\n" +
		"See http://en.wikipedia.org/wiki/Fibonacci_number, http://oeis.org/A000045";
	public static final int MAX_INDEX = 5000000;
	
	public FibonacciNaiveImpl() {
		super(NAME, DESCRIPTION, MAX_INDEX);
	}

	@Override
	protected Response compute(int index) throws Exception {
		BigInteger a = BigInteger.ZERO, b = BigInteger.ONE;
		
		for (int i = 1; i < index; i++) {
			BigInteger t = b;
			b = a.add(b);
			a = t;
		}
		
		String str = (index == 0) ? a.toString() : b.toString();
		Response response = new Response();
		response.stringVal(str);
		return response;
	}

	@Override
	public Name corbaName() {
		return new Name("fib", "naive-java");
	}
}
