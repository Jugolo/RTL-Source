package rtl;

import rtl.exception.RTLRuntimeException;

public class TypeConveter {
	public static String type(Object obj){
		if(obj == null)
			return "null";
		if(obj instanceof String)
			return "string";
		if(obj instanceof Boolean)
			return "bool";
		if(obj instanceof Function)
			return "function";
		if(obj instanceof Integer)
			return "number";
		if(obj instanceof Double)
			return "number";
		if(obj instanceof Long)
			return "number";
		if(obj instanceof Struct)
			return "struct";
		if(obj instanceof StructValue)
			return "structValue";
		if(obj instanceof Array)
			return "array";
		//return obj.getClass().getName();
		return "undefined";
	}

	public static boolean isNumber(Object obj){
		return obj instanceof Integer || obj instanceof Double || obj instanceof Long;
	}

	public static Array array(Object obj) throws RTLRuntimeException{
		if(obj instanceof Array)
			return (Array)obj;
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to array");
	}

	public static Struct toStruct(Object obj) throws RTLRuntimeException{
		if(obj instanceof Struct)
			return (Struct)obj;
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to struct");
	}

	public static StructValue toStructValue(Object obj) throws RTLRuntimeException{
		if(obj instanceof StructValue)
			return (StructValue)obj;
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to structValue");
	}

	public static int toInt(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return (int)obj;
		if(obj instanceof Double)
			return (int)((double)obj);
		if(obj instanceof Long)
			return (int)((long)obj);
			
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to int");
	}

	public static double toDouble(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return (double)((int)obj);
		if(obj instanceof Double)
			return (double)obj;
		if(obj instanceof Long)
			return (double)((long)obj);
			
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to double");
	}

	public static long toLong(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return (long)((int)obj);
		if(obj instanceof Double)
			return (long)((double)obj);
		if(obj instanceof Long)
			return (long)obj;
			
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to long");
	}

	public static boolean bool(Object obj) throws RTLRuntimeException{
		if(obj instanceof Boolean)
			return (boolean)obj;
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to bool");
	}
	
	public static String string(Object obj) throws RTLRuntimeException{
		if(obj instanceof String)
			return (String)obj;

		if(obj instanceof Integer)
			return ((int)obj)+"";

		if(obj instanceof Double)
			return ((double)obj)+"";

		throw new RTLRuntimeException("Cant convert "+type(obj)+" to string");
	}

	public static Function toFunction(Object obj) throws RTLRuntimeException{
		if(obj instanceof Function)
			return (Function)obj;
			
		throw new RTLRuntimeException("Cant convert "+type(obj)+" to function");
	}
}
