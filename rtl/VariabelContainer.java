package rtl;

public class VariabelContainer {
	private int attribute = 0;
	private Object value;

	public VariabelContainer(Object context){
		this.value = context;
	}

	public boolean attribute(int type){
		return (this.attribute & type) != 0;
	}

	public void addAttribute(int type){
		this.attribute = this.attribute | type;
	}

	public Object getValue(){
		return this.value;
	}

	public void putValue(Object value){
		this.value = value;
	}
}
