package rtl;

import rtl.exception.RTLRuntimeException;

public class Expresion {
	public ExpresionType type;
	public String str;
	public Expresion test;
	public Expresion left;
	public Expresion right;
	public Expresion[] list;
	public Statment[] block;
	public CallableArgs arg;

	public Expresion(ExpresionType type){
		this.type = type;
	}

	public Object get(Program program, VariabelDatabase db) throws RTLRuntimeException{
		Object l, r;
		IReference ref;
		switch(this.type){
			case STRING:
				return this.str;
			case BOOL:
				return this.str.equals("true");
			case ASSIGN:
				ref = Reference.toReference(this.left.get(program, db));
				Object v = this.right.get(program, db);
				switch(this.str){
					case "|=":
						v = TypeConveter.toInt(ref.toValue()) | TypeConveter.toInt(Reference.toValue(v));
					break;
					case "+=":
						v = RTLMath.plus(ref.toValue(), Reference.toValue(v));
					break;
					case "-=":
						v = RTLMath.minus(ref.toValue(), Reference.toValue(v));
					break;
				    default:
						v = Reference.toValue(v);
					break;
				}
				
				ref.put(v);
				return v;
			case IDENTIFY:
				return db.get(this.str);
			case CALL:
				Object obj = Reference.toValue(this.left.get(program, db));
				return TypeConveter.toFunction(obj).call(program, this.getArgs(program, this.list, db));
			case NUMBER:
			    try{
					if(this.str.indexOf(".") == -1)
						return Integer.parseInt(this.str);
					return Double.parseDouble(this.str);
			    }catch(NumberFormatException e){
			    	return Long.parseLong(this.str);
			    }
			case COMPARE:
			    l = Reference.toValue(this.left.get(program, db));
			    r = Reference.toValue(this.right.get(program, db));
			    
			    if(this.str.equals("!="))
					return !RTLCompare.equal(l, r);
				return RTLCompare.equal(l, r);
			case TYPEOF:
			    l = this.left.get(program, db);
			    if(l instanceof IReference){
			    	return ((IReference)l).hasBase() ? TypeConveter.type(Reference.toValue(l)) : "undefined";
			    }
			    return TypeConveter.type(l);
			case STRUCT:
				return new StructValue(TypeConveter.toStruct(db.get(this.str).toValue()));
			case STRUCT_GET:
				return TypeConveter.toStructValue(Reference.toValue(this.left.get(program, db))).get(this.str);
			case MATH:
				l = Reference.toValue(this.left.get(program, db));
				r = Reference.toValue(this.right.get(program, db));
				if(this.str.equals("+"))
					return RTLMath.plus(l, r);

				return RTLMath.minus(l, r);
			case ARRAY:
				Array array = new Array();
				for(int i=0;i<this.list.length;i++){
					array.add(Reference.toValue(this.list[i].get(program, db)));
				}
				return array;
			case ARRAY_GET:
			    if(this.right == null)
					return new ArrayReference(TypeConveter.array(Reference.toValue(this.left.get(program, db))));
				return new ArrayReference(TypeConveter.array(Reference.toValue(this.left.get(program, db))), TypeConveter.toInt(Reference.toValue(this.right.get(program, db))));
			case SIZE:
			    if(this.str.equals("<"))
					return RTLCompare.RG(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				if(this.str.equals("<="))
					return RTLCompare.RGE(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				if(this.str.equals(">="))
					return RTLCompare.LGE(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				return RTLCompare.LG(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
			case SELF_INC:
			    ref = Reference.toReference(this.left.get(program, db));
			    Object vsi;
				if(this.str.equals("++"))
					vsi = RTLMath.plus(ref.toValue(), 1);
				else
					vsi = RTLMath.minus(ref.toValue(), 1);
				ref.put(vsi);
				return vsi;
			case NULL:
				return null;
			case NPC:
				Object npc = Reference.toValue(this.left.get(program, db));
				if(npc instanceof Integer)
					return this.str.equals("+") ? +((int)npc) : -((int)npc);
				if(npc instanceof Double)
					return this.str.equals("+") ? +((double)npc) : -((int)npc);
				return this.str.equals("+") ? +((long)npc) : -((long)npc);
			case NOT:
				return !TypeConveter.bool(Reference.toValue(this.left.get(program, db)));
			case AO:
				if(this.str.equals("&&"))
					return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) && TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
				return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) || TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
			case POW:
				if(this.str.equals("*"))
					return RTLMath.additiv(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				return RTLMath.subtiv(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
			case ASK: 
				if(TypeConveter.bool(Reference.toValue(this.test.get(program, db))))
					return Reference.toValue(this.left.get(program, db));
				return Reference.toValue(this.right.get(program, db));
			case BLOCK:
				VariabelDatabase blockDB = new VariabelDatabase();
				blockDB.setLast(db);
				Complication c = program.run(new ProgramInstrucList(this.block), blockDB);
				if(c.isReturn())
					return c.value();
				if(c.isBreak())
					throw new RTLRuntimeException("Break cant be used outsite a block");
				if(c.isContinue())
					throw new RTLRuntimeException("Continue cant be used outsite a block");
				return null;
			case FUNCTION:
				return FunctionUntil.getCallable("<inline>", db, this.arg, this.block);
			case BITWISE:
			    if(this.str.equals(">>"))
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) >> TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				if(this.str.equals(">>>"))
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) >>> TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) << TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
			case BITWISEOR:
				return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) | TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
			case BITWISEAND:
				return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) & TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
			case BITWISENOT:
				return ~TypeConveter.toInt(Reference.toValue(this.left.get(program, db)));
			case BITWISEXOR:
				return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) ^ TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
		}

		throw new RTLRuntimeException("Unknown expresion type: "+this.type);
	}

	private Object[] getArgs(Program program, Expresion[] args, VariabelDatabase db) throws RTLRuntimeException{
		Object[] arg = new Object[args.length];
		for(int i=0;i<arg.length;i++)
			arg[i] = Reference.toValue(args[i].get(program, db));
		return arg;
	}
}
