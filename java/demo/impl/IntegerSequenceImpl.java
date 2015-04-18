package demo.impl;

import java.util.Arrays;
import java.util.Date;

import demo.Response;

/**
 * The generic implementation of the integer sequence service.
 */
public abstract class IntegerSequenceImpl extends demo.IntegerSequencePOA {

	private final String name;
	
	private final String description;
	
	private final int maxIndex;
	
	protected IntegerSequenceImpl(String name, String description, int maxIndex) {
		this.name = name;
		this.description = description;
		this.maxIndex = maxIndex;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public int maxIndex() {
		return maxIndex;
	}
	
	private Response get(int index) {
		Response response = new Response();
		try {
			if (index < 0) {
				throw new ServiceException("Index cannot be negative");
			} else if (index > this.maxIndex()) {
				throw new ServiceException("Index is too big");
			}
			
			response = this.compute(index);
		} catch (Exception e) {
			response.message(e.getMessage());
		}
		
		return response;
	}

	@Override
	public Response number(int index) {
		System.out.format("Requested number #%d from sequence '%s'\n", index, this.name());
		long tStart = new Date().getTime();
		Response response = this.get(index);
		System.out.format("Responded in %d ms\n", new Date().getTime() - tStart);
		return response;
	}

	@Override
	public Response[] numbers(int[] indices) {
		System.out.format("Requested number(s) #%s from sequence '%s'\n", 
				Arrays.toString(indices), this.name());

		long tStart = new Date().getTime();
		Response[] responses = new Response[indices.length];
		for (int i = 0; i < indices.length; i++) {
			responses[i] = this.get(indices[i]);
		}
		System.out.format("Responded in %d ms\n", new Date().getTime() - tStart);
		return responses;
	}
	
	protected abstract Response compute(int index) throws Exception;
	
	public abstract Name nameForCORBA();
}
