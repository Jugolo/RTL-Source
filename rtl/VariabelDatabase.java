package rtl;

import java.util.HashMap;

public class VariabelDatabase {
	private VariabelDatabase last = null;
	private HashMap<String, VariabelContainer> db = new HashMap<String, VariabelContainer>();
	private Object _this;
	
	public VariabelDatabase(){}
	
	public VariabelDatabase(Object _this){
		this._this = _this;
	}
	
	public Object getThis(){
		return this._this;
	}

	public boolean containes(String name){
		return this.db.containsKey(name);
	}

	public void setLast(VariabelDatabase db){
		this.last = db;
	}

	public VariableReference get(String name){
		VariabelDatabase db = this;
		while(db != null){
			if(db.containes(name)){
				if(db == this){
					return new VariableReference(name, this);
				}

				if(db.getContainer(name).attribute(VariabelAttribute.GLOBAL))
					return new VariableReference(name, db);
			}

			db = db.last();
		}

		return new VariableReference(name, this);
	}

	public VariabelDatabase last(){
		return this.last;
	}

	public VariabelContainer getContainer(String name){
		if(this.containes(name))
			return this.db.get(name);
		return null;
	}

	public void put(String name, VariabelContainer container){
		this.db.put(name, container);
	}
}
