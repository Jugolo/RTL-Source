package rtl;

import rtl.exception.RTLRuntimeException;

public interface ICallable {
	public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException;
}
