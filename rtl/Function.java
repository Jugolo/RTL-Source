package rtl;

import rtl.exception.RTLRuntimeException;

public class Function {
	public final String name;
	private VariabelDatabase proto;
	private CallableArgs arg;
	private ICallable callable;
	
	public Function(String name, VariabelDatabase proto, CallableArgs arg, ICallable callable){
		this.arg = arg;
		this.proto = proto;
		this.name = name;
		this.callable = callable;
	}

	public Object call(Program program, Object[] arg) throws RTLRuntimeException{
		if(this.arg.count() > arg.length)
			throw new RTLRuntimeException(this.name+" got to few arguments");

 		VariabelDatabase db = new VariabelDatabase();
 		for(int i=0;i<this.arg.count();i++){
 			db.get(this.arg.getName(i)).put(arg[i]);
 		}

 		db.setLast(this.proto);
		
		return this.callable.onCall(program, arg, db);
	}
}
