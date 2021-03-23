package rtl;

public class ExcutionPosition {
	public final int line;
	public final String file;

	public ExcutionPosition(String file, int line){
		this.line = line;
		this.file = file;
	}
}
