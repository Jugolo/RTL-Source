package rtl;

import rtl.exception.RTLException;

public interface ICallable {
	public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLException;
}
