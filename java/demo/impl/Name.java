package demo.impl;

import org.omg.CosNaming.NameComponent;

/**
 * Full name of the integer sequence service. The name consists of the two parts:
 * <ul>
 * <li>identifier of the sequence, e.g. "fibonacci"
 * <li>kind that describes implementation details, e.g. "naive-py".
 * </ul>
 */
public class Name {

	public final String id;
	
	public final String kind;
	
	final NameComponent component;
	
	public Name(String id, String kind) {
		this.id = id;
		this.kind = kind;
		this.component = null;
	}
	
	Name(NameComponent component) {
		this.id = component.id;
		this.kind = component.kind;
		this.component = component;
	}
	
	public boolean matches(String pattern) {
		return pattern.equals(id) || pattern.equals(this.toString());
	}
	
	public NameComponent[] toComponents() {
		return new NameComponent[] { new NameComponent(this.id, this.kind) };
	}
	
	public String toString() {
		return this.id + "." + this.kind;
	}
}
