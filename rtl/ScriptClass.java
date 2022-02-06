package rtl;

import rtl.exception.RTLRuntimeException;
import java.util.HashMap;

class ScriptClass implements IClass{
	private String name;
	private HashMap<String, ScriptObjectPointer> pointer;
	
	public ScriptClass(String name, HashMap<String, ScriptObjectPointer> pointer){
		this.name = name;
		this.pointer = pointer;
	}
	
	public String getName(){
		return this.name;
	}
	
	public IObject newInstance(Object[] args) throws RTLRuntimeException{
		return new ScriptObject(this, this.pointer);
	}
}
