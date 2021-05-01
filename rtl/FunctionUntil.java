package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;
import rtl.exception.RTLRuntimeException;

public class FunctionUntil {
	public static Function getCallable(String name, VariabelDatabase db, CallableArgs arg, Statment[] body){
		return new Function(name, db, arg, new ICallable(){
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
		if(token.current().is(TokenType.KEYWORD, "function")){
			arg.add("function", token.next().expect(TokenType.IDENTIFY));
			token.next();
			return;
		}
		
		String buffer = token.current().expect(TokenType.IDENTIFY);
		if(token.next().is(TokenType.IDENTIFY)){
			arg.add(buffer, token.current().context());
			token.next();
		}else
			arg.add(buffer);
	}
}
