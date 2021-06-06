package rtl;

import rtl.exception.RTLRuntimeException;

import java.util.ArrayList;

public class CallableArgs {
	private ArrayList<CallableArgsData> arg = new ArrayList<CallableArgsData>();

	public void add(String name){
		this.arg.add(new CallableArgsData(name));
	}
	
	public void add(String name, Expresion expresion){
		this.arg.add(new CallableArgsData(name, expresion));
	}

	public void add(String type, String identify){
		this.arg.add(new CallableArgsData(type, identify));
	}
	
	public void add(String type, String identify, Expresion expresion){
		this.arg.add(new CallableArgsData(type, identify, expresion));
	}
	
	public void add(String type, String identify, Object value){
		this.arg.add(new CallableArgsData(type, identify, value));
	}

	public Object[] set(Function function, Object[] arg, VariabelDatabase db, Program program) throws RTLRuntimeException{
		int length = this.arg.size() < arg.length ? this.arg.size() : arg.length;
		int i=0;
		Object[] result = new Object[this.arg.size() < arg.length ? arg.length: this.arg.size()];
		for(;i<length;i++){
			CallableArgsData data = this.arg.get(i);
			if(data.type != null)
				this.controleType(function, arg[i], data.type);
			db.get(data.identify).put(arg[i]);
			result[i] = arg[i];
		}
		//wee have now the total arguments there was given or maby wee miss some one??
		for(;i<this.arg.size();i++){
			CallableArgsData data = this.arg.get(i);
			if(data.exp == null)
				throw new RTLRuntimeException("When called "+function.name+" it missed some argument");
			Object buf = this.getDefault(data.exp, program, db);
			db.get(data.identify).put(buf);
			result[i] = buf;
		}
		
		for(;i<arg.length;i++)
			result[i] = arg[i];
        
		return result;
	}
	
	private Object getDefault(Object v, Program program, VariabelDatabase db) throws RTLRuntimeException{
		if(v instanceof Expresion){
			return Reference.toValue(((Expresion)v).get(program, db));
		}
		return v;
	}

	private void controleType(Function function, Object value, String type) throws RTLRuntimeException{
		if(value == null)
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
		public final Object exp;

		public CallableArgsData(String name){
			this(null, name, null);
		}

		public CallableArgsData(String name, Object expresion){
			this(name, null, expresion);
		}
		
		public CallableArgsData(String type, String identify){
			this(type, identify, null);
		}

		public CallableArgsData(String type, String identify, Object expresion){
			this.identify = identify;
			this.type = type;
			this.exp = expresion;
		}
	}
}
