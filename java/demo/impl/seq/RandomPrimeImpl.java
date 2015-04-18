package demo.impl.seq;

import java.math.BigInteger;
import java.util.Random;

import demo.Response;
import demo.impl.IntegerSequenceImpl;
import demo.impl.Name;

public class RandomPrimeImpl extends IntegerSequenceImpl  {

	public static final String NAME = "Random primes (Java)";
	public static final String DESCRIPTION = 
		"Returns probably prime number with the given bit length.\n" +
		"This implementation uses BigInteger.probablePrime method for calculations.";
	public static final int MAX_INDEX = 5000;
	
	private final Random random;
	
	public RandomPrimeImpl() {
		super(NAME, DESCRIPTION, MAX_INDEX);
		this.random = new Random();
	}

	@Override
	protected Response compute(int index) throws Exception {
		this.random.setSeed(0); // Maintain same responses for identical requests
		String str = BigInteger.probablePrime(index, this.random).toString();
		Response response = new Response();
		response.stringVal(str);
		return response;
	}

	@Override
	public Name nameForCORBA() {
		return new Name("rnd-prime", "java");
	}
}
