package rtl;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import rtl.exception.RTLRuntimeException;
import rtl.exception.RTLInterprenterException;
import rtl.exception.RTLException;
import rtl.local.TcpSocketClient;
import rtl.local.LocalFile;

public class LocalPath {
	private static Random rand = new Random();
	
	public static void eval(String name, VariabelDatabase db, String root) throws RTLRuntimeException{
		switch(name){
			case "rtl.math.rand":
				rand(db);
			break;
		    case "rtl.typeconveter":
		    	typeconveter(db);
		    break;
		    case "rtl.io.tcp.server":
		    	iotcpserver(db);
		    break;
		    case "rtl.io.tcp.client":
				iotcp(db);
		    break;
		    case "rtl.io.file":
				iofile(db, root);
		    break;
		    case "rtl.io.dir":
		        iodir(db, root);
		    break;
		    case "rtl.system.thread":
		    	thread(db, root);
		    break;
		    case "rtl.string":
		    	string(db);
		    break;
		    case "rtl.array":
		    	array(db);
		    break;
		    case "rtl.time":
		    	time(db);
		    break;
		    case "rtl":
		    	rtl(db);
		    break;
		}
	}

	private static void rtl(VariabelDatabase db) throws RTLRuntimeException{
		Struct RTL = new Struct("RTL", new String[]{
			"version"
		});
		
		VariableReference ref = db.get("RTL");
		ref.put(RTL);
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		
		ref = db.get("rtl");
		ref.put(new Function("rtl", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				StructValue rtl = TypeConveter.toStructValue(db.get("RTL"));
				((StructReference)rtl.get("version")).put(Main.VERSION);
				return rtl;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void array(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference  ref = db.get("count");
		CallableArgs arg = new CallableArgs();
		arg.add("array");
		ref.put(new Function("count", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.array(arg[0]).size();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("in_array");
		arg = new CallableArgs();
		arg.add("array");
		arg.add("find");
		ref.put(new Function("in_array", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				Array array = TypeConveter.array(arg[0]);
				for(int i=0;i<array.size();i++){
					if(array.get(i).equals(arg[1]))
						return true;
				}
				return false;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void string(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("str_split");
		CallableArgs arg = new CallableArgs();
		arg.add("str");
		arg.add("seperator");
		ref.put(new Function("str_split", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new Array(TypeConveter.string(arg[0]).split(TypeConveter.string(arg[1])));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

        ref = db.get("strpos");
		arg = new CallableArgs();
		arg.add("str");
		arg.add("find");
		ref.put(new Function("strpos", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).indexOf(TypeConveter.string(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("strlpos");
		ref.put(new Function("strlpos", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).lastIndexOf(TypeConveter.string(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("substr");
		arg = new CallableArgs();
		arg.add("str");
		arg.add("start");
		arg.add("length");
		ref.put(new Function("substr", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				String str = TypeConveter.string(arg[0]);
				int start = TypeConveter.toInt(arg[1]);
				int length = TypeConveter.toInt(arg[2]);
				try{
					if(length == 0){
						return str.substring(start);
					}
					return str.substring(start, length);
				}catch(IndexOutOfBoundsException e){
					return "";
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("strlen");
		arg = new CallableArgs();
		arg.add("str");
		ref.put(new Function("strlen", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).length();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("ord");
		ref.put(new Function("ord", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				try{
					return (int)TypeConveter.string(arg[0]).charAt(0);
				}catch(IndexOutOfBoundsException e){
					return -1;
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("chr");
		ref.put(new Function("chr", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return ((char)TypeConveter.toInt(arg[0]))+"";
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("strchar");
	    arg = new CallableArgs();
	    arg.add("str");
	    arg.add("index");
	    ref.put(new Function("strchar", db, arg, new ICallable(){
	    	public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
	    		try{
	    			return TypeConveter.string(arg[0]).charAt(TypeConveter.toInt(arg[1]))+"";
	    		}catch(IndexOutOfBoundsException e){
	    			return "";
	    		}
	    	}
	    }));
	    ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void thread(VariabelDatabase db, String root) throws RTLRuntimeException{
		VariableReference ref = db.get("thread");
		CallableArgs arg = new CallableArgs();
		arg.add("func");
		ref.put(new Function("thread", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db){
				Thread thread = new Thread(new Runnable(){
					public void run(){
						Program p = new Program(root);
						try{
							TypeConveter.toFunction(arg[0]).call(p, new Object[0]);
						}catch(RTLRuntimeException e){
							System.err.println("Runtime exception");
							System.err.println("In file '"+p.lastFile()+"' on line "+p.lastLine());
							System.err.println(e.getMessage());
							System.exit(1);
						}catch(Exception e){
							System.err.println("Native system exception throws");
							System.err.println(e.getMessage());
							System.err.println("---");
							e.printStackTrace();
							System.exit(1);
						}
					}
				});
				thread.start();
				return null;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void iofile(VariabelDatabase db, String rootName) throws RTLRuntimeException{
		VariableReference ref = db.get("file_exists");
    	CallableArgs arg = new CallableArgs();
    	arg.add("path");
    	ref.put(new Function("file_exists", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			return new File(TypeConveter.string(arg[0])).exists();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("file");
    	arg = new CallableArgs();
    	arg.add("path");
    	arg.add("mode");
    	ref.put(new Function("file", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			return new LocalFile(TypeConveter.string(arg[0]), TypeConveter.string(arg[1]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_path");
    	arg = new CallableArgs();
    	arg.add("file");
    	ref.put(new Function("file_path", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_path exceptct the first argument to be file stream");
    			}

    			return ((LocalFile)arg[0]).getPath();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_setExecutable");
    	ref.put(new Function("file_setExecutable", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_path exceptct the first argument to be file stream");
    			}

    			return ((LocalFile)arg[0]).setExecutable();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_size");
    	arg = new CallableArgs();
    	arg.add("file");
    	ref.put(new Function("file_size", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_size exceptct the first argument to be file stream");
    			}

    			return ((LocalFile)arg[0]).getSize();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_readLength");
    	arg = new CallableArgs();
    	arg.add("file");
    	arg.add("index");
    	arg.add("length");
    	ref.put(new Function("file_readLength", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_size exceptct the first argument to be file stream");
    			}

    			return ((LocalFile)arg[0]).readLength(TypeConveter.toInt(arg[1]), TypeConveter.toInt(arg[2]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_readBytes");
    	arg = new CallableArgs();
    	arg.add("file");
    	arg.add("index");
    	arg.add("length");
    	ref.put(new Function("file_readBytes", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_readBytes exceptct the first argument to be file stream");
    			}

    			return ((LocalFile)arg[0]).readBytes(TypeConveter.toInt(arg[1]), TypeConveter.toInt(arg[2]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_write");
    	arg = new CallableArgs();
    	arg.add("file");
    	arg.add("message");
    	ref.put(new Function("file_write", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_write exceptct the first argument to be file stream");
    			}

    			((LocalFile)arg[0]).write(TypeConveter.string(arg[1]));
    			return null;
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("file_writeln");
    	ref.put(new Function("file_writeln", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof LocalFile)){
    				throw new RTLRuntimeException("file_writeln exceptct the first argument to be file stream");
    			}

    			((LocalFile)arg[0]).write(TypeConveter.string(arg[1])+"\r\n");
    			return null;
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void iodir(VariabelDatabase db, String root) throws RTLRuntimeException{
		VariableReference ref = db.get("dir");
		CallableArgs arg = new CallableArgs();
		arg.add("path");
		ref.put(new Function("dir", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new File(TypeConveter.string(arg[0]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("dir_exists");
		arg = new CallableArgs();
		arg.add("dir");
		ref.put(new Function("dir_exists", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof File)){
					throw new RTLRuntimeException("dir_exists expect argument 1 to be a part of dir stream");
				}

				File f = (File)arg[0];
				return f.exists() && f.isDirectory();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("dir_create");
		ref.put(new Function("dir_create", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof File)){
					throw new RTLRuntimeException("dir_exists expect argument 1 to be a part of dir stream");
				}

				File f = (File)arg[0];
				if(f.exists() && f.isDirectory())
					return false;
				return f.mkdir();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

    private static void iotcp(VariabelDatabase db) throws RTLRuntimeException{
    	VariableReference ref = db.get("tcp");
    	CallableArgs arg = new CallableArgs();
    	arg.add("server");
    	arg.add("port");
    	ref.put(new Function("tcp", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			try{
    				return new TcpSocketClient(new Socket(TypeConveter.string(arg[0]), TypeConveter.toInt(arg[1])));
    			}catch(IOException e){
    				throw new RTLRuntimeException(e.getMessage());
    			}
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
    	
    	ref = db.get("tcp_readln");
    	arg = new CallableArgs();
    	arg.add("tcp");
    	ref.put(new Function("tcp_readln", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_readln first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).readLine();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_read");
    	ref.put(new Function("tcp_read", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_read first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).read();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
    	
    	ref = db.get("tcp_ip");
    	ref.put(new Function("tcp_ip", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_ip first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).ip();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_flush");
    	ref.put(new Function("tcp_flush", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_ip first argument needed to be tcp socket stream");
    			}
    			return ((TcpSocketClient)arg[0]).flush();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_close");
    	ref.put(new Function("tcp_close", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_close first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).close();
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	arg = new CallableArgs();
    	arg.add("tcp");
    	arg.add("length");
    	ref = db.get("tcp_readLength");
    	ref.put(new Function("tcp_readLength", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_readLength first argument needed to be tcp socket stream");
    			}
    			return ((TcpSocketClient)arg[0]).readLength(TypeConveter.toInt(arg[1]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
    	

    	arg = new CallableArgs();
    	arg.add("tcp");
    	arg.add("message");
    	ref = db.get("tcp_writeln");
    	ref.put(new Function("tcp_writeln", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_writeln first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).writeln(TypeConveter.string(arg[1]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_write");
    	ref.put(new Function("tcp_write", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_write first argument needed to be tcp socket stream");
    			}

    			return ((TcpSocketClient)arg[0]).write(TypeConveter.string(arg[1]));
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_writeBytes");
    	ref.put(new Function("tcp_writeBytes", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_writeBytes first argument needed to be tcp socket stream");
    			}

    			if(!(arg[1] instanceof byte[])){
    				throw new RTLRuntimeException("tcp_writeBytes need second argument to be byte array");
    			}

    			return ((TcpSocketClient)arg[0]).writeBytes((byte[])arg[1]);
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

    	ref = db.get("tcp_lastError");
    	arg = new CallableArgs();
    	arg.add("tcpstream");
    	ref.put(new Function("tcp_lastError", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				return "<unknowntcpstream>";
    			}
    			return ((TcpSocketClient)arg[0]).errmsg;
    		}
    	}));
    	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
    }

	private static void iotcpserver(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("tcpserver_open");
		CallableArgs arg = new CallableArgs();
		arg.add("port");
		ref.put(new Function("tcpserver_open", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				int port = TypeConveter.toInt(arg[0]);
				try{
					return new ServerSocket(port);
				}catch(IOException e){
					throw new RTLRuntimeException(e.getMessage());
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

        ref = db.get("tcpserver_accept");
        arg = new CallableArgs();
        arg.add("connection");
        ref.put(new Function("tcpserver_accept", db, arg, new ICallable(){
        	public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
        		if(!(arg[0] instanceof ServerSocket)){
        			throw new RTLRuntimeException("tcpserver_accept first argument need to be tcpserver stream");
        		}

				try{
					return new TcpSocketClient(((ServerSocket)arg[0]).accept());
				}catch(IOException e){
					throw new RTLRuntimeException(e.getMessage());
				}
        	}
        }));
        ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	

	private static void typeconveter(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("toInt");
		CallableArgs arg = new CallableArgs();
		arg.add("str");
		ref.put(new Function("toInt", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				try{
					return Integer.parseInt(TypeConveter.string(arg[0]));
				}catch(NumberFormatException e){
					return null;
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE);
		ref.attribute(VariabelAttribute.GLOBAL);
	}

	private static void rand(VariabelDatabase db) throws RTLRuntimeException{
		CallableArgs arg = new CallableArgs();
		arg.add("min");
		arg.add("max");
		VariableReference ref = db.get("rand");
		ref.put(new Function("rand", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				int min = TypeConveter.toInt(arg[0]);
				int max = TypeConveter.toInt(arg[1]);
				return rand.nextInt((max - min) + 1) + min;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE);
		ref.attribute(VariabelAttribute.GLOBAL);
	}

	private static void time(VariabelDatabase db) throws RTLRuntimeException{
		Struct TIME = new Struct("TIME", new String[]{
			"date",
			"day",
			"fullYear",
			"year",
			"hour",
			"milisecond",
			"second",
			"minuts",
			"month",
			"now",
		});
		VariableReference ref = db.get("TIME");
		ref.put(TIME);
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("current_time");
		ref.put(new Function("current_time", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				StructValue time = new StructValue(TIME);
				Calendar c = Calendar.getInstance();
				((IReference)time.get("date")).put(c.get(Calendar.DATE));
				((IReference)time.get("day")).put(c.get(Calendar.DAY_OF_WEEK));
				((IReference)time.get("fullYear")).put(c.get(Calendar.YEAR));
				((IReference)time.get("year")).put((c.get(Calendar.YEAR)+"").substring(2));
				((IReference)time.get("hour")).put(c.get(Calendar.HOUR_OF_DAY));
				((IReference)time.get("milisecond")).put(c.get(Calendar.MILLISECOND));
				((IReference)time.get("second")).put(c.get(Calendar.SECOND));
				((IReference)time.get("minuts")).put(c.get(Calendar.MINUTE));
				((IReference)time.get("month")).put(c.get(Calendar.MONTH));
				((IReference)time.get("now")).put(c.getTime().getTime());
				return time;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("utcstring");
		CallableArgs arg = new CallableArgs();
		arg.add("time");
		ref.put(new Function("utcstring", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				Calendar c = Calendar.getInstance();
				long l = arg[0] instanceof Long ? (long)arg[0] : 0;
				c.setTime(new Date(l));
				String[] DayName = new String[]{
					"Sun",
					"Mon",
					"Tue",
					"Wed",
					"Thu",
					"Fri",
					"Sat"
				};

				String[] MonthName = new String[]{
					"Jan",
					"Feb",
					"Mar",
					"Apr",
					"May",
					"Jun",
					"Jul",
					"Aug",
					"Sep",
					"Oct",
					"Nov",
					"Dec"
				};

				return DayName[c.get(Calendar.DAY_OF_WEEK)-1]+", "+twoDigit(c.get(Calendar.DATE))+" "+
				MonthName[c.get(Calendar.MONTH)]+" "+c.get(Calendar.YEAR)+" "+twoDigit(c.get(Calendar.HOUR_OF_DAY))+":"+
				twoDigit(c.get(Calendar.MINUTE))+":"+twoDigit(c.get(Calendar.SECOND))+" GMT";
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static String twoDigit(int n){
		return n < 10 ? "0"+n : n+"";
	}
}
