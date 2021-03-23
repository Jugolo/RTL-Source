package rtl.token;

import rtl.exception.RTLInterprenterException;

import java.io.FileReader;

public class TokenReader {
	private FileReader reader;
	private String path;
	private int buffer = -1;
	private int line = 1;

	public TokenReader(FileReader reader, String path){
		this.path = path;
		this.reader = reader;
	}

	public String getPath(){
		return this.path;
	}

	public int getLine(){
		return this.line;
	}

	public int peek() throws RTLInterprenterException{
		if(this.buffer != -1){
			return this.buffer;
		}

		return this.buffer = this.read();
	}

	public int read() throws RTLInterprenterException{
		if(this.buffer != -1){
			int buf = this.buffer;
			this.buffer = -1;
			if(buf == '\n')
				this.line++;
			return buf;
		}
		try{
		  int c = this.reader.read();
		  if(c == '\n')
		  	this.line++;
		  return c;
		}catch(java.io.IOException e){
			throw new RTLInterprenterException(e.getMessage());
		}
	}
}
