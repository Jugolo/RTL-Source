package rtl.token;

import rtl.exception.RTLInterprenterException;

import java.io.FileReader;

public class Tokenizer {
	private TokenReader reader;
	private TokenBuffer buffer;

	private String[] keywords = {
		"println",
		"print",
		"readln",
		"include",
		"function",
		"for",
		"return",
		"break",
		"while",
		"if",
		"else",
		"elseif",
		"typeof",
		"struct",
		"true",
		"false",
		"null",
		"global"
		};
	
	public Tokenizer(FileReader reader, String path) throws RTLInterprenterException{
		this.reader = new TokenReader(reader, path);
		this.next();	
	}

	public TokenBuffer next() throws RTLInterprenterException{
		return this.buffer = this.generateNext();
	}

	public TokenBuffer current(){
		return this.buffer;
	}

	private TokenBuffer generateNext() throws RTLInterprenterException{
		int c = this.gc();

		if(c == -1){
			return new TokenBuffer(TokenType.EOS, "End of script", this.reader.getPath(), this.reader.getLine());
		}

		if(this.isIdentifyStart(c)){
			return this.getIdentify(c);
		}

		if(this.isNumber(c))
			return this.getNumber(c);

		if(c == '"' || c == '\'')
		  return this.getString(c);

		return this.getPunctor(c);
	}

	private TokenBuffer getNumber(int first) throws RTLInterprenterException{
		StringBuilder builder = new StringBuilder();
		builder.append((char)first);
		getCleanNumber(builder);
		if(this.reader.peek() == '.'){
			builder.append(".");
			getCleanNumber(builder);
		}

		return new TokenBuffer(TokenType.NUMBER, builder.toString(), this.reader.getPath(), this.reader.getLine());
	}

	private void getCleanNumber(StringBuilder buffer) throws RTLInterprenterException{
		while(this.isNumber(this.reader.peek())){
			buffer.append((char)this.reader.read());
		}
	}

	private TokenBuffer getString(int stop) throws RTLInterprenterException{
		StringBuilder str = new StringBuilder();
		int line = this.reader.getLine();

        while(true){
        	int buffer = this.reader.read();
        	if(buffer == -1)
        		throw new RTLInterprenterException("Missing "+((char)stop)+" to end string got end of string",
        		this.reader.getPath(), line);
        	if(buffer == stop)
        		break;
        	//if this is \ wee need to handle this on the correct way....
        	if(buffer == '\\'){
        		int c = this.reader.read();
        		switch(c){
        			case 'n':
        				str.append("\n");
        			break;
        		    case 'r':
        		    	str.append("\r");
        		    break;
        		    case 't':
        		    	str.append("\t");
        		    break;
        			default:
        				str.append((char)c);
        		}
        	}else
        		str.append((char)buffer);
        }

		return new TokenBuffer(TokenType.STRING, str.toString(), this.reader.getPath(), line);
	}

	private TokenBuffer getIdentify(int c) throws RTLInterprenterException{
		StringBuilder buffer = new StringBuilder(c);
		buffer.append((char)c);
		while(this.isIdentifyPart(this.reader.peek())){
			buffer.append((char)this.reader.read());
		}

        if(exists(buffer.toString(), keywords))
        	return new TokenBuffer(TokenType.KEYWORD, buffer.toString(), this.reader.getPath(), this.reader.getLine());
		
		//System.out.println(buffer.toString());
		return new TokenBuffer(TokenType.IDENTIFY, buffer.toString(), this.reader.getPath(), this.reader.getLine());
	}

	private TokenBuffer getPunctor(int c) throws RTLInterprenterException{
		if(c == ';')
			return new TokenBuffer(TokenType.PUNCTOR, ";", this.reader.getPath(), this.reader.getLine());
		if(c == ':')
			return new TokenBuffer(TokenType.PUNCTOR, ":", this.reader.getPath(), this.reader.getLine());
		if(c == '(')
			return new TokenBuffer(TokenType.PUNCTOR, "(", this.reader.getPath(), this.reader.getLine());
		if(c == ')')
			return new TokenBuffer(TokenType.PUNCTOR, ")", this.reader.getPath(), this.reader.getLine());
		if(c == '{')
			return new TokenBuffer(TokenType.PUNCTOR, "{", this.reader.getPath(), this.reader.getLine());
		if(c == '}')
			return new TokenBuffer(TokenType.PUNCTOR, "}", this.reader.getPath(), this.reader.getLine());
		if(c == '[')
			return new TokenBuffer(TokenType.PUNCTOR, "[", this.reader.getPath(), this.reader.getLine());
		if(c == ']')
			return new TokenBuffer(TokenType.PUNCTOR, "]", this.reader.getPath(), this.reader.getLine());
		if(c == ',')
			return new TokenBuffer(TokenType.PUNCTOR, ",", this.reader.getPath(), this.reader.getLine());
		if(c == '.')
			return new TokenBuffer(TokenType.PUNCTOR, ".", this.reader.getPath(), this.reader.getLine());
		if(c == '^')
			return new TokenBuffer(TokenType.PUNCTOR, "^", this.reader.getPath(), this.reader.getLine());
		if(c == '*')
			return new TokenBuffer(TokenType.PUNCTOR, "*", this.reader.getPath(), this.reader.getLine());
		if(c == '/')
			return new TokenBuffer(TokenType.PUNCTOR, "/", this.reader.getPath(), this.reader.getLine());
		if(c == '?')
			return new TokenBuffer(TokenType.PUNCTOR, "?", this.reader.getPath(), this.reader.getLine());
		if(c == '&'){
			if(this.reader.peek() == '&'){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "&&", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "&", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '<'){
			if(this.reader.peek() == '='){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "<=", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "<", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '>'){
			if(this.reader.peek() == '='){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, ">=", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, ">", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '|'){
			if(this.reader.peek() == '|'){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "||", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "|", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '='){
			if(this.reader.peek() == '='){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "==", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "=", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '+'){
			if(this.reader.peek() == '+'){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "++", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "+", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '!'){
			if(this.reader.peek() == '='){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "!=", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "!", this.reader.getPath(), this.reader.getLine());
		}
		if(c == '-'){
			if(this.reader.peek() == '-'){
				this.reader.read();
				return new TokenBuffer(TokenType.PUNCTOR, "--", this.reader.getPath(), this.reader.getLine());
			}
			return new TokenBuffer(TokenType.PUNCTOR, "-", this.reader.getPath(), this.reader.getLine());
		}
		throw new RTLInterprenterException("Unknown char detected in the source: "+((char)c)+"("+c+")",
		this.reader.getPath(), this.reader.getLine());
	}

	private int gc() throws RTLInterprenterException{
		int c = this.reader.read();

        if(c == ' ' || c == '\n')
        	return this.gc();

        if(c == '/' && this.reader.peek() == '/'){
        	while(this.reader.read() != '\n');
        	return this.gc();
        }
		
		return c;
	}

	private boolean isNumber(int c){
		return c >= '0' && c <= '9';
	}

	private boolean isIdentifyPart(int c){
		return this.isIdentifyStart(c) || this.isNumber(c);
	}

	private boolean isIdentifyStart(int c){
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
	}

	private boolean exists(Object o, Object[] array){
		for(int i=0;i<array.length;i++){
			if(o.equals(array[i]))
				return true;
		}

		return false;
	}
}