package org.davebcn.ukemi.store;

import org.davebcn.ukemi.Identifier;

public class StringIdentifier implements Identifier{
	
	
	private static final long serialVersionUID = -9084980249009816037L;
	private String id;

	public StringIdentifier(String id) {
		super();
		this.id = id;
	}
	
	public String toString(){
		return id.toString();
	}
	
	public boolean equals(Object o){
		return o != null && o instanceof StringIdentifier && ((StringIdentifier)o).id.equals(this.id);
	}
	
	@Override
	public int hashCode(){
		return this.id.hashCode();
	}
}
