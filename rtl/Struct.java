package rtl;

import rtl.exception.RTLRuntimeException;

public class Struct {
	private String[] fields;
	private int size;
	private String name;

	public Struct(String name, String[] fields){
		this.size = fields.length;
		this.fields = fields;
		this.name = name;
	}

	public int getSize(){
		return this.size;
	}

	public String[] getNames(){
		return this.fields;
	}

	public String getName(){
		return this.name;
	}
}
