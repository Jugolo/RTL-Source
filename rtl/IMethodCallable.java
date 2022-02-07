package rtl;

import rtl.exception.RTLException;

public interface IMethodCallable{
	public Object call(Program program, VariabelDatabase db, Object[] arg, IObject caller) throws RTLException;
}
