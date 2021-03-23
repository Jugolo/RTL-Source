package rtl;

public class ProgramInstrucList extends ProgramInstruc{
	private Statment[] list;
	
	public ProgramInstrucList(Statment[] list){
		this.list = list;	
	}

	public int size(){
		return this.list.length;
	}

	public Statment get(int i){
		return this.list[i];
	}
}
