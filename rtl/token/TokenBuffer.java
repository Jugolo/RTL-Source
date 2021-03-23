package rtl.token;

import rtl.exception.RTLInterprenterException;

public class TokenBuffer {
	private TokenType type;
	private String context;
	private String file;
	private int line;

	public TokenBuffer(TokenType type, String context, String file, int line){
		this.type = type;
		this.context = context;
		this.file = file;
		this.line = line;
	}

	public TokenType type(){
		return this.type;
	}

	public String context(){
		return this.context;
	}

	public String file(){
		return this.file;
	}

	public int line(){
		return this.line;
	}

	public void expect(TokenType type) throws RTLInterprenterException{
		if(type != this.type)
			throw new RTLInterprenterException("Unexpected type '"+this.type+"' expected '"+type+"'", this.file, this.line);
	}

	public void expect(TokenType type, String context) throws RTLInterprenterException{
		if(this.type != type || !context.equals(this.context))
		  throw new RTLInterprenterException("Unexpected '"+this.context+"'("+this.type+") expected '"+context+"'("+type+")", this.file, this.line);
	}

	public boolean is(TokenType type){
		return this.type == type;
	}

	public boolean is(TokenType type, String context){
		return this.type == type && this.context.equals(context);
	}

	public boolean is(TokenType type, String[] context){
		if(this.type != type)
			return false;
		for(int i=0;i<context.length;i++){
			if(context[i].equals(this.context))
				return true;
		}
		return false;
	}
}
