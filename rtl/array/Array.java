package rtl.array;

import rtl.exception.*;
import rtl.nativestruct.ArrayStruct;
import rtl.*;


import java.util.ArrayList;

public class Array implements IArray{
	private ArrayList<Object> list = new ArrayList<Object>();

    public Array(){}

    public Array(String[] arg){
    	for(int i=0;i<arg.length;i++){
    		this.list.add(arg[i]);
    	}
    }

	public void add(Program program, VariabelDatabase db, Object obj){
		this.list.add(obj);
	}

	public Object get(Program program, VariabelDatabase db, Object key) throws RTLRuntimeException{
		int i = TypeConveter.toInt(key);
		if(this.size(program, db) <= i || i < 0)
			throw new RTLRuntimeException("Index out of size: "+i);
		return this.list.get(i);
	}

	public int size(Program program, VariabelDatabase db){
		return this.list.size();
	}

	public void put(Program program, VariabelDatabase db, Object key, Object context) throws RTLRuntimeException{
		int i = TypeConveter.toInt(key);
		if(this.size(program, db)+1 < i || i < 0)
			throw new RTLRuntimeException("Index out of size: "+i);
		
		if(i == this.size(program, db))
			this.add(program, db, context);
		else
			this.list.set(i, context);
	}
	
	public StructValue toStructValue(Program program, VariabelDatabase db) throws RTLException{
		StructValue value = new StructValue(new ArrayStruct(), program, new Object[0]);
		((StructReference)value.get("length", program, db)).put(this.size(program, db));
		CallableArgs arg = new CallableArgs();
		arg.add("find");
		((StructReference)value.get("indexOf", program, db)).put(new Function("indexOf", new VariabelDatabase(), arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, Object _this, VariabelDatabase db) throws RTLException{
				for(int i=0;i<list.size();i++){
					if(RTLCompare.equal(arg[0], list.get(i)))
						return i;
				}
				return -1;
			}
		}));
		
		return value;
	}
}
