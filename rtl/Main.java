package rtl;

import rtl.exception.*;

import java.io.File;

public class Main {
	public static String VERSION = "V1.2";
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Failed to run the program. Reason: No args to file");
			return;
		}
		
		ProgramData pd = new ProgramData();
		int fileIndex = 0;
		if(args[fileIndex].equals("@test") && args.length > fileIndex+1){
			fileIndex++;
			pd.test = true;
		}

		boolean wait = args[fileIndex].indexOf("file://") == 0;

		int pos = args[fileIndex].lastIndexOf(".");
		if(pos == -1 || !args[fileIndex].substring(pos+1).equals("rts")){
			System.out.println("Unknown file prototcol. Only accept .rts");
			if(wait)
				System.console().readLine();
			return;
		}

		if(wait){
			args[fileIndex] = args[fileIndex].substring(7);
		}

		//okay let get the file and see what happens
		File programCode = new File(args[fileIndex]);
		if(!programCode.exists()){
			System.out.println("Failed to locate the file. Please controle the path");
			if(wait)
				System.console().readLine();
			return;
		}
        pd.root = programCode.getParent();
        Program program = new Program(pd);

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
		if(wait)
			System.console().readLine();
	}
}
