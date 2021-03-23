package rtl;

import rtl.exception.RTLRuntimeException;

public interface IReference {
	public void put(Object obj) throws RTLRuntimeException;
	public Object toValue() throws RTLRuntimeException;
	public boolean hasBase();
}
