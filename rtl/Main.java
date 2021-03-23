package rtl;

import rtl.exception.*;

import java.io.File;

public class Main {
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Failed to run the program. Reason: No args to file");
			return;
		}

		int pos = args[0].lastIndexOf(".");
		if(pos == -1 || !args[0].substring(pos+1).equals("rts")){
			System.out.println("Unknown file prototcol. Only accept .rts");
			return;
		}

		//okay let get the file and see what happens
		File programCode = new File(args[0]);
		if(!programCode.exists()){
			System.out.println("Failed to locate the file. Please controle the path");
			return;
		}

        Program program = new Program(programCode.getParent());

		try{
		  VariabelDatabase db = new VariabelDatabase();
		  program.run(ProgramBuilder.build(programCode), db);
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
}
