package rtl;

import rtl.exception.RTLRuntimeException;

public class ArrayReference implements IReference{
	private Array base;
	private int index;
	private boolean hasKey = false;

	public ArrayReference(Array base, int index){
		this.base = base;
		this.index = index;
		this.hasKey = true;
	}
	
	public ArrayReference(Array base){
		this.base = base;
	}

	public boolean hasBase(){
		return true;
	}

	public Object toValue() throws RTLRuntimeException{
		if(this.base.size() <= this.index || this.index < 0)
			throw new RTLRuntimeException("Index out of size: "+this.index);
		return this.base.get(this.index);
	}

	public void put(Object obj) throws RTLRuntimeException{
		if(!this.hasKey){
			this.base.add(obj);
			return;
		}
		if(this.base.size()+1 < this.index || this.index < 0)
			throw new RTLRuntimeException("Index out of size: "+this.index);
		this.base.put(this.index, obj);
	}
}
