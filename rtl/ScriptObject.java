package rtl;

class ScriptObject implements IObject{
	private ScriptClass owner;
	
	public ScriptObject(ScriptClass owner){
		this.owner = owner;
	}
	
	public IClass getOwner(){
		return this.owner;
	}
}
