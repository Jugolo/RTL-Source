package rtl.array;

import rtl.exception.*;
import rtl.Program;
import rtl.VariabelDatabase;

public interface IArray{
	public Object get(Program program, VariabelDatabase db, Object key) throws RTLException;
	public void add(Program program, VariabelDatabase db, Object obj) throws RTLException;
	public void put(Program program, VariabelDatabase db, Object key, Object context) throws RTLException;
	public int size(Program program, VariabelDatabase db) throws RTLException;
}
