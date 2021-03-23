package rtl;

import rtl.exception.RTLRuntimeException;

public class Reference {
	public static IReference toReference(Object v) throws RTLRuntimeException{
		if(!(v instanceof IReference))
			throw new RTLRuntimeException("Cant convert "+TypeConveter.type(v)+" to reference");
		return (IReference)v;
	}

	public static Object toValue(Object v) throws RTLRuntimeException{
		if(v instanceof IReference)
			return ((IReference)v).toValue();
		return v;
	}
}
