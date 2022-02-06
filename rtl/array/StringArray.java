package rtl.array;

import rtl.exception.RTLRuntimeException;
import rtl.*;

public class StringArray implements IArray{
	private String str;
	
	public StringArray(String str){
		this.str = str;
	}
	
	public Object get(Program program, VariabelDatabase db, Object key) throws RTLRuntimeException{
		int i = TypeConveter.toInt(key);
		int size = this.str.length();
		if(size <= i || i < 0)
			throw new RTLRuntimeException("Index out of size: "+i);
		return this.str.charAt(i)+"";
	}
	
	public void add(Program program, VariabelDatabase db, Object obj) throws RTLRuntimeException{
		throw new RTLRuntimeException("Cant add a element to StringArray");
	}
	
	public void put(Program program, VariabelDatabase db, Object key, Object context) throws RTLRuntimeException{
		throw new RTLRuntimeException("Cant add a element to StringArray");
	}
	
	public int size(Program program, VariabelDatabase db){
		return this.str.length();
	}
}
