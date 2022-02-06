package rtl;

import rtl.exception.RTLRuntimeException;

public interface IObject{
	public IClass getOwner();
	public Object getPointer(IObject caller, String name) throws RTLRuntimeException;
}
