package rtl.exception;

public class RTLException extends Exception{
	private String file = "unknown";
	private int line = -1;
	
	public RTLException(String msg){
		super(msg);
	}
}
