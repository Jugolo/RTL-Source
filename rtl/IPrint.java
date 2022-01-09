package rtl;

import rtl.exception.RTLRuntimeException;

public interface IPrint{
	public void println(Program program, Object[] data) throws RTLRuntimeException;
	public void print(Program program, Object[] data) throws RTLRuntimeException;
	public void readln(Program program, String name);
}
