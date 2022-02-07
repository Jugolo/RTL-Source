package rtl;

import rtl.exception.*;

import java.util.ArrayList;

public class CallableArgs {
	private CallableArgsData[] arg = new CallableArgsData[10];
	private int argI = 0;

	public void add(String name) throws RTLInterprenterException{
		this.put(new CallableArgsData(name));
	}
	
	public void add(String name, Expresion expresion) throws RTLInterprenterException{
		this.put(new CallableArgsData(name, expresion));
	}

	public void add(String type, String identify) throws RTLInterprenterException{
		this.put(new CallableArgsData(type, identify));
	}
	
	public void add(String type, String identify, Expresion expresion) throws RTLInterprenterException{
		this.put(new CallableArgsData(type, identify, expresion));
	}
	
	public void add(String type, String identify, Object value) throws RTLInterprenterException{
		this.put(new CallableArgsData(type, identify, value));
	}

	public Object[] set(Function function, Object[] arg, VariabelDatabase db, Program program) throws RTLException{
		return set(function.name, arg, db, program);
	}
	
	public Object[] set(Method method, Object[] arg, VariabelDatabase db, Program program) throws RTLException{
		return set(method.name, arg, db, program);
	}
	
	private Object[] set(String name, Object[] arg, VariabelDatabase db, Program program) throws RTLException{
		int length = this.argI < arg.length ? this.argI : arg.length;
		int i=0;
		Object[] result = new Object[this.argI < arg.length ? arg.length: this.argI];
		for(;i<length;i++){
			CallableArgsData data = this.arg[i];
			if(data.type != null)
				this.controleType(name, arg[i], data.type);
			db.get(data.identify).put(arg[i]);
			result[i] = arg[i];
		}
		int size = this.argI;
		//wee have now the total arguments there was given or maby wee miss some one??
		if(i<size){
			for(;i<size;i++){
				CallableArgsData data = this.arg[i];
				if(!data.hasDefault)
					throw new RTLRuntimeException("When called "+name+" it missed some argument");
				Object buf = this.getDefault(data.exp, program, db);
				db.get(data.identify).put(buf);
				result[i] = buf;
			}
		}else{
			for(;i<arg.length;i++)
				result[i] = arg[i];
		}
        
		return result;
	}
	
	private void put(CallableArgsData data) throws RTLInterprenterException{
		if(this.arg.length == this.argI)
			throw new RTLInterprenterException("Cant put more end "+this.arg.length+" args in a function");
		this.arg[this.argI] = data;
		this.argI++;
	}
	
	private Object getDefault(Object v, Program program, VariabelDatabase db) throws RTLException{
		if(v instanceof Expresion){
			return Reference.toValue(((Expresion)v).get(program, db));
		}
		return v;
	}

	private void controleType(String name, Object value, String type) throws RTLRuntimeException{
		if(value == null)
			return;
			
		RTLType nt = TypeConveter.type(value);

        if(nt == RTLType.NULL || nt == RTLType.STRUCTVALUE && ((StructValue)value).getName().equals(type))
			return;
			
		if(nt == RTLType.OBJECT && ((IObject)value).getOwner().getName().equals(type))
			return;

		if(!nt.toString().equals(type))
			throw new RTLRuntimeException("Unexpected argument type: "+nt+" expected "+type+" when called "+name);
	}

	class CallableArgsData{
		public final String identify;
		public final String type;
		public final Object exp;
		public final boolean hasDefault;

		public CallableArgsData(String name){
			this(null, name);
		}

		public CallableArgsData(String name, Object expresion){
			this(name, null, expresion);
		}
		
		public CallableArgsData(String type, String identify){
			this.type = type;
			this.identify = identify;
			this.hasDefault = false;
			this.exp = null;
		}

		public CallableArgsData(String type, String identify, Object expresion){
			this.identify = identify;
			this.type = type;
			this.exp = expresion;
			this.hasDefault = true;
		}
	}
}
