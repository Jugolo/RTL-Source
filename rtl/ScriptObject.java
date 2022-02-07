package rtl;

import rtl.exception.*;

import java.util.*;

class ScriptObject implements IObject{
	private ScriptClass owner;
	private HashMap<String, ScriptObjectPointer> pointer;
	
	public ScriptObject(ScriptClass owner, HashMap<String, ScriptObjectPointer> pointer){
		this.owner   = owner;
		this.clonePointer(pointer);
	}
	
	public IClass getOwner(){
		return this.owner;
	}
	
	public Object getPointer(IObject caller, String name) throws RTLRuntimeException{
		if(!this.pointer.containsKey(name))
			throw new RTLRuntimeException("Unknown pointer '"+name+"' in the class: "+this.getOwner().getName());
			
		ScriptObjectPointer pointer = this.pointer.get(name);
		if(caller == null && pointer.access != ObjectAccess.PUBLIC){
			throw new RTLRuntimeException("Can access a "+pointer.access.toString()+" pointer outsite its object");
		}
		
		return new PointerReference(pointer);
	}
	
	public Object callMethod(IObject caller, String name, Program program, Object[] args) throws RTLException{
		return this.owner.getMethod(caller, name).call(program, args, this);
	}
	
	private void clonePointer(HashMap<String, ScriptObjectPointer> pointer){
		this.pointer = new HashMap<String, ScriptObjectPointer>();
		for(Map.Entry<String, ScriptObjectPointer> entry : pointer.entrySet()) {
			this.pointer.put(entry.getKey(), entry.getValue().clone());
		}
	}
}
