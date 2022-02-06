package rtl;

enum ObjectAccess{
	PUBLIC("public"),
	PRIVATE("private");
	
	private String name;
	
	ObjectAccess(String name){
		this.name = name;
	}
	
	public String toString(){
		return this.name;
	}
}
