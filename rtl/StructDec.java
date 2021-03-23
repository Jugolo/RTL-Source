package rtl;

import rtl.exception.RTLRuntimeException;

public class StructDec extends StructValue{

	public StructDec(Struct value){
		super(value);
	}

	public Object get(String name) throws RTLRuntimeException{
		switch(name){
			case "name":
				return this.owner.getName();
			default:
				throw new RTLRuntimeException("Unknown struct field name '"+name+"' in STRUCT_INFO");
		}
	}
}
