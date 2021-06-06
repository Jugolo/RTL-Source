package rtl;

import rtl.exception.RTLRuntimeException;

public class StructReference implements IReference{
	private StructItem item;

	public StructReference(StructItem item){
		this.item = item;
	}

	public boolean hasBase(){
		return true;
	}

	public Object toValue(){
		return this.item.value;
	}

	public void put(Object context) throws RTLRuntimeException{
		if(this.item.field.isConst && this.item.hasSet)
			throw new RTLRuntimeException("Cant append on a const field in a struct");
		this.item.value = context;
		this.item.hasSet = true;
	}
}
