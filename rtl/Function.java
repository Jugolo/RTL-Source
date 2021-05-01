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
		VariabelDatabase db = new VariabelDatabase();
 		db.setLast(this.proto);
		return this.callable.onCall(program, this.arg.set(this, arg, db), db);
	}
}
