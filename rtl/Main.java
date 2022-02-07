package rtl;

import rtl.exception.*;

import java.io.File;

public class Main {
	public static String VERSION = "V2.3";
	public static void main(String[] args){
		if(args.length == 0){
			args = new String[]{
				"index.rts"
			};
		}
		
		ProgramData pd = new ProgramData();
		
		int fileIndex = 0;
		boolean profile = false;
		long time = 0l;
		
		while(true){
			if(args.length > fileIndex+1){
				if(args[fileIndex].equals("@test")){
					fileIndex++;
					pd.test = true;
				}else if(args[fileIndex].equals("@profile")){
					fileIndex++;
					profile = true;
				}else{
					break;
				}
			}else{
				break;
			}
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
		  if(profile)
			time = System.nanoTime();
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
		
		if(profile)
			System.out.println("Profile: "+(time-System.nanoTime()));
		
		if(wait)
			System.console().readLine();
	}
}
