package rtl;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.Math;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import rtl.exception.RTLRuntimeException;
import rtl.exception.RTLInterprenterException;
import rtl.exception.RTLException;
import rtl.local.TcpSocketClient;
import rtl.local.LocalFile;
import rtl.local.LocalDir;
import rtl.local.MySQL;
import rtl.local.MySQLResult;

public class LocalPath {
	private static Random rand = new Random();
	
	public static void eval(String name, VariabelDatabase db, ProgramData data) throws RTLRuntimeException{
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
			iofile(db);
		    break;
		    case "rtl.io.dir":
		        iodir(db);
		    break;
		    case "rtl.system.thread":
		    	thread(db, data);
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
		    	rtl(db, data);
		    break;
		    //from version 1.1
		    case "rtl.db.mysql":
		    	mysql(db);
		    break;
		    case "rtl.error":
			error(db);
		    break;
		    case "rtl.test.asset":
			asset(db);
		    break;
		    case "rtl.math.function":
			math(db);
		    break;
		    //from version 1.2
		    case "rtl.encode.base64":
			base64(db);
		    break;
		    case "rtl.script":
		    script(db, data);
		    break;
		}
	}
	
	private static void script(VariabelDatabase db, ProgramData data) throws RTLRuntimeException{
		VariableReference ref = db.get("script_getProgram");
		ref.put(new Function("script_getProgram", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new Program(data);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("script_variabelDatabase");
		ref.put(new Function("script_variabelDatabase", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new VariabelDatabase();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("script_run");
		CallableArgs arg = new CallableArgs();
		arg.add("program");
		arg.add("database");
		arg.add("string", "code");
		ref.put(new Function("script_run", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof Program))
					throw new RTLRuntimeException("script_run expect argument 1 to be Program");
					
				if(!(arg[1] instanceof VariabelDatabase))
					throw new RTLRuntimeException("script_run expect argument 2 to be VariabelDatabase");
				
				try{
					return ((Program)arg[0]).run(ProgramBuilder.build(TypeConveter.string(arg[2])), (VariabelDatabase)arg[1]);
				}catch(RTLInterprenterException e){
					throw new RTLRuntimeException(e.getMessage());
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("script_setIdentify");
		arg = new CallableArgs();
		arg.add("____db");
		arg.add("string", "name");
		arg.add("value");
		arg.add("bool", "isConst", false);
		arg.add("bool", "isGlobal", false);
		ref.put(new Function("script_setIdentify", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof VariabelDatabase))
					throw new RTLRuntimeException("script_setIdentify expect argument 1 to be VariabelDatabase");
				VariableReference rf = ((VariabelDatabase)arg[0]).get(TypeConveter.string(arg[1]));
				rf.put(arg[2]);
				int state = 0;
				if(TypeConveter.bool(arg[3]))
				  state = VariabelAttribute.NOT_WRITE;
				  
				if(TypeConveter.bool(arg[4]))
					state |= VariabelAttribute.GLOBAL;
				rf.attribute(state);
				return null;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("script_call");
		arg = new CallableArgs();
		arg.add("function", "callback");
		arg.add("program");
		arg.add("array", "args");
		ref.put(new Function("script_call", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[1] instanceof Program))
					throw new RTLRuntimeException("script_call expect argument 1 to be Program");
				Array array = TypeConveter.array(arg[2]);
				Object[] buf = new Object[array.size()];
				for(int i=0;i<buf.length;i++)
					buf[i] = array.get(i);
				return TypeConveter.toFunction(arg[0]).call((Program)arg[1], buf);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	
	private static void base64(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("base64_encode");
		CallableArgs arg = new CallableArgs();
		arg.add("context");
		ref.put(new Function("base64_encode", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(arg[0] instanceof Array){
					Array array = (Array)arg[0];
					byte[] buffer = new byte[array.size()];
					for(int i=0;i<buffer.length;i++)
						buffer[i] = TypeConveter.toByte(array.get(i));
					return new String(Base64.getEncoder().encode(buffer),  StandardCharsets.UTF_8);
				}
				return new String(Base64.getEncoder().encode(TypeConveter.string(arg[0]).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	
	private static void asset(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("asset");
		CallableArgs arg = new CallableArgs();
		arg.add("bool", "testConditon");
		arg.add("string", "errorMessage");
		ref.put(new Function("asset", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!TypeConveter.bool(arg[0])){
					throw new RTLRuntimeException(TypeConveter.string(arg[1]));
				}
				return null;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	
	private static void math(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("pow");
		CallableArgs arg = new CallableArgs();
		arg.add("number", "first");
		arg.add("number", "second");
		ref.put(new Function("pow", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return Math.pow(TypeConveter.toDouble(arg[0]), TypeConveter.toDouble(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	
	private static void error(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("error");
		CallableArgs arg = new CallableArgs();
		arg.add("string", "message");
		ref.put(new Function("error", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				throw new RTLRuntimeException(TypeConveter.string(arg[0]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void mysql(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("mysql");
		CallableArgs arg = new CallableArgs();
		arg.add("string", "host");
		arg.add("string", "username");
		arg.add("string", "password");
                arg.add("string", "table");
		ref.put(new Function("mysql", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
                                try{
					String password = TypeConveter.string(arg[2]);
					if(password.length() > 0)
						password = "&password="+password;
                                	return new MySQL(DriverManager.getConnection("jdbc:mysql://"+((String)arg[0])+"/"+((String)arg[3])+"?" +
	                                   "user="+((String)arg[1])+password+"&autoReconnect=true"));
				}catch(SQLException e){
					return null;
                                }
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("mysql_escape");
		arg = new CallableArgs();
		arg.add("string", "query");
		ref.put(new Function("mysql_escape", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).replaceAll("\\\\", "\\\\\\\\")
				.replaceAll("\n", "\\\\n")
				.replaceAll("\r", "\\\\r")
				.replaceAll("\t", "\\\\t")
				.replaceAll("\00", "\\\\0")
				.replaceAll("'", "\\\\'")
				.replaceAll("\"", "\\\\\"");
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("mysql_query");
		arg = new CallableArgs();
		arg.add("mysql_connection");
		arg.add("string", "query");
		ref.put(new Function("mysql_query", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof MySQL)){
					throw new RTLRuntimeException("mysql_query expect argument 0 to be <mysqlstream>");
				}
				
				return ((MySQL)arg[0]).query(TypeConveter.string(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("mysql_fetchHashmap");
		arg = new CallableArgs();
		arg.add("mysqlresult");
		ref.put(new Function("mysql_fetchHashmap", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof MySQLResult))
					throw new RTLRuntimeException("mysql_fetchHashmap expect argument 0 to be <mysqlresultstream>");
				return ((MySQLResult)arg[0]).fetchHashmap(db, program);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("mysql_fetch");
		ref.put(new Function("mysql_fetch", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof MySQLResult))
					throw new RTLRuntimeException("mysql_fetch expect argument 0 to be <mysqlresultstream>");
				return ((MySQLResult)arg[0]).fetch();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void rtl(VariabelDatabase db, ProgramData data) throws RTLRuntimeException{
		Struct RTL = new Struct("RTL", new String[]{
			"version",
			"test"
		});
		
		VariableReference ref = db.get("RTL");
		ref.put(RTL);
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		
		ref = db.get("rtl");
		ref.put(new Function("rtl", db, new CallableArgs(), new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				StructValue rtl = new StructValue(RTL);
				((StructReference)rtl.get("version")).put(Main.VERSION);
				((StructReference)rtl.get("test")).put(data.test);
				return rtl;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void array(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference  ref = db.get("count");
		CallableArgs arg = new CallableArgs();
		arg.add("array", "array");
		ref.put(new Function("count", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.array(arg[0]).size();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("in_array");
		arg = new CallableArgs();
		arg.add("array", "array");
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
		arg.add("string", "str");
		arg.add("string", "seperator");
		ref.put(new Function("str_split", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new Array(TypeConveter.string(arg[0]).split(TypeConveter.string(arg[1])));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("strpos");
		arg = new CallableArgs();
		arg.add("string", "str");
		arg.add("string", "find");
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
		arg.add("string", "str");
		arg.add("number", "start");
		arg.add("number", "length");
		ref.put(new Function("substr", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				String str = TypeConveter.string(arg[0]);
				int start = TypeConveter.toInt(arg[1]);
				int length = TypeConveter.toInt(arg[2]);
				try{
					if(length == 0){
						return str.substring(start);
					}
					return str.substring(start, start+length);
				}catch(IndexOutOfBoundsException e){
					return "";
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("strlen");
		arg = new CallableArgs();
		arg.add("string", "str");
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
		
		ref = db.get("strtolower");
		ref.put(new Function("strtolower", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).toLowerCase();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("strtoupper");
		ref.put(new Function("strtoupper", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.string(arg[0]).toUpperCase();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("chr");
		arg = new CallableArgs();
		arg.add("number", "number");
		ref.put(new Function("chr", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return ((char)TypeConveter.toInt(arg[0]))+"";
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);


		ref = db.get("strchar");
		arg = new CallableArgs();
		arg.add("string", "str");
		arg.add("number", "_index");
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

	private static void thread(VariabelDatabase db, ProgramData data) throws RTLRuntimeException{
		VariableReference ref = db.get("thread");
		CallableArgs arg = new CallableArgs();
		arg.add("function", "func");
		ref.put(new Function("thread", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db){
				Thread thread = new Thread(new Runnable(){
					public void run(){
						Program p = new Program(data);
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

	private static void iofile(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("file_exists");
		CallableArgs arg = new CallableArgs();
		arg.add("string", "path");
		ref.put(new Function("file_exists", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				File f = new File(TypeConveter.string(arg[0]));
				return f.exists() && f.isFile();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("file");
		arg = new CallableArgs();
		arg.add("string", "path");
		arg.add("string", "mode");
		ref.put(new Function("file", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new LocalFile(TypeConveter.string(arg[0]), TypeConveter.string(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("file_path");
		arg = new CallableArgs();
		arg.add("_file");
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
		arg.add("_file");
		ref.put(new Function("file_size", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalFile)){
					throw new RTLRuntimeException("file_size exceptct the first argument to be file stream");
				}

				return ((LocalFile)arg[0]).getSize();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("is_file");
		ref.put(new Function("is_file", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return arg[0] instanceof LocalFile;
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("file_modified");
		ref.put(new Function("file_modified", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalFile))
					throw new RTLRuntimeException("file_modified exceptct the first argument to be file stream");
					
				return ((LocalFile)arg[0]).modified();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("file_delete");
		ref.put(new Function("file_delete", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalFile))
					throw new RTLRuntimeException("file_delete exceptct the first argument to be file stream");
					
				return ((LocalFile)arg[0]).delete();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("file_readLength");
		arg = new CallableArgs();
		arg.add("_file");
		arg.add("number", "_index");
		arg.add("number", "length");
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
		arg.add("_file");
		arg.add("number", "_index");
		arg.add("number", "length");
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
		arg.add("_file");
		arg.add("string", "message");
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
		
		ref = db.get("file_setLength");
		arg = new CallableArgs();
		arg.add("_file");
		arg.add("number", "length");
		ref.put(new Function("file_setLength", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalFile)){
					throw new RTLRuntimeException("file_setLength exceptct the first argument to be file stream");
				}
				
				return ((LocalFile)arg[0]).setLength(TypeConveter.toLong(arg[1]));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}
	
	private static void iodir(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("dir");
		CallableArgs arg = new CallableArgs();
		arg.add("string", "path");
		ref.put(new Function("dir", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return new LocalDir(new File(TypeConveter.string(arg[0])));
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("dir_exists");
		arg = new CallableArgs();
		arg.add("_dir");
		ref.put(new Function("dir_exists", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalDir)){
					throw new RTLRuntimeException("dir_exists expect argument 1 to be a part of dir stream");
				}
				return ((LocalDir)arg[0]).exists();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);

		ref = db.get("dir_create");
		ref.put(new Function("dir_create", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalDir)){
					throw new RTLRuntimeException("dir_create expect argument 1 to be a part of dir stream");
				}

				return ((LocalDir)arg[0]).create();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("dir_item");
		ref.put(new Function("dir_item", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalDir)){
					throw new RTLRuntimeException("dir_next expect argument 1 to be a part of dir stream");
				}

				return ((LocalDir)arg[0]).item();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("dir_path");
		ref.put(new Function("dir_path", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(!(arg[0] instanceof LocalDir)){
					throw new RTLRuntimeException("dir_path expect argument 1 to be a part of dir stream");
				}
				return ((LocalDir)arg[0]).path();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

    private static void iotcp(VariabelDatabase db) throws RTLRuntimeException{
    	VariableReference ref = db.get("tcp");
    	CallableArgs arg = new CallableArgs();
    	arg.add("string", "server");
    	arg.add("number", "port");
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
    	arg.add("_tcp");
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
    	arg.add("_tcp");
    	arg.add("number", "length");
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
    	arg.add("_tcp");
    	arg.add("string", "message");
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
    	arg = new CallableArgs();
    	arg.add("tcpstream");
    	arg.add("message");
    	ref.put(new Function("tcp_writeBytes", db, arg, new ICallable(){
    		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
    			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_writeBytes first argument needed to be tcp socket stream");
    			}
			
			if(arg[1] instanceof Array){
			  Array array = (Array)arg[1];
			  byte[] buffer = new byte[array.size()];
			  for(int i=0;i<buffer.length;i++)
			    buffer[i] = TypeConveter.toByte(array.get(i));
			  arg[1] = buffer;
			}else if(!(arg[1] instanceof byte[])){
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
	
	ref = db.get("tcp_ready");
	ref.put(new Function("tcp_ready", db, arg, new ICallable(){
		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_ready first argument needed to be tcp socket stream");
    			}
			
			return ((TcpSocketClient)arg[0]).ready();
		}
	}));
	
	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	
	ref = db.get("tcp_readByte");
	ref.put(new Function("tcp_readByte", db, arg, new ICallable(){
		public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
			if(!(arg[0] instanceof TcpSocketClient)){
    				throw new RTLRuntimeException("tcp_readByte first argument needed to be tcp socket stream");
    			}
			
			return ((TcpSocketClient)arg[0]).readByte();
		}
	}));
	
	ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
    }

	private static void iotcpserver(VariabelDatabase db) throws RTLRuntimeException{
		VariableReference ref = db.get("tcpserver_open");
		CallableArgs arg = new CallableArgs();
		arg.add("number", "port");
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
		arg.add("string", "str");
		ref.put(new Function("toInt", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				try{
					return Integer.parseInt(TypeConveter.string(arg[0]));
				}catch(NumberFormatException e){
					try{
						return Long.parseLong(TypeConveter.string(arg[0]));
					}catch(NumberFormatException ee){
						return null;
					}
				}
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE);
		ref.attribute(VariabelAttribute.GLOBAL);
		
		//from 1.1
		ref = db.get("numberInt");
		arg = new CallableArgs();
		arg.add("number", "number");
		ref.put(new Function("numberInt", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return (int)TypeConveter.toDouble(arg[0]);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("numberDouble");
		arg = new CallableArgs();
		arg.add("number", "number");
		ref.put(new Function("numberDouble", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return TypeConveter.toDouble(arg[0]);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		//from v1.2
		ref = db.get("toByte");
		arg = new CallableArgs();
		arg.add("number", "num");
		ref.put(new Function("toByte", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				return ((Integer)TypeConveter.toInt(arg[0])).byteValue();
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
		
		ref = db.get("string");
		arg = new CallableArgs();
		arg.add("value");
		ref.put(new Function("string", db, arg, new ICallable(){
			public Object onCall(Program program, Object[] arg, VariabelDatabase db) throws RTLRuntimeException{
				if(arg[0] instanceof Array){
					Array n = (Array)arg[0];
					byte[] buffer = new byte[n.size()];
					for(int i=0;i<buffer.length;i++)
						buffer[i] = TypeConveter.toByte(n.get(i));
					return new String(buffer, StandardCharsets.UTF_8);
					//return new String(buffer);
				}
				return TypeConveter.string(arg[0]);
			}
		}));
		ref.attribute(VariabelAttribute.NOT_WRITE | VariabelAttribute.GLOBAL);
	}

	private static void rand(VariabelDatabase db) throws RTLRuntimeException{
		CallableArgs arg = new CallableArgs();
		arg.add("number", "min");
		arg.add("number", "max");
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
		arg.add("number", "time");
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
