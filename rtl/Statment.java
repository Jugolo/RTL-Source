package rtl;

import java.util.HashMap;
import java.util.ArrayList;

public class Statment {
	public StatmentType type;
	public Expresion expresion;//for expresions
	public Expresion[] expresions;
	public String name;//for names example function names and so on.
	public String returnType;
	public CallableArgs arg;
	public Statment[] body;
	public Statment after;
	public String[] context;
	public ForData forData;
	public boolean isGlobal = false;
	public boolean isConst = false;
	public StructField[] struct;
	public HashMap<String, ScriptObjectPointer> pointer;
	public Statment constructor;
	public String access;
	public ArrayList<Statment> methods = new ArrayList<Statment>();

	public final int line;
	public final String file;

	public Statment(StatmentType type, String file, int line){
		this.type = type;
		this.line = line;
		this.file = file;
	}
}
