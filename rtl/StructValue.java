package rtl;

import rtl.exception.*;
import rtl.array.IArray;

public class StructValue implements IArray{
	private StructItem[] fields = new StructItem[0];
	private int fieldsSize = 0;
	protected Struct owner;
	private StructItem $get;
	private StructItem $set;
	private Object $arrayget;
	private Object $arrayput;
	private Object $arrayadd;
	private Object $arraysize;
	
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
			switch(buffer.field.name){
				case "$construct":
					constructor = buffer;
				break;
				case "$get":
					this.$get = buffer;
				break;
			    case "$set":
			        this.$set = buffer;
			    break;
			    case "$arrayGet":
					this.$arrayget = buffer.value;
				break;
				case "$arrayPut":
				   this.$arrayput = buffer.value;
				break;
				case "$arrayAdd":
				   this.$arrayadd = buffer.value;
				break;
				case "$arraySize":
			       this.$arraysize = buffer.value;
			    break;
			}
			this.fields[i] = buffer;
		}
		
		//in version 2.0 wee support a constructor fields when call
		if(constructor != null && !(this instanceof StructDec))
			TypeConveter.toFunction(constructor.value).call(program, args, this);
	}

	public Object get(String name, Program program, VariabelDatabase db) throws RTLException{
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
	
	public Object get(Program program, VariabelDatabase db, Object key) throws RTLException{
		return TypeConveter.toFunction(this.$arrayget).call(program, new Object[]{key}, db);
	}
	
	public void add(Program program, VariabelDatabase db, Object obj) throws RTLException{
		TypeConveter.toFunction(this.$arrayadd).call(program, new Object[]{obj}, db);
	}
	
	public void put(Program program, VariabelDatabase db, Object key, Object context) throws RTLException{
		TypeConveter.toFunction(this.$arrayput).call(program, new Object[]{key, context}, db);
	}
	
	public int size(Program program, VariabelDatabase db) throws RTLException{
		return TypeConveter.toInt(TypeConveter.toFunction(this.$arraysize).call(program, new Object[0], db));
	}
}
