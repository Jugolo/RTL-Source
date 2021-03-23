package rtl;

import java.util.ArrayList;

public class ProgramInstruc {
	private ArrayList<Statment> container = new ArrayList<Statment>();

	public int size(){
		return this.container.size();
	}

	public Statment get(int i){
		return this.container.get(i);
	}

	public void add(Statment statment){
		this.container.add(statment);
	}
}
