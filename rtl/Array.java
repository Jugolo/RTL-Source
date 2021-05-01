package rtl;

import java.util.ArrayList;

public class Array {
	private ArrayList<Object> list = new ArrayList<Object>();

    public Array(){}

    public Array(String[] arg){
    	for(int i=0;i<arg.length;i++){
    		this.list.add(arg[i]);
    	}
    }

	public void add(Object obj){
		this.list.add(obj);
	}

	public Object get(int i){
		return this.list.get(i);
	}

	public int size(){
		return this.list.size();
	}

	public void put(int i, Object context){
		if(i == this.size())
			this.add(context);
		else
			this.list.set(i, context);
	}
}
