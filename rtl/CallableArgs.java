package rtl;

import rtl.exception.RTLRuntimeException;

import java.util.ArrayList;

public class CallableArgs {
	private ArrayList<CallableArgsData> arg = new ArrayList<CallableArgsData>();

	public void add(String name){
		this.arg.add(new CallableArgsData(name));
	}

	public void add(String type, String identify){
		this.arg.add(new CallableArgsData(type, identify));
	}

	public Object[] set(Function function, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
        //if there is less of the given arg end wee expect tell it here..
        if(arg.length < this.arg.size())
        	throw new RTLRuntimeException("Missing argument. Expected "+this.arg.size()+" got "+arg.length);

        for(int i=0;i<this.arg.size();i++){
        	CallableArgsData data = this.arg.get(i);
        	if(data.type != null)
        		this.controleType(function, arg[i], data.type);
        	db.get(data.identify).put(arg[i]);
        }
        
		return arg;
	}

	private void controleType(Function function, Object value, String type) throws RTLRuntimeException{
		if(type == null)
			return;
			
		String nt = TypeConveter.type(value);

		if(nt.equals("structValue") && ((StructValue)value).getName().equals(type))
			return;

		if(!nt.equals(type))
			throw new RTLRuntimeException("Unexpected argument type: "+nt+" expected "+type+" when called "+function.name);
	}

	class CallableArgsData{
		public final String identify;
		public final String type;

		public CallableArgsData(String name){
			this.identify = name;
			this.type = null;
		}

		public CallableArgsData(String type, String identify){
			this.identify = identify;
			this.type = type;
		}
	}
}
