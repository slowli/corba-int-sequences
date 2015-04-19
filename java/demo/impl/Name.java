package demo.impl;

import org.omg.CosNaming.NameComponent;

/**
 * Full name of the integer sequence service. The name consists of the two parts:
 * <ul>
 * <li>identifier of the sequence, e.g. "fib" (Fibonacci numbers)
 * <li>kind that describes implementation details, e.g. "naive-py" 
 * (Python programming language, naive implementation).
 * </ul>
 */
public class Name {

	/** Identifier of the sequence. */
	public final String id;
	
	/** Description of implementation details. */
	public final String kind;
	
	public Name(String id, String kind) {
		this.id = id;
		this.kind = kind;
	}
	
	Name(NameComponent component) {
		this.id = component.id;
		this.kind = component.kind;
	}
	
	/**
	 * Converts name to a list of components.
	 * 
	 * @return
	 *    array suitable for use in <code>NamingContext</code> methods
	 */
	public NameComponent[] toComponents() {
		return new NameComponent[] { new NameComponent(this.id, this.kind) };
	}
	
	public String toString() {
		return this.id + "." + this.kind;
	}
}
