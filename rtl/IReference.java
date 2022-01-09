package rtl;

import rtl.exception.RTLException;

public interface IReference {
	public void put(Object obj) throws RTLException;
	public Object toValue() throws RTLException;
	public boolean hasBase();
	public Object getBase();
}
