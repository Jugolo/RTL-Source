package rtl.token;

public class TokenPos {
	public final int line;
	public final String file;

	public TokenPos(String file, int line){
		this.line = line;
		this.file = file;
	}
}
