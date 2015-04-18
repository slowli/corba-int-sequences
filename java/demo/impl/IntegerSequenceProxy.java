package demo.impl;

import java.util.Arrays;
import java.util.Date;

import demo.IntegerSequence;
import demo.IntegerSequenceOperations;
import demo.Response;
import demo.ResponseType;

/**
 * Proxy for an integer sequence that addresses requests to the remote CORBA service.
 */
public class IntegerSequenceProxy implements IntegerSequenceOperations {

	private final IntegerSequence reference;
	
	/** Name of the service in the CORBA system. */
	public final Name corbaName;
	
	/** Do we need to shorten long numbers in output? */
	public final boolean shortenNumbers;
	
	public IntegerSequenceProxy(IntegerSequence reference, Name name, 
			boolean shortenNumbers) throws ServiceException {
		
		this.reference = reference;
		this.corbaName = name;
		this.shortenNumbers = shortenNumbers;
	}
	
	@Override
	public String name() {
		return this.reference.name();
	}

	@Override
	public String description() {
		return this.reference.description();
	}

	@Override
	public int maxIndex() {
		return this.reference.maxIndex();
	}

	@Override
	public Response number(int index) {
		System.out.format("Performing request %s(%d)\n", 
				this.corbaName.id, index);
		
		long tStart = new Date().getTime();
		Response val = this.reference.number(index);
		System.out.format("Request completed in %d ms\n", new Date().getTime() - tStart);
		this.printVal(index, val);
		return val;
	}

	@Override
	public Response[] numbers(int[] indices) {
		System.out.format("Performing batch request %s(%s)\n", 
				this.corbaName.id, Arrays.toString(indices));
		long tStart = new Date().getTime();
		Response[] values = this.reference.numbers(indices);
		System.out.format("Request completed in %d ms\n", new Date().getTime() - tStart);
		for (int i = 0; i < values.length; i++) {
			this.printVal(indices[i], values[i]);
		}
		return values;
	}

	public void printVal(int index, Response value) {
		if (value.discriminator().equals(ResponseType.t_error)) {
			System.out.format("Error getting %s(%s): %s\n", 
					this.corbaName.id, index, value.message());
		} else if (value.discriminator().equals(ResponseType.t_int)) {
			System.out.format("%s(%s) = %d\n", this.corbaName.id, index, value.intVal());
		} else if (value.discriminator().equals(ResponseType.t_string)) {
			String str = value.stringVal();
			int len = str.length();
			if ((len > 50) && this.shortenNumbers) {
				str = String.format("%s...[%d digits skipped]...%s", str.substring(0, 20),
						len - 40, str.substring(len - 20, len));
			}
			System.out.format("%s(%s) = %s\n", this.corbaName.id, index, str);
		}
	}
}
