package rtl;

public class StructReference implements IReference{
	private StructItem item;

	public StructReference(StructItem item){
		this.item = item;
	}

	public boolean hasBase(){
		return true;
	}

	public Object toValue(){
		return this.item.value;
	}

	public void put(Object context){
		this.item.value = context;
	}
}
