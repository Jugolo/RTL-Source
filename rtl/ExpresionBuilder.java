package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenBuffer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;

import java.util.ArrayList;

public class ExpresionBuilder {
	public static Expresion build(Tokenizer token) throws RTLInterprenterException{
		return assign(token);
	}

    private static EXPSign getAssingSign(String sign) throws RTLInterprenterException{
		switch(sign){
			case "=":
				return EXPSign.ASSIGN;
			case "|=":
				return EXPSign.BITWISE_ASSIGN;
			case "+=":
				return EXPSign.PLUS_ASSIGN;
			case "-=":
				return EXPSign.MINUS_ASSIGN;
		}
		
		throw new RTLInterprenterException("Unknown assign sign: "+sign);
	}

	private static Expresion assign(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = ask(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"=", "|=", "+=", "-="})){
			Expresion buffer = new Expresion(ExpresionType.ASSIGN);
			buffer.left = exp;
			buffer.sign = getAssingSign(token.current().context());
			token.next();
			buffer.right = assign(token);
			return buffer;
		}
		return exp;
	}
	
	private static Expresion ask(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = AO(token);
		if(token.current().is(TokenType.PUNCTOR, "?")){
			Expresion buffer = new Expresion(ExpresionType.ASK);
			buffer.test = exp;
			token.next();
			buffer.left = ask(token);
			token.current().expect(TokenType.PUNCTOR, ":");
			token.next();
			buffer.right = ask(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion AO(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = compare(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"&&", "||"})){
			Expresion buffer = new Expresion(ExpresionType.AO);
			buffer.left = exp;
			buffer.sign = token.current().context().equals("&&") ? EXPSign.AND : EXPSign.OR;
			token.next();
			buffer.right = AO(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion compare(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = bitwiseor(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"==", "!="})){
			Expresion buffer = new Expresion(ExpresionType.COMPARE);
			buffer.left = exp;
			buffer.sign = token.current().context().equals("==") ? EXPSign.EQUAL : EXPSign.NOT_EQUAL;
			token.next();
			buffer.right = compare(token);
			return buffer;
		}
		return exp;
	}
	
	private static Expresion bitwiseor(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = bitwisexor(token);
		if(token.current().is(TokenType.PUNCTOR, "|")){
			token.next();
			Expresion buffer = new Expresion(ExpresionType.BITWISEOR);
			buffer.left = exp;
			buffer.right = bitwiseor(token);
			return buffer;
		}
		return exp;
	}
	
	private static Expresion bitwisexor(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = bitwiseand(token);
		if(token.current().is(TokenType.PUNCTOR, "^")){
			token.next();
			Expresion buffer = new Expresion(ExpresionType.BITWISEXOR);
			buffer.left = exp;
			buffer.right = bitwisexor(token);
			return buffer;
		}
		return exp;
	}
	
	private static Expresion bitwiseand(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = bitwise(token);
		if(token.current().is(TokenType.PUNCTOR, "&")){
			token.next();
			Expresion buffer = new Expresion(ExpresionType.BITWISEAND);
			buffer.left = exp;
			buffer.right = bitwiseand(token);
			return buffer;
		}
		return exp;
	}
	
	private static EXPSign getBitwiseSign(String sign) throws RTLInterprenterException{
		switch(sign){
			case "<<":
				return EXPSign.SIGN_L;
			case ">>":
				return EXPSign.SIGN_R;
			case ">>>":
				return EXPSign.UNSIGN_R;
		}
		throw new RTLInterprenterException("Unknown bitwise sign: "+sign);
	}
	
	private static Expresion bitwise(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getSizeCompare(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"<<", ">>", ">>>"})){
			Expresion buffer = new Expresion(ExpresionType.BITWISE);
			buffer.left = exp;
			buffer.sign = getBitwiseSign(token.current().context());
			token.next();
			buffer.right = bitwise(token);
			return buffer;
		}
		return exp;
	}
	
	private static EXPSign getSizeSign(String sign) throws RTLInterprenterException{
		switch(sign){
			case "<":
				return EXPSign.CREATER;
			case ">":
				return EXPSign.LESS;
			case "<=":
				return EXPSign.CREATER_EQUAL;
			case ">=":
				return EXPSign.LESS_EQUAL;
		}
		throw new RTLInterprenterException("Unknown size sign: "+sign);
	}

	private static Expresion getSizeCompare(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getMath(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"<", ">", "<=", ">="})){
			Expresion buffer = new Expresion(ExpresionType.SIZE);
			buffer.left = exp;
			buffer.sign = getSizeSign(token.current().context());
			token.next();
			buffer.right = getSizeCompare(token);
			return buffer;
		}
		return exp;
	}
	
	private static EXPSign getMathSize(String sign) throws RTLInterprenterException{
		switch(sign){
			case "+":
				return EXPSign.PLUS;
			case "-":
				return EXPSign.MINUS;
		}
		throw new RTLInterprenterException("Unknown math sign: "+sign);
	}

	private static Expresion getMath(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getPow(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"+", "-"})){
			Expresion buffer = new Expresion(ExpresionType.MATH);
			buffer.left = exp;
			buffer.sign = getMathSize(token.current().context());
			token.next();
			buffer.right = getMath(token);
			return buffer;
		}
		return exp;
	}
	
	private static EXPSign getPowSign(String sign) throws RTLInterprenterException{
		switch(sign){
			case "*":
				return EXPSign.GANGE;
			case "/":
				return EXPSign.DIV;
			case "%":
				return EXPSign.MOD;
		}
		throw new RTLInterprenterException("Unknown pow sign: "+sign);
	}

	private static Expresion getPow(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getPrefix(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"*", "/", "%"})){
			Expresion buffer = new Expresion(ExpresionType.POW);
			buffer.left = exp;
			buffer.sign = getPowSign(token.current().context());
			token.next();
			buffer.right = getPow(token);
			return buffer;
		}
		return exp;
	}
	
	private static EXPSign getPrefixSign(String sign) throws RTLInterprenterException{
		switch(sign){
			case "+":
				return EXPSign.PLUS;
			case "-":
				return EXPSign.MINUS;
		}
		throw new RTLInterprenterException("Unknown prefix sign: "+sign);
	}

	private static Expresion getPrefix(Tokenizer token) throws RTLInterprenterException{
		if(token.current().is(TokenType.KEYWORD, "typeof")){
			token.next();
			Expresion exp = new Expresion(ExpresionType.TYPEOF);
			exp.left = getPrimary(token);
			return exp;
		}

		if(token.current().is(TokenType.PUNCTOR, new String[]{"-", "+"})){
			Expresion exp = new Expresion(ExpresionType.NPC);
			exp.sign = getPrefixSign(token.current().context());
			token.next();
			exp.left = getPrimary(token);
			return exp;
		}

		if(token.current().is(TokenType.PUNCTOR, "!")){
			Expresion exp = new Expresion(ExpresionType.NOT);
			token.next();
			exp.left = getPrimary(token);
			return exp;
		}
		
		if(token.current().is(TokenType.PUNCTOR, "~")){
			Expresion exp = new Expresion(ExpresionType.BITWISENOT);
			token.next();
			exp.left = getPrimary(token);
			return exp;
		}

		return getPrimary(token);
	}

	private static Expresion getPrimary(Tokenizer token) throws RTLInterprenterException{
		if(token.current().is(TokenType.PUNCTOR, "{")){
			Expresion exp = new Expresion(ExpresionType.BLOCK);
			exp.block = ProgramBuilder.getBody(token);
			return handleAfterIdentify(exp, token);
		}
		
		TokenBuffer buffer = token.current();
		token.next();

		if(buffer.is(TokenType.STRING)){
			Expresion exp = new Expresion(ExpresionType.STRING);
			exp.str = buffer.context();
			return exp;
		}

		if(buffer.is(TokenType.IDENTIFY)){
			Expresion exp = new Expresion(ExpresionType.IDENTIFY);
			exp.str = buffer.context();
			return handleAfterIdentify(exp, token);
		}

		if(buffer.is(TokenType.KEYWORD, new String[]{"true", "false"})){
			Expresion exp = new Expresion(ExpresionType.BOOL);
			exp.sign = buffer.context().equals("true") ? EXPSign.TRUE : EXPSign.FALSE;
			return exp;
		}

		if(buffer.is(TokenType.NUMBER)){
			Expresion exp = new Expresion(ExpresionType.NUMBER);
			exp.str = buffer.context();
			return exp;
		}
		
		if(buffer.is(TokenType.HEXNUMBER)){
			Expresion exp = new Expresion(ExpresionType.NUMBER);
			exp.str = ""+Integer.parseInt(buffer.context(), 16);
			return exp;
		}

		if(buffer.is(TokenType.KEYWORD, "struct")){
			Expresion exp = new Expresion(ExpresionType.STRUCT);
			token.current().expect(TokenType.IDENTIFY);
			exp.str = token.current().context();
			if(token.next().is(TokenType.PUNCTOR, "(")){
				token.next();
				exp.list = getArgsCall(token);
			}
			
			if(token.current().is(TokenType.PUNCTOR, "{"))
				getStructItem(exp, token);
			return exp;
		}

		if(buffer.is(TokenType.PUNCTOR, "["))
			return array(token);

		if(buffer.is(TokenType.PUNCTOR, "(")){
			Expresion exp = build(token);
			token.current().expect(TokenType.PUNCTOR, ")");
			token.next();
			return handleAfterIdentify(exp, token);
		}

		if(buffer.is(TokenType.KEYWORD, "null")){
			return new Expresion(ExpresionType.NULL);
		}

		if(buffer.is(TokenType.KEYWORD, "function"))
			return handleAfterIdentify(func(token), token);
		
		throw new RTLInterprenterException("Unknown token detected "+buffer.type()+"("+buffer.context()+")");
	}
	
	private static void getStructItem(Expresion exp, Tokenizer token) throws RTLInterprenterException{
		if(!token.next().is(TokenType.PUNCTOR, "}")){
			getStructSingleItem(exp, token);
			while(token.current().is(TokenType.PUNCTOR, ",")){
				token.next();
				getStructSingleItem(exp, token);
			}
			token.current().expect(TokenType.PUNCTOR, "}");
		}
		token.next();
	}
	
	private static void getStructSingleItem(Expresion exp, Tokenizer token) throws RTLInterprenterException{
		String name = token.current().expect(TokenType.IDENTIFY);
		token.next().expect(TokenType.PUNCTOR, "=");
		token.next();
		exp.structArgs.put(name, build(token));
	}

	private static Expresion handleAfterIdentify(Expresion before, Tokenizer token) throws RTLInterprenterException{
		if(token.current().is(TokenType.PUNCTOR, "(")){
			Expresion exp = new Expresion(ExpresionType.CALL);
			token.next();
			exp.left = before;
			exp.list = getArgsCall(token);
			return handleAfterIdentify(exp, token);
		}

		if(token.current().is(TokenType.PUNCTOR, ".")){
			Expresion exp = new Expresion(ExpresionType.STRUCT_GET);
			token.next().expect(TokenType.IDENTIFY);
			exp.left = before;
			exp.str = token.current().context();
			token.next();
			return handleAfterIdentify(exp, token);
		}

		if(token.current().is(TokenType.PUNCTOR, "[")){
			Expresion exp = new Expresion(ExpresionType.ARRAY_GET);
			exp.left = before;
			if(!token.next().is(TokenType.PUNCTOR, "]")){
				exp.right = build(token);
			}

			token.current().expect(TokenType.PUNCTOR, "]");
			token.next();
			return handleAfterIdentify(exp, token);
		}

		if(token.current().is(TokenType.PUNCTOR, new String[]{"++", "--"})){
			Expresion exp = new Expresion(ExpresionType.SELF_INC);
			exp.left = before;
			exp.sign = token.current().context().equals("++") ? EXPSign.SELF_INC : EXPSign.SELF_DEC;
			token.next();
			return exp;
		}
		
		return before;
	}

	private static Expresion func(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = new Expresion(ExpresionType.FUNCTION);
		if(token.current().is(TokenType.IDENTIFY)){
			exp.returnType = token.current().context();
			token.next();
		}
		exp.arg = FunctionUntil.getArg(token);
		token.next().expect(TokenType.PUNCTOR, "{");
		exp.block = ProgramBuilder.getBody(token);
		return exp;
	}

	private static Expresion[] getArgsCall(Tokenizer token) throws RTLInterprenterException{
		ArrayList<Expresion> array = new ArrayList<Expresion>();
		if(!token.current().is(TokenType.PUNCTOR, ")")){
			array.add(build(token));
			while(token.current().is(TokenType.PUNCTOR, ",")){
				token.next();
				array.add(build(token));
			}
		}
		token.current().expect(TokenType.PUNCTOR, ")");
		token.next();
		return toArray(array);
	}

	private static Expresion[] toArray(ArrayList<Expresion> array){
		Expresion[] buffer = new Expresion[array.size()];
		for(int i=0;i<array.size();i++){
			buffer[i] = array.get(i);
		}
		return buffer;
	}

	private static Expresion array(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = new Expresion(ExpresionType.ARRAY);
		ArrayList<Expresion> array = new ArrayList<Expresion>();
		if(!token.current().is(TokenType.PUNCTOR, "]")){
			array.add(build(token));
			while(token.current().is(TokenType.PUNCTOR, ",")){
				token.next();
				array.add(build(token));
			}
		}
		exp.list = toArray(array);
		token.current().expect(TokenType.PUNCTOR, "]");
		token.next();
		return exp;
	}
}
