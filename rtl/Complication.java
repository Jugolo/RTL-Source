package rtl;

public class Complication {
	public static Complication normal(){
		return normal(null);
	}

	public static Complication normal(Object value){
		return new Complication(ComplicationType.NORMAL, value);
	}

	public static Complication _return(){
		return _return(null);
	}

	public static Complication _return(Object value){
		return new Complication(ComplicationType.RETURN, value);
	}

	public static Complication _break(){
		return new Complication(ComplicationType.BREAK, null);
	}

	public static Complication _continue(){
		return new Complication(ComplicationType.CONTINUE, null);
	}

	private ComplicationType type;
	private Object value;

	public Complication(ComplicationType type, Object value){
		this.type = type;
		this.value = value;
	}

	public ComplicationType type(){
		return this.type;
	}

	public Object value(){
		return this.value;
	}

	public boolean isNormal(){
		return this.type == ComplicationType.NORMAL;
	}

	public boolean isReturn(){
		return this.type == ComplicationType.RETURN;
	}

	public boolean isBreak(){
		return this.type == ComplicationType.BREAK;
	}

	public boolean isContinue(){
		return this.type == ComplicationType.CONTINUE;
	}
}
