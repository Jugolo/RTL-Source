package rtl;

import rtl.exception.*;

import java.util.HashMap;
import java.util.Map;

public class Expresion {
	public ExpresionType type;
	public String str;
	public String returnType;
	public Expresion test;
	public Expresion left;
	public Expresion right;
	public Expresion[] list;
	public Statment[] block;
	public CallableArgs arg;
	public HashMap<String, Expresion> structArgs = new HashMap<String, Expresion>();
	public EXPSign sign;

	public Expresion(ExpresionType type){
		this.type = type;
	}

	public Object get(Program program, VariabelDatabase db) throws RTLException{
		Object l, r;
		IReference ref;
		switch(this.type){
			case STRING:
				return this.str;
			case BOOL:
			    return this.sign == EXPSign.TRUE;//true
			case ASSIGN:
				ref = Reference.toReference(this.left.get(program, db));
				Object v = this.right.get(program, db);
				switch(this.sign){
					case BITWISE_ASSIGN:
						v = TypeConveter.toInt(ref.toValue()) | TypeConveter.toInt(Reference.toValue(v));
					break;
					case PLUS_ASSIGN:
						v = RTLMath.plus(ref.toValue(), Reference.toValue(v));
					break;
					case MINUS_ASSIGN:
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
			    Object _this = null;
			    l = this.left.get(program, db);
			    if(l instanceof StructReference)
					_this = Reference.toReference(l).getBase();
				return TypeConveter.toFunction(Reference.toValue(l)).call(program, this.getArgs(program, this.list, db), _this);
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
			    
			    if(this.sign == EXPSign.NOT_EQUAL)
					return !RTLCompare.equal(l, r);
				return RTLCompare.equal(l, r);
			case TYPEOF:
			    l = this.left.get(program, db);
			    if(l instanceof IReference){
			    	return ((IReference)l).hasBase() ? TypeConveter.type(Reference.toValue(l)).toString() : "undefined";
			    }
			    return TypeConveter.type(l).toString();
			case STRUCT:
			    Struct struct = TypeConveter.toStruct(db.get(this.str).toValue());
			    Object[] args = this.list == null ? new Object[0] : getArgs(program, this.list, db);
				StructValue sv =  new StructValue(TypeConveter.toStruct(db.get(this.str).toValue()), program, args);
				if(this.structArgs.size() > 0){
					for(Map.Entry<String, Expresion> entry : this.structArgs.entrySet()){
						Reference.toReference(sv.get(entry.getKey(), program)).put(Reference.toValue(entry.getValue().get(program, db)));
					}
				}
				return sv;
			case STRUCT_GET:
				return TypeConveter.toStructValue(Reference.toValue(this.left.get(program, db)), program).get(this.str, program);
			case MATH:
				l = Reference.toValue(this.left.get(program, db));
				r = Reference.toValue(this.right.get(program, db));
				if(this.sign == EXPSign.PLUS)
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
			    if(this.sign == EXPSign.CREATER)//<
					return RTLCompare.RG(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				if(this.sign == EXPSign.CREATER_EQUAL)//<=
					return RTLCompare.RGE(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				if(this.sign == EXPSign.LESS_EQUAL)//>=
					return RTLCompare.LGE(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				return RTLCompare.LG(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
			case SELF_INC:
			    ref = Reference.toReference(this.left.get(program, db));
			    Object vsi;
			    if(this.sign == EXPSign.SELF_INC)//++
					vsi = RTLMath.plus(ref.toValue(), 1);
				else//--
					vsi = RTLMath.minus(ref.toValue(), 1);
				ref.put(vsi);
				return vsi;
			case NULL:
				return null;
			case NPC:
				Object npc = Reference.toValue(this.left.get(program, db));
				if(npc instanceof Integer)
					return this.sign == EXPSign.PLUS ? +TypeConveter.toInt(npc) : -TypeConveter.toInt(npc);
				if(npc instanceof Double)
					return this.sign == EXPSign.PLUS ? +TypeConveter.toDouble(npc) : -TypeConveter.toDouble(npc);
				return this.sign == EXPSign.PLUS ? +TypeConveter.toLong(npc) : -TypeConveter.toDouble(npc);
			case NOT:
				return !TypeConveter.bool(Reference.toValue(this.left.get(program, db)));
			case AO:
			    if(this.sign == EXPSign.AND)//&&
					return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) && TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
			    //||
				return TypeConveter.bool(Reference.toValue(this.left.get(program, db))) || TypeConveter.bool(Reference.toValue(this.right.get(program, db)));
			case POW:
			    if(this.sign == EXPSign.GANGE)//*
					return RTLMath.additiv(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				if(this.sign == EXPSign.MOD)//%
					return RTLMath.modus(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
				// /
				return RTLMath.subtiv(Reference.toValue(this.left.get(program, db)), Reference.toValue(this.right.get(program, db)));
			case ASK: 
				if(TypeConveter.bool(Reference.toValue(this.test.get(program, db))))
					return Reference.toValue(this.left.get(program, db));
				return Reference.toValue(this.right.get(program, db));
			case BLOCK:
				VariabelDatabase blockDB = new VariabelDatabase();
				blockDB.setLast(db);
				Complication c = program.getProgramEvoluator().run(new ProgramInstrucList(this.block), blockDB);
				if(c.isReturn())
					return c.value();
				if(c.isBreak())
					throw new RTLRuntimeException("Break cant be used outsite a block");
				if(c.isContinue())
					throw new RTLRuntimeException("Continue cant be used outsite a block");
				return null;
			case FUNCTION:
				return FunctionUntil.getCallable("<inline>", this.returnType, db, this.arg, this.block);
			case BITWISE:
			    if(this.sign == EXPSign.SIGN_R)//>>
					return TypeConveter.toInt(Reference.toValue(this.left.get(program, db))) >> TypeConveter.toInt(Reference.toValue(this.right.get(program, db)));
				if(this.sign == EXPSign.UNSIGN_R)//>>>
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
			case NEW:
				return TypeConveter.toClass(Reference.toValue(db.get(this.str))).newInstance(this.getArgs(program, this.list, db));
			case THIS:
			    l = db.getThis();
			    if(l == null)
					throw new RTLRuntimeException("Can`t use the keyword 'this'");
				return l;
		}

		throw new RTLRuntimeException("Unknown expresion type: "+this.type);
	}

	private Object[] getArgs(Program program, Expresion[] args, VariabelDatabase db) throws RTLException{
		Object[] arg = new Object[args.length];
		for(int i=0;i<arg.length;i++)
			arg[i] = Reference.toValue(args[i].get(program, db));
		return arg;
	}
}
