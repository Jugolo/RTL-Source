package rtl.exception;

public class RTLInterprenterException extends RTLException{
	public RTLInterprenterException(String msg){
		super(msg);
	}

	public RTLInterprenterException(String msg, String file, int line){
		super(msg+" in file '"+file+"'("+line+")");
	}
}
