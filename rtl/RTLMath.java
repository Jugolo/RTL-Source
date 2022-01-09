package rtl;

import rtl.exception.RTLRuntimeException;

public class RTLMath{
	public static Object minus(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use - on non numberic value");
			
		if(l instanceof Integer)
			return (int)l - TypeConveter.toInt(r);
			
		if(l instanceof Long)
			return (long)l - TypeConveter.toLong(r);
			
		return TypeConveter.toDouble(l) - TypeConveter.toDouble(r);
	}
	
	public static Object plus(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			return TypeConveter.string(l)+TypeConveter.string(r);
			
		if(l instanceof Integer)
			return (int)l + TypeConveter.toInt(r);
			
		if(l instanceof Long)
			return (long)l + TypeConveter.toLong(r);
			
		return TypeConveter.toDouble(l) + TypeConveter.toDouble(r);
	}
	
	public static Object additiv(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use the sign * on non numric value");
			
		if(l instanceof Integer)
			return (int)l * TypeConveter.toInt(r);
		if(l instanceof Long)
			return (long)l * TypeConveter.toLong(r);
		return (double)l * TypeConveter.toDouble(r);
	}
	
	public static Object subtiv(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use the sign / on non numric value");
		if(l instanceof Integer){
			if(TypeConveter.toInt(r) == 0)
				throw new RTLRuntimeException("Cant use the sign / on zero");
			return (int)l / TypeConveter.toInt(r);
		}
			
		if(l instanceof Long){
		    if(TypeConveter.toLong(r) == 0L)
				throw new RTLRuntimeException("Cant use the sign / on zero");
			return (long)l / TypeConveter.toLong(r);
		}
		
		if(TypeConveter.toDouble(r) == 0.0)
			throw new RTLRuntimeException("Cant use the sign / on zero");
		return (double)l / TypeConveter.toDouble(r);
	}
	
	public static Object modus(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use the sign % on non numric value");
			
		if(l instanceof Integer)
			return (int)l % TypeConveter.toInt(r);
		if(l instanceof Long)
			return (long)l % TypeConveter.toLong(r);
		return (double)l % TypeConveter.toDouble(r);
	}
}
