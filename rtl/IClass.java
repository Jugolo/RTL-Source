package rtl;

import rtl.exception.*;

public interface IClass{
	public String getName();
	public IObject newInstance(Object[] args, Program program) throws RTLException;
	public Method getMethod(IObject caller, String name) throws RTLRuntimeException;
}
