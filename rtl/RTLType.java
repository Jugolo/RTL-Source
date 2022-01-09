package rtl;

enum RTLType{
	NULL("null"),
	STRING("string"),
	BOOL("bool"),
	FUNCTION("function"),
	NUMBER("number"),
	STRUCT("struct"),
	STRUCTVALUE("structValue"),
	ARRAY("array"),
	BYTE("byte"),
	UNDEFINED("undefined"),
	CLASS("class"),
	OBJECT("object");
	
	private String name;
	
	RTLType(String name){
		this.name = name;
	}
	
	public String toString(){
		return this.name;
	}
}
