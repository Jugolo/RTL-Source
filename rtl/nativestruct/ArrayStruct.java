package rtl.nativestruct;

import rtl.Struct;
import rtl.StructField;

public class ArrayStruct extends Struct{
	public ArrayStruct(){
		super("ARRAY", new String[]{"length"});
		
		StructField[] fields = this.getFields();
		for(int i=0;i<fields.length;i++){
			fields[i].isConst = true;
		}
	}
}
