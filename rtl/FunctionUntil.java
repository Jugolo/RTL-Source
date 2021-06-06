package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;
import rtl.exception.RTLRuntimeException;

public class FunctionUntil {
	public static Function getCallable(String name, String returnType, VariabelDatabase db, CallableArgs arg, Statment[] body){
		return new Function(name, returnType, db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				Complication c = program.run(new ProgramInstrucList(body), db);
				if(c.type() == ComplicationType.RETURN)
					return c.value();
				return null;
			}
		});
	}
	
	public static CallableArgs getArg(Tokenizer token) throws RTLInterprenterException{
		token.current().expect(TokenType.PUNCTOR, "(");
		CallableArgs arg = new CallableArgs();
		if(!token.next().is(TokenType.PUNCTOR, ")")){
			addArgs(token, arg);
			while(token.current().is(TokenType.PUNCTOR, ",")){
				token.next();
				addArgs(token, arg);
			}
		}
		token.current().expect(TokenType.PUNCTOR, ")");
		return arg;
	}

	private static void addArgs(Tokenizer token, CallableArgs arg) throws RTLInterprenterException{
		String name;
		if(token.current().is(TokenType.KEYWORD, "function")){
			name = token.next().expect(TokenType.IDENTIFY);
			if(token.next().is(TokenType.PUNCTOR, "=")){
				token.next();
				arg.add("function", name, ExpresionBuilder.build(token));
			}else
				arg.add("function", name);
			return;
		}
		
		String buffer = token.current().expect(TokenType.IDENTIFY);
		if(token.next().is(TokenType.IDENTIFY)){
			name = token.current().context();
			if(token.next().is(TokenType.PUNCTOR, "=")){
				token.next();
				arg.add(buffer, name, ExpresionBuilder.build(token));
			}else
			  arg.add(buffer, name);
		}else{
			if(token.current().is(TokenType.PUNCTOR, "=")){
				token.next();
				arg.add(buffer, ExpresionBuilder.build(token));
			}else
				arg.add(buffer);
		}
	}
}
