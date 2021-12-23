package rtl;

import rtl.exception.*;

public class Function {
	public final String name;
	private VariabelDatabase proto;
	private CallableArgs arg;
	private ICallable callable;
	private String returnType = null;
	
	public Function(String name, VariabelDatabase proto, CallableArgs arg, ICallable callable){
		this(name, null, proto, arg, callable);
	}
	
	public Function(String name, String returnType, VariabelDatabase proto, CallableArgs arg, ICallable callable){
		this.arg = arg;
		this.proto = proto;
		this.name = name;
		this.callable = callable;
		this.returnType = returnType;
	}

	public Object call(Program program, Object[] arg) throws RTLException{
		VariabelDatabase db = new VariabelDatabase();
 		db.setLast(this.proto);
		Object result = this.callable.onCall(program, this.arg.set(this, arg, db, program), db);
		if(this.returnType != null){
			RTLType type = TypeConveter.type(result);
			if(type == RTLType.NULL || type == RTLType.STRUCTVALUE && ((StructValue)result).getName().equals(this.returnType))
				return result;
			if(!type.toString().equals(this.returnType))
				throw new RTLRuntimeException("The call to "+this.name+" result in wrong return type. Excpected "+this.returnType+" got "+TypeConveter.type(result));
		}
		return result;
	}
}
