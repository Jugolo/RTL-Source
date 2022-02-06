package rtl;

class ScriptObjectPointer{
	public Object value;
	public ObjectAccess access;
	
	public ScriptObjectPointer clone(){
		ScriptObjectPointer pointer = new ScriptObjectPointer();
		pointer.value  = value;
		pointer.access = access;
		return pointer;
	}
}
