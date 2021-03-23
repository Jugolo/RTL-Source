package rtl;

import rtl.exception.RTLRuntimeException;

public class Expresion {
	public ExpresionType type;
	public String str;
	public Expresion test;
	public Expresion left;
	public Expresion right;
	public Expresion[] list;

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
				Object v = Reference.toValue(this.right.get(program, db));
				ref.put(v);
				return v;
			case IDENTIFY:
				return db.get(this.str);
			case CALL:
				Object obj = Reference.toValue(this.left.get(program, db));
				return TypeConveter.toFunction(obj).call(program, this.getArgs(program, this.list, db));
			case NUMBER:
				return Integer.parseInt(this.str);
			case COMPARE:
			    l = Reference.toValue(this.left.get(program, db));
			    r = Reference.toValue(this.right.get(program, db));
			    
			    if(this.str.equals("!=")){
			    	if(l == null || r == null)
			    		return l != r;
			    	return !l.equals(r);
			    }

			    if(l == null || r == null)
			    	return l == r;
				return l.equals(r);
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
				if(this.str.equals("+")){
					if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
						return TypeConveter.string(l)+TypeConveter.string(r);
					return TypeConveter.toInt(l)+TypeConveter.toInt(r);
				}

				return TypeConveter.toInt(l)-TypeConveter.toInt(r);
			case ARRAY:
				Array array = new Array();
				for(int i=0;i<this.list.length;i++){
					array.add(this.list[i].get(program, db));
				}
				return array;
			case ARRAY_GET:
				return new ArrayReference(TypeConveter.array(Reference.toValue(this.left.get(program, db))), 
				this.right == null ? -1 : TypeConveter.toInt(Reference.toValue(this.right.get(program, db))));
			case SIZE:
			    if(this.str.equals("<"))
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) < TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				if(this.str.equals("<="))
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) <= TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				if(this.str.equals(">="))
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) >= TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) > TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
			case SELF_INC:
			    ref = Reference.toReference(this.left.get(program, db));
				if(this.str.equals("++"))
					ref.put(TypeConveter.toInt(ref.toValue())+1);
				else
					ref.put(TypeConveter.toInt(ref.toValue())-1);
				return ref;
			case NULL:
				return null;
			case NPC:
				int npc = TypeConveter.toInt(Reference.toValue(this.left.get(program, db)));
				return this.str.equals("+") ? +npc : -npc;
			case NOT:
				return !TypeConveter.bool(Reference.toValue(this.left.get(program, db)));
			case AO:
				if(this.str.equals("&&"))
					return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) && TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
				return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) || TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
			case POW:
				int lp = TypeConveter.toInt(Reference.toValue(this.left.get(program, db)));
				int rp = TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				if(this.str.equals("*"))
					return lp*rp;
				if(this.str.equals("/"))
					return lp/rp;
				return (int)Math.pow(lp, rp);
			case ASK: 
				if(TypeConveter.bool(Reference.toValue(this.test.get(program, db))))
					return Reference.toValue(this.left.get(program, db));
				return Reference.toValue(this.right.get(program, db));
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
