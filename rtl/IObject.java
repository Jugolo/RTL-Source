package rtl;

import rtl.exception.*;

public interface IObject{
	public IClass getOwner();
	public Object getPointer(IObject caller, String name) throws RTLRuntimeException;
	public Object callMethod(IObject caller, String name, Program program, Object[] args) throws RTLException;
}
