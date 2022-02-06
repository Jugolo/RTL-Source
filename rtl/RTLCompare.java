package rtl;

import rtl.exception.RTLRuntimeException;

public class RTLCompare{
	public static boolean LG(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use > on non numric value");
			
		if(l instanceof Integer)
			return (int)l > TypeConveter.toInt(r);
		if(l instanceof Long)
			return (long)l > TypeConveter.toLong(r);
		return (double)l > TypeConveter.toDouble(r);
	}
	
	public static boolean RG(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use < on non numric value");
			
		if(l instanceof Integer)
			return (int)l < TypeConveter.toInt(r);
			
		if(l instanceof Long)
			return (long)l < TypeConveter.toLong(r);
			
		return (double)l < TypeConveter.toDouble(r);
	}
	
	public static boolean LGE(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use >= on non numric value");
			
		if(l instanceof Integer)
			return (int)l >= TypeConveter.toInt(r);
		if(l instanceof Long)
			return (long)l >= TypeConveter.toLong(r);
		return (double)l >= TypeConveter.toDouble(r);
	}
	
	public static boolean RGE(Object l, Object r) throws RTLRuntimeException{
		if(!TypeConveter.isNumber(l) || !TypeConveter.isNumber(r))
			throw new RTLRuntimeException("Cant use <= on non numric value");
			
		if(l instanceof Integer)
			return (int)l <= TypeConveter.toInt(r);
			
		if(l instanceof Long)
			return (long)l <= TypeConveter.toLong(r);
			
		return (double)l <= TypeConveter.toDouble(r);
	}
	
	public static boolean equal(Object l, Object r) throws RTLRuntimeException{
		if(l == null || r == null)
			return l == r;
	    
	    RTLType lt = TypeConveter.type(l);
	    
	    if(lt != TypeConveter.type(r))
			return false;
				
		if(lt == RTLType.NUMBER){
			if(l instanceof Integer)
				return (int)l == TypeConveter.toInt(r);
			
			if(l instanceof Long)
				return (long)l == TypeConveter.toLong(r);
				
			return (double)l == TypeConveter.toDouble(r);
		}
						
	    return l.equals(r);
	}
}
