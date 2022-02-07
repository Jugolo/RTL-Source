package rtl;

import rtl.exception.*;
import java.util.HashMap;

class ScriptClass implements IClass{
	private String name;
	private HashMap<String, ScriptObjectPointer> pointer;
	private HashMap<String, MethodInfo> method;
	private Method constructor;
	
	public ScriptClass(String name, Method constructor, HashMap<String, ScriptObjectPointer> pointer, HashMap<String, MethodInfo> method){
		this.name = name;
		this.pointer = pointer;
		this.constructor = constructor;
		this.method = method;
	}
	
	public String getName(){
		return this.name;
	}
	
	public IObject newInstance(Object[] args, Program program) throws RTLException{
		ScriptObject obj = new ScriptObject(this, this.pointer);
		if(this.constructor != null)
			this.constructor.call(program, args, obj);
		return obj;
	}
	
	public Method getMethod(IObject caller, String name) throws RTLRuntimeException{
		if(!this.method.containsKey(name))
			throw new RTLRuntimeException("Unknown method '"+name+"' in class "+this.getName());
		MethodInfo info = this.method.get(name);
		if(caller == null && info.access.equals("private"))
			throw new RTLRuntimeException("You can not call method "+name+" outsite the class");
		return this.method.get(name).method;
	}
}
