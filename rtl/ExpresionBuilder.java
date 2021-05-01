package rtl;

import rtl.token.Tokenizer;
import rtl.token.TokenBuffer;
import rtl.token.TokenType;
import rtl.exception.RTLInterprenterException;

import java.util.ArrayList;

public class ExpresionBuilder {
	public static Expresion build(Tokenizer token) throws RTLInterprenterException{
		return ask(token);
	}

	private static Expresion ask(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = assign(token);
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

	private static Expresion assign(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = AO(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"=", "|=", "+=", "-="})){
			Expresion buffer = new Expresion(ExpresionType.ASSIGN);
			buffer.left = exp;
			buffer.str = token.current().context();
			token.next();
			buffer.right = assign(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion AO(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = compare(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"&&", "||"})){
			Expresion buffer = new Expresion(ExpresionType.AO);
			buffer.left = exp;
			buffer.str = token.current().context();
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
			buffer.str = token.current().context();
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
	
	private static Expresion bitwise(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getSizeCompare(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"<<", ">>", ">>>"})){
			Expresion buffer = new Expresion(ExpresionType.BITWISE);
			buffer.left = exp;
			buffer.str = token.current().context();
			token.next();
			buffer.right = bitwise(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion getSizeCompare(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getMath(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"<", ">", "<=", ">="})){
			Expresion buffer = new Expresion(ExpresionType.SIZE);
			buffer.left = exp;
			buffer.str = token.current().context();
			token.next();
			buffer.right = getSizeCompare(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion getMath(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getPow(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"+", "-"})){
			Expresion buffer = new Expresion(ExpresionType.MATH);
			buffer.left = exp;
			buffer.str = token.current().context();
			token.next();
			buffer.right = getMath(token);
			return buffer;
		}
		return exp;
	}

	private static Expresion getPow(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = getPrefix(token);
		if(token.current().is(TokenType.PUNCTOR, new String[]{"*", "/"})){
			Expresion buffer = new Expresion(ExpresionType.POW);
			buffer.left = exp;
			buffer.str = token.current().context();
			token.next();
			buffer.right = getPow(token);
			return buffer;
		}
		return exp;
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
			exp.str = token.current().context();
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
			exp.str = buffer.context();
			return exp;
		}

		if(buffer.is(TokenType.NUMBER)){
			Expresion exp = new Expresion(ExpresionType.NUMBER);
			exp.str = buffer.context();
			return exp;
		}

		if(buffer.is(TokenType.KEYWORD, "struct")){
			Expresion exp = new Expresion(ExpresionType.STRUCT);
			token.current().expect(TokenType.IDENTIFY);
			exp.str = token.current().context();
			token.next();
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
			exp.str = token.current().context();
			token.next();
			return exp;
		}
		
		return before;
	}

	private static Expresion func(Tokenizer token) throws RTLInterprenterException{
		Expresion exp = new Expresion(ExpresionType.FUNCTION);
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
