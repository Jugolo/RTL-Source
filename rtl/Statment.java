package rtl;

public class Statment {
	public StatmentType type;
	public Expresion expresion;//for expresions
	public String name;//for names example function names and so on. 
	public CallableArgs arg;
	public Statment[] body;
	public Statment after;
	public String[] context;
	public ForData forData;

	public final int line;
	public final String file;

	public Statment(StatmentType type, String file, int line){
		this.type = type;
		this.line = line;
		this.file = file;
	}
}
