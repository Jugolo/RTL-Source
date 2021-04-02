package rtl.token;

import rtl.exception.RTLInterprenterException;

public class TokenBuffer {
	private TokenType type;
	private String context;
	private TokenPos pos;

	public TokenBuffer(TokenType type, String context, TokenPos pos){
		this.type = type;
		this.context = context;
		this.pos = pos;
	}

	public TokenType type(){
		return this.type;
	}

	public String context(){
		return this.context;
	}

	public String file(){
		return this.pos.file;
	}

	public int line(){
		return this.pos.line;
	}

	public void expect(TokenType type) throws RTLInterprenterException{
		if(type != this.type)
			throw new RTLInterprenterException("Unexpected type '"+this.type+"' expected '"+type+"'", this.pos.file, this.pos.line);
	}

	public void expect(TokenType type, String context) throws RTLInterprenterException{
		if(this.type != type || !context.equals(this.context))
		  throw new RTLInterprenterException("Unexpected '"+this.context+"'("+this.type+") expected '"+context+"'("+type+")", this.pos.file, this.pos.line);
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
