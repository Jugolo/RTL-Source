package rtl;

import rtl.exception.*;
import rtl.array.IArray;
import rtl.array.Array;
import rtl.array.StringArray;
import rtl.nativestruct.StringStruct;

public class TypeConveter {
	public static RTLType type(Object obj){
		if(obj == null)
			return RTLType.NULL;
		if(obj instanceof String)
			return RTLType.STRING;
		if(obj instanceof Boolean)
			return RTLType.BOOL;
		if(obj instanceof Function)
			return RTLType.FUNCTION;
		if(obj instanceof Integer)
			return RTLType.NUMBER;
		if(obj instanceof Double)
			return RTLType.NUMBER;
		if(obj instanceof Long)
			return RTLType.NUMBER;
		if(obj instanceof Struct)
			return RTLType.STRUCT;
		if(obj instanceof StructValue)
			return RTLType.STRUCTVALUE;
		if(obj instanceof IArray)
			return RTLType.ARRAY;
		if(obj instanceof Byte)
			return RTLType.BYTE;
		if(obj instanceof IClass)
			return RTLType.CLASS;
		if(obj instanceof IObject)
			return RTLType.OBJECT;
		System.out.println(obj.getClass().getName());
		return RTLType.UNDEFINED;
	}

	public static boolean isNumber(Object obj){
		return obj instanceof Integer || obj instanceof Double || obj instanceof Long;
	}

	public static IArray array(Object obj) throws RTLRuntimeException{
		if(obj instanceof IArray)
			return (IArray)obj;
		if(obj instanceof String)
			return new StringArray((String)obj);
		wrongType(obj, "array");
		return null;
	}

	public static Struct toStruct(Object obj) throws RTLRuntimeException{
		if(obj instanceof Struct)
			return (Struct)obj;
		wrongType(obj, "struct");
		return null;
	}

	public static StructValue toStructValue(Object obj, Program program, VariabelDatabase db) throws RTLException{
		if(obj instanceof StructValue)
			return (StructValue)obj;
		if(obj instanceof Array)
			return ((Array)obj).toStructValue(program, db);
		if(obj instanceof String){
			String str = (String)obj;
			StructValue value = new StructValue(new StringStruct(), program, new Object[0]);
			((StructReference)value.get("length", program, db)).put(str.length());
			return value;
		}
			
		wrongType(obj, "structValue");
		return null;
	}

	public static int toInt(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return (int)obj;
		if(obj instanceof Double)
			return (int)Math.round((double)obj);
		if(obj instanceof Long)
			return (int)((long)obj);
		if(obj instanceof Byte)
			return Byte.toUnsignedInt((byte)obj);
			
		wrongType(obj, "int");
		return -1;
	}

	public static double toDouble(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return ((Integer)obj).doubleValue();
		if(obj instanceof Double)
			return (double)obj;
		if(obj instanceof Long)
			return (double)((long)obj);
			
		wrongType(obj, "double");
		return -1.0;
	}

	public static long toLong(Object obj) throws RTLRuntimeException{
		if(obj instanceof Integer)
			return ((Integer)obj).longValue();
		if(obj instanceof Double)
			return (long)((double)obj);
		if(obj instanceof Long)
			return (long)obj;
			
		wrongType(obj, "long");
		return -1;
	}

	public static boolean bool(Object obj) throws RTLRuntimeException{
		if(obj instanceof Boolean)
			return (boolean)obj;
			
		wrongType(obj, "bool");
		return false;
	}
	
	public static String string(Object obj) throws RTLRuntimeException{
		if(obj instanceof String)
			return (String)obj;

		if(obj instanceof Integer){
			return ((int)obj)+"";
		}

		if(obj instanceof Double){
		    double d = (double)obj;
		    if((d % 1) == 0)
				return Math.round(d)+"";
			return d+"";
		}
			
		if(obj instanceof Long)
			return ((long)obj)+"";
			
		if(obj instanceof Byte)
			return ((byte)obj)+"";

		wrongType(obj, "string");
		return null;
	}
	
	public static byte toByte(Object obj) throws RTLRuntimeException{
		if(obj instanceof Byte)
			return (byte)obj;
			
		if(obj instanceof Integer)
			return ((Integer)obj).byteValue();
			
		wrongType(obj, "byte");
		return 0;
	}

	public static Function toFunction(Object obj) throws RTLRuntimeException{
		if(obj instanceof Function)
			return (Function)obj;
			
		wrongType(obj, "function");
		return null;
	}
	
	public static IClass toClass(Object obj) throws RTLRuntimeException{
		if(obj instanceof IClass)
			return (IClass)obj;
		wrongType(obj, "class");
		return null;
	}
	
	public static IObject toObject(Object obj) throws RTLRuntimeException{
		if(obj instanceof IObject)
			return (IObject)obj;
			
		wrongType(obj, "object");
		return null;
	}
	
	private static void wrongType(Object given, String to) throws RTLRuntimeException{
		throw new RTLRuntimeException("Cant convert "+type(given).toString()+" to "+to);
	}
}
