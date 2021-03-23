package rtl.local;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.IOException;

import rtl.exception.RTLRuntimeException;

public class TcpSocketClient {
	private Socket socket;
	private BufferedReader reader;
	private OutputStream writer;

	public TcpSocketClient(Socket socket) throws RTLRuntimeException{
		this.socket = socket;
		try{
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.writer = socket.getOutputStream();
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String readLine() throws RTLRuntimeException{
		try{
			return this.reader.readLine();
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String read() throws RTLRuntimeException{
		try{
			int c = this.reader.read();
		  	return c == -1 ? null : ((char)c)+"";
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String readLength(int length) throws RTLRuntimeException{
		char[] buffer = new char[length];
		try{
			this.reader.read(buffer, 0, length);
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
		return new String(buffer);
	}

	public void writeln(String message) throws RTLRuntimeException{
		this.write(message+"\r\n");
	}

	public void write(String message) throws RTLRuntimeException{
		try{
			this.writer.write(message.getBytes());
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public void writeBytes(byte[] buffer) throws RTLRuntimeException{
		try{
			this.writer.write(buffer, 0, buffer.length);
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public void flush() throws RTLRuntimeException{
		try{
			this.writer.flush();
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String ip(){
		return this.socket.getInetAddress().getHostAddress();
	}

	public void close() throws RTLRuntimeException{
		try{
			this.writer.close();
			this.reader.close();
			this.socket.close();
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}
}
