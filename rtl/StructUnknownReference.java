package rtl;

import rtl.exception.*;

public class StructUnknownReference implements IReference{
	private StructItem set;
	private StructItem get;
	private String name;
	private Program program;
	private StructValue self;
	
	public StructUnknownReference(StructValue self, StructItem get, StructItem set, String name, Program program){
		this.get = get;
		this.set = set;
		this.name = name;
		this.program = program;
		this.self = self;
	}
	
	public void put(Object obj) throws RTLException{
		if(this.set == null){
			throw new RTLRuntimeException("Unknown struct field '"+name+"' in the struct "+this.self.getName());
		}
		TypeConveter.toFunction(this.set.value).call(this.program, new Object[]{this.name, obj}, this.self);
	}
	
	public Object toValue() throws RTLException{
		if(this.get == null){
			throw new RTLRuntimeException("Unknown struct field '"+name+"' in the struct "+this.self.getName());
		}
		return TypeConveter.toFunction(this.get.value).call(this.program, new Object[]{this.name}, this.self);
	}
	
	public boolean hasBase(){
		return true;
	}
	
	public Object getBase(){
		return this.self;
	}
}
