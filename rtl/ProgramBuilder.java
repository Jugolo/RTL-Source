package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;

public class ProgramBuilder {
	public static ProgramInstruc build(String code) throws RTLInterprenterException{
		return eval(new Tokenizer(new StringReader(code), "<inline>"));
	}
	
	public static ProgramInstruc build(File file) throws java.io.FileNotFoundException, RTLInterprenterException{
		return eval(new Tokenizer(new FileReader(file), file.getAbsolutePath()));
	}
	
	private static ProgramInstruc eval(Tokenizer token) throws RTLInterprenterException{ 
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
				case "const":
					return _const(token);
				case "for":
					return _for(token);
				case "break":
					return _break(token);
				case "continue":
					return _continue(token);
				case "class":
					return _class(token);
			}
		}
		return getExpresion(token);
	}
	
	private static Statment _class(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.CLASS, token.current().file(), token.current().line());
		statment.name = token.next().expect(TokenType.IDENTIFY);
		token.next().expect(TokenType.PUNCTOR, "{");
		token.next().expect(TokenType.PUNCTOR, "}");
		token.next();
		return statment;
	}

	private static Statment _continue(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.CONTINUE, token.current().file(), token.current().line());
		token.next().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
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
		}else{
			data.first = new Expresion(ExpresionType.NULL);
		}

		token.current().expect(TokenType.PUNCTOR, ";");

		if(!token.next().is(TokenType.PUNCTOR, ";")){
			data.second = ExpresionBuilder.build(token);
		}else{
			data.second = new Expresion(ExpresionType.BOOL);
			data.second.str = "true";
		}

		token.current().expect(TokenType.PUNCTOR, ";");

		if(!token.next().is(TokenType.PUNCTOR, ")")){
			data.last = ExpresionBuilder.build(token);
		}else{
			data.last = new Expresion(ExpresionType.NULL);
		}

		token.current().expect(TokenType.PUNCTOR, ")");
		statment.forData = data;
		token.next();
		statment.body = getBody(token);
		return statment;
	}

	private static Statment global(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.GLOBAL, token.current().file(), token.current().line());
		if(token.next().is(TokenType.KEYWORD, "const")){
			statment.isConst = true;
			token.next();
		}
		token.current().expect(TokenType.IDENTIFY);
		statment.name = token.current().context();
		if(token.next().is(TokenType.PUNCTOR, "=")){
			token.next();
			statment.expresion = ExpresionBuilder.build(token);
		}

		token.current().expect(TokenType.PUNCTOR, ";");
		token.next();
		return statment;
	}

    private static Statment _const(Tokenizer token) throws RTLInterprenterException{
    	Statment statment = new Statment(StatmentType.CONST, token.current().file(), token.current().line());
    	if(token.next().is(TokenType.KEYWORD, "global")){
    		statment.isGlobal = true;
    		token.next();
    	}
        token.current().expect(TokenType.IDENTIFY);
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
		ArrayList<StructField> buffer = new ArrayList<StructField>();
		while(!token.next().is(TokenType.PUNCTOR, "}")){
			StructField field = new StructField();
			if(token.current().is(TokenType.KEYWORD, "const")){
				field.isConst = true;
				token.next();
			}
			
			field.name = token.current().expect(TokenType.IDENTIFY);
			
			if(token.next().is(TokenType.PUNCTOR, "=")){
				token.next();
				field.context = ExpresionBuilder.build(token);
			}
			
			buffer.add(field);
			if(!token.current().is(TokenType.PUNCTOR, ","))
				break;
		}

		token.current().expect(TokenType.PUNCTOR, "}");
		token.next();
		StructField[] buf = new StructField[buffer.size()];
		for(int i=0;i<buf.length;i++)
			buf[i] = buffer.get(i);
		statment.struct = buf;
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
		String name = token.next().expect(TokenType.IDENTIFY);
		if(token.next().is(TokenType.IDENTIFY)){
			statment.name = token.current().context();
			statment.returnType = name;
			token.next();
		}else
			statment.name = name;
		statment.arg = FunctionUntil.getArg(token);
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
		Statment statment = new Statment(StatmentType.PRINT, token.current().file(), token.current().line());
		token.next();
		ArrayList<Expresion> buffer = new ArrayList<Expresion>();
		buffer.add(ExpresionBuilder.build(token));
		while(token.current().is(TokenType.PUNCTOR, ",")){
			token.next();
			buffer.add(ExpresionBuilder.build(token));
		}
		token.current().expect(TokenType.PUNCTOR, ";");
		statment.expresions = toExpresionArray(buffer);
		token.next();
		return statment;
	}

    //println <expresion>;
	private static Statment println(Tokenizer token) throws RTLInterprenterException{
		Statment statment = new Statment(StatmentType.PRINTLN, token.current().file(), token.current().line());
		token.next();
		ArrayList<Expresion> buffer = new ArrayList<Expresion>();
		buffer.add(ExpresionBuilder.build(token));
		while(token.current().is(TokenType.PUNCTOR, ",")){
			token.next();
			buffer.add(ExpresionBuilder.build(token));
		}
		token.current().expect(TokenType.PUNCTOR, ";");
		statment.expresions = toExpresionArray(buffer);
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

	public static Statment[] getBody(Tokenizer token) throws RTLInterprenterException{
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
	
	private static Expresion[] toExpresionArray(ArrayList<Expresion> array){
		Expresion[] buffer = new Expresion[array.size()];
		for(int i=0;i<array.size();i++)
			buffer[i] = array.get(i);
		return buffer;
	}
}
