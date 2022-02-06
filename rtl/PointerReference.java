package rtl;

class PointerReference implements IReference{
	private ScriptObjectPointer pointer;
	
	public PointerReference(ScriptObjectPointer pointer){
		this.pointer = pointer;	
	}
	
	public void put(Object obj){
		this.pointer.value = obj;
	}
	
	public Object toValue(){
		return this.pointer.value;
	}
	
	public boolean hasBase(){
		return false;
	}
	
	public Object getBase(){
		return null;
	}
}
