package rtl.nativestruct;

import rtl.Struct;
import rtl.StructField;

public class StringStruct extends Struct{
	public StringStruct(){
		super("STRING", new String[]{
			"length"
		});
		
		StructField[] fields = this.getFields();
		for(int i=0;i<fields.length;i++){
			fields[i].isConst = true;
		}
	}
}
