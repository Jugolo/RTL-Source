package rtl;

import rtl.exception.RTLRuntimeException;

public class StructDec extends StructValue{
    private StructValue current;
    
	public StructDec(Struct value, StructValue current){
		super(value);
		this.current = current;
	}

	public Object get(String name) throws RTLRuntimeException{
		switch(name){
			case "name":
				return this.owner.getName();
			case "keys":
			    Array array = new Array();
			    StructItem[] fields = this.getFields();
			    for(int i=0;i<fields.length;i++){
					array.add(fields[i].field.name);
				}
				return array;
			case "values":
				Array varray = new Array();
				StructItem[] vfields = this.current.getFields();
				for(int i=0;i<vfields.length;i++){
					varray.add(vfields[i].value);
				}
				return varray;
			default:
				throw new RTLRuntimeException("Unknown struct field name '"+name+"' in STRUCT_INFO");
		}
	}
}
