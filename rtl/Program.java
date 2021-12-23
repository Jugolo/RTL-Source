package rtl;

import rtl.Statment;
import rtl.exception.*;
import rtl.plugin.Plugin;

import java.util.ArrayList;
import java.io.File;
import java.net.URISyntaxException;

public class Program{
	private ArrayList<Statment> statment = new ArrayList<Statment>();
	public final ProgramData data;
	private ArrayList<ExcutionPosition> lastPos = new ArrayList<ExcutionPosition>();
	private boolean isFinish = true;
	private Plugin plugin = new Plugin();
	private ProgramRunner runner = new ProgramRunner(this);
	private String[] localPath = new String[]{
		"rtl.math.rand",
		"rtl.math.function",
		"rtl.typeconveter",
		"rtl.io.tcp.server",
		"rtl.io.tcp.client",
		"rtl.io.file",
		"rtl.io.dir",
		"rtl.system.thread",
		"rtl.string",
		"rtl.array",
		"rtl.time",
		"rtl",
		//from version 1.1
		"rtl.db.mysql",
		"rtl.error",
		"rtl.test.asset",
		//from version 1.2
		"rtl.encode.base64",
		"rtl.script"
	};

	public Program(ProgramData data){
		this.data = data;
	}
	
	public void start(){
		this.isFinish = false;
	}
	
	public void stop(){
		this.isFinish = true;
	}

	public Complication run(ProgramInstruc instruct, VariabelDatabase db) throws RTLException{
		this.isFinish = false;
		Complication state = this.runner.run(instruct, db);
		this.isFinish = true;
		return state;
	}
	

	public String lastFile(){
		return this.runner.getLastFile();
	}

	public int lastLine(){
		return this.runner.getLastLine();
	}
	
	public boolean isFinish(){
		return this.isFinish && this.plugin.isFinish();
	}
	
	public boolean isProgramFinish(){
		return this.isFinish;
	}
	
	public void close(){
		this.plugin.close();
		this.isFinish = true;
	}
	
	public Plugin getPlugin(){
		return this.plugin;
	}
	
	public ProgramRunner getProgramEvoluator(){
		return this.runner;
	}
}
