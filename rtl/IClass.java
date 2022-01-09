package rtl;

import rtl.exception.RTLRuntimeException;

public interface IClass{
	public String getName();
	public IObject newInstance(Object[] args) throws RTLRuntimeException;
}
