package rtl;

import rtl.exception.*;

public class StructValue {
	private StructItem[] fields = new StructItem[0];
	private int fieldsSize = 0;
	protected Struct owner;
	private StructItem $get;
	private StructItem $set;
	
	public StructValue(Struct struct, Program program) throws RTLException{
		this(struct, program, new Object[0]);
	}
	
	public StructValue(Struct struct, Program program, Object[] args) throws RTLException{
		this.owner = struct;
		this.fieldsSize = struct.getSize();
		this.fields = new StructItem[this.fieldsSize];
		StructField[] names = struct.getFields();
		StructItem constructor = null;
		for(int i=0;i<this.fieldsSize;i++){
			StructItem buffer = new StructItem(names[i]);
			if(buffer.field.name.equals("$construct"))
				constructor = buffer;
			if(buffer.field.name.equals("$get"))
			    this.$get = buffer;
			if(buffer.field.name.equals("$set"))
				this.$set = buffer;
			this.fields[i] = buffer;
			
		}
		
		//in version 2.0 wee support a constructor fields when call
		if(constructor != null && !(this instanceof StructDec))
			TypeConveter.toFunction(constructor.value).call(program, args, this);
	}

	public Object get(String name, Program program) throws RTLException{
		//if the name is _struct it is not a item its containe.
		if(name.equals("_struct")){
			return new rtl.StructDec(this.owner, this, program);
		}
		
		for(int i=0;i<this.fieldsSize;i++){
			if(this.fields[i].field.name.equals(name))
				return new StructReference(this.fields[i], this);
		}
		
		//have it a value called $call?
		return new StructUnknownReference(this, this.$get, this.$set, name, program);
	}

	public String getName(){
		return this.owner.getName();
	}
	
	public StructItem[] getFields(){
		return this.fields;
	}
}
