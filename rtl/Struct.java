package rtl;

import rtl.exception.RTLRuntimeException;

public class Struct {
	private StructField[] fields;
	private int size;
	private String name;
	
	public Struct(String name, String[] fields){
		this.size = fields.length;
		this.name = name;
		StructField[] buffer = new StructField[this.size];
		for(int i=0;i<buffer.length;i++){
			StructField buf = new StructField();
			buf.name = fields[i];
			buffer[i] = buf;
		}
		this.fields = buffer;
	}

	public Struct(String name, StructField[] fields){
		this.size = fields.length;
		this.fields = fields;
		this.name = name;
	}

	public int getSize(){
		return this.size;
	}

	public StructField[] getFields(){
		return this.fields;
	}

	public String getName(){
		return this.name;
	}
}
