package rtl.array;

import rtl.exception.*;
import rtl.*;

public class ArrayReference implements IReference{
	private IArray base;
	private Object index;
	private Program program;
	private VariabelDatabase db;
	private boolean hasKey = false;

	public ArrayReference(Program program, VariabelDatabase db, IArray base, Object index){
		this.base = base;
		this.index = index;
		this.program = program;
		this.db = db;
		this.hasKey = true;
	}
	
	public ArrayReference(Program program, VariabelDatabase db, IArray base){
		this.base = base;
		this.program = program;
		this.db = db;
	}

	public boolean hasBase(){
		return true;
	}

	public Object toValue() throws RTLException{
		return this.base.get(this.program, this.db, this.index);
	}

	public void put(Object obj) throws RTLException{
		if(!this.hasKey){
			this.base.add(this.program, this.db, obj);
			return;
		}
		this.base.put(this.program, this.db, this.index, obj);
	}
	
	public Object getBase(){
		return this.base;
	}
}
