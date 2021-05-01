package rtl;

import rtl.exception.*;

import java.io.File;
import java.net.URISyntaxException;

public class Install {
  public static void main(String[] args){
  	File code = new File("install/install.rts");
  	if(!code.exists()){
  		System.err.println("Failed to finde the install program");
  		return;
  	}
	
	ProgramData pd = new ProgramData();
	pd.root = code.getParent();

  	Program program = new Program(pd);

	try{
	  VariabelDatabase db = new VariabelDatabase();
	  setFunctions(db);
	  program.run(ProgramBuilder.build(code), db);
	}catch(RTLInterprenterException e){
		System.err.println("Compile exception");
		System.err.println(e.getMessage());
	}catch(RTLRuntimeException e){
		System.err.println("Runtime exception");
		System.err.println("In file '"+program.lastFile()+"' on line "+program.lastLine());
		System.err.println(e.getMessage());
	}catch(Exception e){
		System.err.println("Native system exception throws");
		System.err.println(e.getMessage());
		System.err.println("---");
		e.printStackTrace();
	}
  }

  private static void setFunctions(VariabelDatabase db) throws RTLRuntimeException{
  	pushFunction(db, new Function("os", db, new CallableArgs(), new ICallable(){
  		public Object onCall(Program program, Object[] arg, VariabelDatabase db){
  			return System.getProperty("os.name").toLowerCase();
  		}
  	}));

  	pushFunction(db, new Function("mainPath", db, new CallableArgs(), new ICallable(){
  		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
  			try{
  				return new File(Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
  			}catch(URISyntaxException e){
  				throw new RTLRuntimeException(e.getMessage());
  			}
  		}
  	}));
  }

  private static void pushFunction(VariabelDatabase db, Function func) throws RTLRuntimeException{
  	VariableReference ref = db.get(func.name);
  	ref.put(func);
  	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
  }
}
