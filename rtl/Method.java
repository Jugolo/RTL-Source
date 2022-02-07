package rtl;

import rtl.exception.RTLException;

class Method{
	private IMethodCallable callable;
	private VariabelDatabase proto;
	private CallableArgs arg;
	public final String name;
	
	public Method(String name, VariabelDatabase proto, CallableArgs arg, IMethodCallable callable){
		this.callable = callable;
		this.proto    = proto;
		this.arg = arg;
		this.name = name;
	}
	
	public Object call(Program program, Object[] arg, IObject caller) throws RTLException{
		VariabelDatabase db = new VariabelDatabase(caller);
		db.setLast(this.proto);
		this.arg.set(this, arg, db, program);
		return this.callable.call(program, db, arg, caller);
	}
}
