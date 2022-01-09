package rtl;

import rtl.exception.*;
import rtl.plugin.Plugin;
import rtl.plugin.IPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

class ProgramRunner{
	private int[] line = new int[100];
	private String[] file = new String[100];
	private int lineCount = 0;
	private int fileCount = 0;
	private boolean isFinish = false;
	private Program program;
	
	public ProgramRunner(Program program){
		this.program = program;
	}
	
	public int getLastLine(){
		if(this.lineCount < 1 || this.lineCount > this.file.length)
			return -1;
		return this.line[this.lineCount-1];
	}
	
	public String getLastFile(){
		if(this.fileCount < 1 || this.fileCount > this.file.length)
			return "<unknown>";
		return this.file[this.fileCount-1];
	}
	
	public Complication run(ProgramInstruc instruct, VariabelDatabase db) throws RTLException{
		for(int i=0;i<instruct.size() && !this.program.isFinish();i++){
			Complication buffer = runStep(instruct.get(i), db);
			if(buffer.type() != ComplicationType.NORMAL)
				return buffer;
		}
		return Complication.normal();
	}
	
	private void popPos(){
		this.fileCount--;
		this.lineCount--;
	}
	
	private void pushPos(String file, int line) throws RTLRuntimeException{
		if(this.line.length == this.lineCount)
			throw new RTLRuntimeException("The file and line stack is full.");
		this.line[this.lineCount++] = line;
		this.file[this.fileCount++] = file;
	}
	
	public Complication runStep(Statment statment, VariabelDatabase db) throws RTLException{
		VariableReference ref;
		Complication c;
		Object v;
		Object[] ob;
		this.pushPos(statment.file, statment.line);
		switch(statment.type){
			case PRINTLN:
			    ob = toObjectArray(statment.expresions, db);
			    if(this.program.data.print != null){
					this.program.data.print.println(this.program, ob);
					this.popPos();
					return Complication.normal();
				}
				String println;
				if(ob.length == 0){
					println = "";
				}else{
					println = TypeConveter.string(ob[0]);
					for(int i=1;i<ob.length;i++){
						println += ", "+TypeConveter.string(ob[i]);
					}
				}
				System.out.println(println);
				this.popPos();
				return Complication.normal();
			case PRINT:
			    ob = toObjectArray(statment.expresions, db);
			    if(this.program.data.print != null){
					this.program.data.print.print(this.program, ob);
					this.popPos();
					return Complication.normal();
				}
				String print;
				if(ob.length == 0)
					print = "";
				else{
					print = TypeConveter.string(ob[0]);
					for(int i=1;i<ob.length;i++)
						print += ", "+TypeConveter.string(ob[i]);
				}
				System.out.print(print);
				this.popPos();
				return Complication.normal();
			case READLN:
			    if(this.program.data.print != null){
					this.program.data.print.readln(this.program, statment.name);
					this.popPos();
					return Complication.normal();
				}
				db.get(statment.name).put(System.console().readLine());
				this.popPos();
				return Complication.normal();
			case INCLUDE:
			    v = Reference.toValue(statment.expresion.get(this.program, db));
			    if(v instanceof IPathInfo){
					c = getProgram(new File(((IPathInfo)v).getPath()), db, statment.file, statment.line);
				}else{
					c = getProgram(TypeConveter.string(v), db, statment.file, statment.line);
				}
				this.popPos();
				return c;
			case FUNCTION:
			    Function func = FunctionUntil.getCallable(statment.name, statment.returnType, db, statment.arg, statment.body);
				ref = db.get(statment.name);
				ref.put(func);
				ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
				this.popPos();
				return Complication.normal(func);
			case WHILE:
				while(!this.program.isProgramFinish() && TypeConveter.bool(Reference.toValue(statment.expresion.get(this.program, db)))){
					c = runBody(statment.body, db);
					if(!c.isNormal()){
						if(c.isBreak()){
							this.popPos();
							return Complication.normal();
						}else if(c.isContinue()){
							c = Complication.normal();
							continue;
						}
						this.popPos();
						return c;
					}
				}
				this.popPos();
				return Complication.normal();
			case FOR:
				 Reference.toValue(statment.forData.first.get(this.program, db));

				 while(TypeConveter.bool(Reference.toValue(statment.forData.second.get(this.program, db)))){
				 	c = runBody(statment.body, db);
				 	if(!c.isNormal()){
				 		if(c.isBreak()){
				 			this.popPos();
				 			return Complication.normal();
				 		}else if(c.isContinue()){
							c = Complication.normal();
						}else{
				 			this.popPos();
				 			return c;
						}
				 	}
				 	Reference.toValue(statment.forData.last.get(this.program, db));
				 }
				 this.popPos();
				 return Complication.normal();
			case EXPRESION:
			     v = Reference.toValue(statment.expresion.get(this.program, db));
				 this.popPos();
			     return Complication.normal(v);
			case IF:
				if(TypeConveter.bool(Reference.toValue(statment.expresion.get(this.program, db)))){
					c = runBody(statment.body, db);
					this.popPos();
					return c;
				}
				
				if(statment.after != null){
				    c = runStep(statment.after, db);
				    this.popPos();
				    return c;
				}
				
				this.popPos();
				return Complication.normal();
			case ELSE:
				c = runBody(statment.body, db);
				this.popPos();
				return c;
			case RETURN:
				c = Complication._return(statment.expresion == null ? null : Reference.toValue(statment.expresion.get(this.program, db)));
				this.popPos();
				return c;
			case STRUCT:
				ref = db.get(statment.name);
				ref.put(new Struct(statment.name, evulateStructField(statment.struct, this.program, db)));
				ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
				v = ref.toValue();
				this.popPos();
				return Complication.normal(v);
			case CONST:
				ref = db.get(statment.name);
				if(statment.expresion != null){
					ref.put(Reference.toValue(statment.expresion.get(this.program, db)));
				}else{
					if(!ref.hasBase())
						ref.put(null);
				}
				ref.attribute(VariabelAttribute.NOT_WRITE);
			    if(statment.isGlobal)
			    	ref.attribute(VariabelAttribute.GLOBAL);
			    v = ref.toValue();
			    this.popPos();
			    return Complication.normal(v);
			case GLOBAL:
			    ref = db.get(statment.name);
			    if(statment.expresion != null){
			    	ref.put(Reference.toValue(statment.expresion.get(this.program, db)));
			    }else{
			    	if(!ref.hasBase())
			    		ref.put(null);
			    }
			    ref.attribute(VariabelAttribute.GLOBAL);
			    if(statment.isConst)
			    	ref.attribute(VariabelAttribute.NOT_WRITE);
			    v = ref.toValue();
			    this.popPos();
			    return Complication.normal(v);
			case BREAK:
			    this.popPos();
				return Complication._break();
			case CONTINUE:
				this.popPos();
				return Complication._continue();
			case CLASS:
				ref = db.get(statment.name);
				ref.put(new ScriptClass(statment.name));
				ref.attribute(VariabelAttribute.GLOBAL | VariabelAttribute.NOT_WRITE);
				this.popPos();
				return Complication.normal();
		}
		throw new RTLRuntimeException("Unknown statment type: "+statment.type);
	}
	
	private StructField[] evulateStructField(StructField[] fields, Program program, VariabelDatabase db) throws RTLException{
		int size = fields.length;
		for(int i=0;i<size;i++){
			if(fields[i].context instanceof Expresion){
				fields[i].context = Reference.toValue(((Expresion)fields[i].context).get(program, db));
			}
		}
		return fields;
	}
	
	private Complication runBody(Statment[] list, VariabelDatabase db) throws RTLException{
		for(int i=0;i<list.length && !this.program.isProgramFinish();i++){
			Complication c = this.runStep(list[i], db);
			if(c.type() != ComplicationType.NORMAL){
				return c;
			}
		}

		return Complication.normal();
	}
	
	private Complication getProgram(File code, VariabelDatabase db, String file, int line) throws RTLException{
		if(!code.exists() || !code.isFile())
			throw new RTLRuntimeException("Unknown script file: "+code.getPath());

		ProgramInstruc included = null;
		try{
        	included = ProgramBuilder.build(code);
		}catch(java.io.FileNotFoundException e){
			throw new RTLRuntimeException("Failed to load the incluede file: "+code.getPath());
		}catch(RTLInterprenterException e){
			throw new RTLRuntimeException("Failed to compile the include file: "+code.getPath()+" reason: "+e.getMessage());
		}
		
		return this.run(included, db);
	}
	
	private Complication getProgram(String path, VariabelDatabase db, String file, int line) throws RTLException{
		Plugin plugin = this.program.getPlugin();
		if(plugin.shouldInlize())
			plugin.inilize();
		IPlugin current = plugin.getPlugin(path);
		if(current != null){
			current.init(db, this.program);
			return Complication.normal();
		}
		
		if(path.indexOf("..") != -1)
			throw new RTLRuntimeException("A include file path must not containe ..");
			
		if(path.indexOf("rtl") == 0){
			try{
				//maby it a local include file??
				File localFile = new File(new File(Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "rtlSystem/"+path.replace(".", "/")+".rts");
				if(localFile.exists()){
					return getProgram(localFile, db, file, line);
				}
			}catch(URISyntaxException e){}//if there are a exception in the try body. dont care just continue
		}
			
		File f = new File(this.program.data.root, path);
		if(!f.exists()){
			throw new RTLRuntimeException("Unknown include file: "+path);
		}
		
		try{
			return this.run(ProgramBuilder.build(f), db);
		}catch(FileNotFoundException | RTLInterprenterException e){
			throw new RTLRuntimeException("Failed to parse include file '"+path+"' becuse: "+e.getMessage());
		}
	}
	
	private Object[] toObjectArray(Expresion[] array, VariabelDatabase db) throws RTLException{
		Object[] buffer = new Object[array.length];
		for(int i=0;i<array.length;i++)
			buffer[i] = Reference.toValue(array[i].get(this.program, db));
		return buffer;
	}
}
