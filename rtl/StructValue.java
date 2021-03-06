package rtl;

import rtl.exception.RTLRuntimeException;

public class StructValue {
	private StructItem[] fields = new StructItem[0];
	private int fieldsSize = 0;
	protected Struct owner;

	public StructValue(Struct struct){
		this.owner = struct;
		this.fieldsSize = struct.getSize();
		this.fields = new StructItem[this.fieldsSize];
		StructField[] names = struct.getFields();
		for(int i=0;i<this.fieldsSize;i++){
			this.fields[i] = new StructItem(names[i]);
		}
	}

	public Object get(String name) throws RTLRuntimeException{
		//if the name is _struct it is not a item its containe.
		if(name.equals("_struct")){
			return new rtl.StructDec(this.owner, this);
		}
		for(int i=0;i<this.fieldsSize;i++){
			if(this.fields[i].field.name.equals(name))
				return new StructReference(this.fields[i]);
		}

		throw new RTLRuntimeException("Unknown struct field '"+name+"' in the struct "+this.owner.getName());
	}

	public String getName(){
		return this.owner.getName();
	}
	
	public StructItem[] getFields(){
		return this.fields;
	}
}
