package rtl;

import rtl.exception.*;
import rtl.array.Array;

public class StructDec extends StructValue{
    private StructValue current;
    
	public StructDec(Struct value, StructValue current, Program program) throws RTLException{
		super(value, program, new Object[0]);
		this.current = current;
	}

	public Object get(String name, Program program, VariabelDatabase db) throws RTLException{
		CallableArgs arg;
		switch(name){
			case "name":
				return this.owner.getName();
			case "keys":
			    Array array = new Array();
			    StructItem[] fields = this.getFields();
			    for(int i=0;i<fields.length;i++){
					array.add(program, db, fields[i].field.name);
				}
				return array;
			case "values":
				Array varray = new Array();
				StructItem[] vfields = this.current.getFields();
				for(int i=0;i<vfields.length;i++){
					varray.add(program, db, vfields[i].value);
				}
				return varray;
			case "set":
			    arg = new CallableArgs();
			    arg.add("string", "key");
			    arg.add("value");
			    return new Function("<structset>", new VariabelDatabase(), arg, new ICallable(){
					public Object onCall(Program program, Object[] arg, Object _this, VariabelDatabase db) throws RTLException{
						Reference.toReference(current.get(TypeConveter.string(arg[0]), program, db)).put(arg[1]);
						return null;
					}
				});
			case "get":
			    arg = new CallableArgs();
			    arg.add("string", "key");
			    return new Function("<structget>", new VariabelDatabase(), arg, new ICallable(){
					public Object onCall(Program program, Object[] arg, Object _this, VariabelDatabase db) throws RTLException{
						return Reference.toReference(current.get(TypeConveter.string(arg[0]), program, db)).toValue();
					}
				});
			default:
				throw new RTLRuntimeException("Unknown struct field name '"+name+"' in STRUCT_INFO");
		}
	}
}
