package rtl;

import java.util.ArrayList;

public class CallableArgs {
	private ArrayList<String> arg = new ArrayList<String>();

	public void add(String name){
		this.arg.add(name);
	}

	public int count(){
		return this.arg.size();
	}

	public String getName(int i){
		return this.arg.get(i);
	}
}
