package rtl;

import rtl.exception.RTLRuntimeException;

class ScriptClass implements IClass{
	private String name;
	
	public ScriptClass(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public IObject newInstance(Object[] args) throws RTLRuntimeException{
		return new ScriptObject(this);
	}
}
