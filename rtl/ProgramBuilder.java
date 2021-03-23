package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ProgramBuilder {
	public static ProgramInstruc build(File file) throws java.io.FileNotFoundException, rtl.exception.RTLInterprenterException{
		Tokenizer token = new Tokenizer(new FileReader(file), file.getAbsolutePath());
		ProgramInstruc instruct = new ProgramInstruc();
		while(!token.current().is(TokenType.EOS)){
			instruct.add(getNextStatment(token));
		}
		return instruct;
	}

	private static Statment getNextStatment(Tokenizer token) throws RTLInterprenterException{
		if(token.current().type() == TokenType.KEYWORD){
			switch(token.current().context()){
				case "println":
					return println(token);
				case "print":
					return print(token);
				case "include":
				    return include(token);
				case "function":
					return function(token);
				case "readln":
					return readln(token);
				case "return":
					return _return(token);
				case "while":
					return _while(token);
				case "if":
					return _if(token);
				case "struct":
					return struct(token);
				case "global":
					return global(token);
				case "for":
					return _for(token);
				case "break":
					return _break(token);
			}
		}
		return getExpresion(token);
	}

	private static Statment _break(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.BREAK, token.current().file(), token.current().line());
		token.next().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static Statment _for(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.FOR, token.current().file(), token.current().line());
		token.next().expect(TokenType.PUNCTOR);
		ForData data = new ForData();
		if(!token.next().is(TokenType.PUNCTOR, ";")){
			data.first = ExpresionBuilder.build(token);
		}

		token.current().expect(TokenType.PUNCTOR, ";");

		if(!token.next().is(TokenType.PUNCTOR, ";")){
			data.second = ExpresionBuilder.build(token);
		}

		token.current().expect(TokenType.PUNCTOR, ";");

		if(!token.next().is(TokenType.PUNCTOR, ")")){
			data.last = ExpresionBuilder.build(token);
		}

		token.current().expect(TokenType.PUNCTOR, ")");
		statment.forData = data;
		token.next();
		statment.body = getBody(token);
		return statment;
	}

	private static Statment global(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.GLOBAL, token.current().file(), token.current().line());
		token.next().expect(TokenType.IDENTIFY);
		statment.name = token.current().context();
		if(token.next().is(TokenType.PUNCTOR, "=")){
			token.next();
			statment.expresion = ExpresionBuilder.build(token);
		}

		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static Statment struct(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.STRUCT, token.current().file(), token.current().line());
		token.next().expect(TokenType.IDENTIFY);
		statment.name = token.current().context();
		token.next().expect(TokenType.PUNCTOR, "{");
		ArrayList<String> buffer = new ArrayList<String>();
		while(token.next().is(TokenType.IDENTIFY)){
			buffer.add(token.current().context());
			if(!token.next().is(TokenType.PUNCTOR, ","))
				break;
		}

		token.current().expect(TokenType.PUNCTOR, "}");
		token.next();
		statment.context = toStringArray(buffer);
		return statment;
	}

	private static Statment _if(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.IF, token.current().file(), token.current().line());
		token.next().expect(TokenType.PUNCTOR, "(");
		token.next();
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ")");
		token.next();
		statment.body = getBody(token);
		if(token.current().is(TokenType.KEYWORD, "elseif")){
			statment.after = _if(token);
		}else if(token.current().is(TokenType.KEYWORD, "else")){
			Statment after = new Statment(StatmentType.ELSE, token.current().file(), token.current().line());
			token.next();
			after.body = getBody(token);
			statment.after = after;
		}
		return statment;
	}

	private static Statment _while(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.WHILE, token.current().file(), token.current().line());
		token.next().expect(TokenType.PUNCTOR, "(");
		token.next();
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ")");
		token.next();
		statment.body = getBody(token);
		return statment;
	}

	private static Statment _return(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.RETURN, token.current().file(), token.current().line());
		token.next();
		if(!token.current().is(TokenType.PUNCTOR, ";")){
			statment.expresion = ExpresionBuilder.build(token);
			token.current().expect(TokenType.PUNCTOR, ";");
		}
		token.next();
		return statment;
	}

	private static Statment readln(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.READLN, token.current().file(), token.current().line());
		token.next().expect(TokenType.IDENTIFY);
		statment.name = token.current().context();
		token.next().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static Statment function(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.FUNCTION, token.current().file(), token.current().line());
		token.next().expect(TokenType.IDENTIFY);
		statment.name = token.current().context();
		token.next().expect(TokenType.PUNCTOR, "(");
		statment.arg = new CallableArgs();
		if(!token.next().is(TokenType.PUNCTOR, ")")){
			addArgs(token, statment.arg);
			while(token.current().is(TokenType.PUNCTOR, ",")){
				token.next();
				addArgs(token, statment.arg);
			}
		}
		token.current().expect(TokenType.PUNCTOR, ")");
		token.next().expect(TokenType.PUNCTOR, "{");
		
		statment.body = getBody(token);
		return statment;
	}

	private static Statment include(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.INCLUDE, token.current().file(), token.current().line());
		token.next();
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static Statment print(Tokenizer token) throws RTLInterprenterException{
		token.next();
		Statment statment = new Statment(StatmentType.PRINT, token.current().file(), token.current().line());
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

    //println <expresion>;
	private static Statment println(Tokenizer token) throws RTLInterprenterException{
		token.next();
		Statment statment = new Statment(StatmentType.PRINTLN, token.current().file(), token.current().line());
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static Statment getExpresion(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.EXPRESION, token.current().file(), token.current().line());
		statment.expresion = ExpresionBuilder.build(token);
		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

	private static void addArgs(Tokenizer token, CallableArgs arg) throws RTLInterprenterException{
		token.current().expect(TokenType.IDENTIFY);
		arg.add(token.current().context());
		token.next();
	}

	private static Statment[] getBody(Tokenizer token) throws RTLInterprenterException{
		if(token.current().is(TokenType.PUNCTOR, "{")){
			token.next();
			ArrayList<Statment> array = new ArrayList<Statment>();
			while(!token.current().is(TokenType.PUNCTOR, "}")){
				array.add(getNextStatment(token));
			}
			token.next();
			return toStatmentArray(array);
		}
		return new Statment[]{
			getNextStatment(token)		
		};
	}

	private static String[] toStringArray(ArrayList<String> array){
		String[] buffer = new String[array.size()];
		for(int i=0;i<array.size();i++){
			buffer[i] = array.get(i);
		}
		return buffer;
	}

	private static Statment[] toStatmentArray(ArrayList<Statment> array){
		Statment[] buffer = new Statment[array.size()];
		for(int i=0;i<array.size();i++){
			buffer[i] = array.get(i);
		}
		return buffer;
	}
}
