package rtl;

import rtl.exception.RTLRuntimeException;

public class ArrayReference implements IReference{
	private Array base;
	private int index;

	public ArrayReference(Array base, int index){
		this.base = base;
		this.index = index;
	}

	public boolean hasBase(){
		return true;
	}

	public Object toValue() throws RTLRuntimeException{
		if(this.base.size() <= this.index || this.index < 0)
			throw new RTLRuntimeException("Index out of size");
		return this.base.get(this.index);
	}

	public void put(Object obj) throws RTLRuntimeException{
		if(this.index == -1){
			this.base.add(obj);
			return;
		}
		if(this.base.size()+1 < this.index || this.index < 0)
			throw new RTLRuntimeException("Index out of size");
		this.base.put(this.index, obj);
	}
}
