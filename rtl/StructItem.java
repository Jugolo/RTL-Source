package rtl;

public class StructItem {
	public final StructField field;
	public Object value = null;
	public boolean hasSet = false;

	public StructItem(StructField field){
		this.field = field;
		if(field.context != null){
			this.value = field.context;
			this.hasSet = true;
		}
	}
}
