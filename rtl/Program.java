package rtl;

import rtl.Statment;
import rtl.exception.*;
import rtl.local.LocalFile;

import java.util.ArrayList;
import java.io.File;
import java.net.URISyntaxException;

public class Program{
	private ArrayList<Statment> statment = new ArrayList<Statment>();
	public final ProgramData data;
	private ArrayList<ExcutionPosition> lastPos = new ArrayList<ExcutionPosition>();
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
		Object[] ob;
		this.pushPos(statment.file, statment.line);
		switch(statment.type){
			case PRINTLN:
			    ob = toObjectArray(statment.expresions, db);
			    if(this.data.print != null){
					this.data.print.println(this, ob);
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
			    if(this.data.print != null){
					this.data.print.print(this, ob);
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
			    if(this.data.print != null){
					this.data.print.readln(this, statment.name);
					this.popPos();
					return Complication.normal();
				}
				db.get(statment.name).put(System.console().readLine());
				this.popPos();
				return Complication.normal();
			case INCLUDE:
				c = getProgram(getIncludePath(Reference.toValue(statment.expresion.get(this, db))), db, statment.file, statment.line);
				this.popPos();
				return c;
			case FUNCTION:
			    Function func = FunctionUntil.getCallable(statment.name, statment.returnType, db, statment.arg, statment.body);
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
				ref.put(new Struct(statment.name, statment.struct));
				ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
				v = ref.toValue();
				this.popPos();
				return Complication.normal(v);
			case CONST:
				ref = db.get(statment.name);
				if(statment.expresion != null){
					ref.put(Reference.toValue(statment.expresion.get(this, db)));
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
			    	ref.put(Reference.toValue(statment.expresion.get(this, db)));
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
	
	private String getIncludePath(Object o) throws RTLRuntimeException{
		if(o instanceof LocalFile){
			return ((LocalFile)o).getPath();
		}
		return TypeConveter.string(o);
	}

	private Complication getProgram(String path, VariabelDatabase db, String file, int line) throws RTLRuntimeException{
		if(isLocalPath(path)){
			LocalPath.eval(path, db, this.data);
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
			code = new File(this.data.root, path);
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
	
	private Object[] toObjectArray(Expresion[] array, VariabelDatabase db) throws RTLRuntimeException{
		Object[] buffer = new Object[array.length];
		for(int i=0;i<array.length;i++)
			buffer[i] = Reference.toValue(array[i].get(this, db));
		return buffer;
	}
}
