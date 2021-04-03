package rtl;

import rtl.Statment;
import rtl.exception.*;

import java.util.ArrayList;
import java.io.File;
import java.net.URISyntaxException;

public class Program{
	private ArrayList<Statment> statment = new ArrayList<Statment>();
	private String root;
	private ArrayList<ExcutionPosition> lastPos = new ArrayList<ExcutionPosition>();
	private String[] localPath = new String[]{
		"rtl.math.rand",
		"rtl.typeconveter",
		"rtl.io.tcp.server",
		"rtl.io.tcp.client",
		"rtl.io.file",
		"rtl.io.dir",
		"rtl.system.thread",
		"rtl.string",
		"rtl.array",
		"rtl.time",
		"rtl"
	};

	public Program(String root){
		this.root = root;
	}

	public Complication run(ProgramInstruc instruct, VariabelDatabase db) throws RTLRuntimeException{
		for(int i=0;i<instruct.size();i++){
			Complication buffer = runStep(instruct.get(i), db);
			if(buffer.type() != ComplicationType.NORMAL)
				return buffer;
		}
		return Complication.normal();
	}

	public String lastFile(){
		if(this.lastPos.size() == 0)
			return "<unknown>";
		return this.lastPos.get(this.lastPos.size()-1).file;
	}

	public int lastLine(){
		if(this.lastPos.size() == 0)
			return -1;
		return this.lastPos.get(this.lastPos.size()-1).line;
	}

	private void pushPos(String file, int line){
		this.lastPos.add(new ExcutionPosition(file, line));
	}

	private void popPos(){
		this.lastPos.remove(this.lastPos.size()-1);
	}

	private Complication runStep(Statment statment, VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref;
		Complication c;
		Object v;
		this.pushPos(statment.file, statment.line);
		switch(statment.type){
			case PRINTLN:
				System.out.println(TypeConveter.string(Reference.toValue(statment.expresion.get(this, db))));
				this.popPos();
				return Complication.normal();
			case PRINT:
				System.out.print(TypeConveter.string(Reference.toValue(statment.expresion.get(this, db))));
				this.popPos();
				return Complication.normal();
			case READLN:
				db.get(statment.name).put(System.console().readLine());
				this.popPos();
				return Complication.normal();
			case INCLUDE:
				c = getProgram(TypeConveter.string(statment.expresion.get(this, db)), db, statment.file, statment.line);
				this.popPos();
				return c;
			case FUNCTION:
				Function func = new Function(statment.name, db, statment.arg, new ICallable(){
					public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
							Complication c = program.run(new ProgramInstrucList(statment.body), db);
							if(c.type() == ComplicationType.RETURN)
								return c.value();
							return null;
					}
				});
				ref = db.get(statment.name);
				ref.put(func);
				ref.attribute(VariabelAttribute.NOT_WRITE);
				ref.attribute(VariabelAttribute.GLOBAL);
				this.popPos();
				return Complication.normal(func);
			case WHILE:
				while(TypeConveter.bool(Reference.toValue(statment.expresion.get(this, db)))){
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
				 if(statment.forData.first != null){
				 	Reference.toValue(statment.forData.first.get(this, db));
				 }

				 while(statment.forData.second == null || TypeConveter.bool(Reference.toValue(statment.forData.second.get(this, db)))){
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
				 	if(statment.forData.last != null){
				 		Reference.toValue(statment.forData.last.get(this, db));
				 	}
				 }
				 this.popPos();
				 return Complication.normal();
			case EXPRESION:
			     v = statment.expresion.get(this, db);
				 this.popPos();
			     return Complication.normal(v);
			case IF:
				if(TypeConveter.bool(Reference.toValue(statment.expresion.get(this, db)))){
					c = runBody(statment.body, db);
					this.popPos();
					return c;
				}else if(statment.after != null){
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
				c = Complication._return(statment.expresion == null ? null : Reference.toValue(statment.expresion.get(this, db)));
				this.popPos();
				return c;
			case STRUCT:
				ref = db.get(statment.name);
				ref.put(new Struct(statment.name, statment.context));
				ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
				v = ref.toValue();
				this.popPos();
				return Complication.normal(v);
			case GLOBAL:
			    ref = db.get(statment.name);
			    if(statment.expresion != null){
			    	ref.put(Reference.toValue(statment.expresion.get(this, db)));
			    }else{
			    	if(!ref.hasBase())
			    		ref.put(null);
			    }
			    ref.attribute(VariabelAttribute.GLOBAL);
			    v = ref.toValue();
			    this.popPos();
			    return Complication.normal(v);
			case BREAK:
			    this.popPos();
				return Complication._break();
			case CONTINUE:
				this.popPos();
				return Complication._continue();
		}
		throw new RTLRuntimeException("Unknown statment type: "+statment.type);
	}

	private Complication runBody(Statment[] list, VariabelDatabase db) throws RTLRuntimeException{
		for(int i=0;i<list.length;i++){
			Complication c = runStep(list[i], db);
			if(c.type() != ComplicationType.NORMAL){
				return c;
			}
		}

		return Complication.normal();
	}

	private Complication getProgram(String path, VariabelDatabase db, String file, int line) throws RTLRuntimeException{
		if(isLocalPath(path)){
			LocalPath.eval(path, db, this.root);
			return Complication.normal();
		}
		//if there is more end .. stop it here!!!
		if(path.indexOf("..") != -1)
			throw new RTLRuntimeException("There must not be more end one dot in path");

		File code;
		if(path.indexOf("rtl") == 0){
			try{
				code = new File(new File(Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "rtlSystem/"+path.replace(".", "/")+".rts");
			}catch(URISyntaxException e){
				code = new File("./rtlSystem/"+path.replace(".", "/")+".rts");
			}
		}else{
			code = new File(this.root, path);
		}
		
		if(!code.exists() || !code.isFile())
			throw new RTLRuntimeException("Unknown script file: "+path);

		ProgramInstruc included = null;
		try{
        	included = ProgramBuilder.build(code);
		}catch(java.io.FileNotFoundException e){
			throw new RTLRuntimeException("Failed to load the incluede file: "+path);
		}catch(RTLInterprenterException e){
			throw new RTLRuntimeException("Failed to compile the include file: "+path+" reason: "+e.getMessage());
		}
		
		return this.run(included, db);
	}

	private boolean isLocalPath(String path){
		for(int i=0;i<this.localPath.length;i++){
			if(this.localPath[i].equals(path))
				return true;
		}
		return false;
	}
}
