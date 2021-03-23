package rtl;

import rtl.exception.RTLRuntimeException;

public class VariableReference implements IReference{
	private String name;
	private VariabelDatabase container;
	
	public VariableReference(String name, VariabelDatabase container){
		this.name = name;
		this.container = container;
	}

	public void put(Object context) throws RTLRuntimeException{
		if(this.container.containes(name)){
			if(this.getAttribute(VariabelAttribute.NOT_WRITE))
				throw new RTLRuntimeException(this.name+" can't be overriden");

			this.container.getContainer(this.name).putValue(context);
			return;
		}

		this.container.put(this.name, new VariabelContainer(context));
	}

	public void attribute(int arg){
		container.getContainer(this.name).addAttribute(arg);
	}

	public boolean getAttribute(int a){
		return container.getContainer(this.name).attribute(a);
	}

	public Object toValue() throws RTLRuntimeException{
		if(this.container.containes(this.name))
			return this.container.getContainer(this.name).getValue();
		throw new RTLRuntimeException("Unknown identify '"+this.name+"'");
	}

	public VariabelDatabase db(){
		return this.container;
	}

	public boolean hasBase(){
		return this.container.containes(this.name);
	}
}
