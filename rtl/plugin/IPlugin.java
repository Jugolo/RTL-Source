package rtl.plugin;

import rtl.VariabelDatabase;
import rtl.Program;
import rtl.exception.RTLException;

public interface IPlugin{
	public String getIncludePath();
	public void init(VariabelDatabase db, Program program) throws RTLException;
	public boolean isFinish();
	public void close();
}
