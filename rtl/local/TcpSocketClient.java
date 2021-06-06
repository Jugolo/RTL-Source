package rtl.local;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import rtl.exception.RTLRuntimeException;

public class TcpSocketClient {
	private Socket socket;
	private InputStream reader;
	private OutputStream writer;
	public String errmsg = "<noerror>";

	public TcpSocketClient(Socket socket) throws RTLRuntimeException{
		this.socket = socket;
		try{
			this.reader = socket.getInputStream();
			this.writer = socket.getOutputStream();
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String readLine() throws RTLRuntimeException{
		try{
			String line = "";
			int c;
			while((c = this.reader.read()) != -1){
				if(c == '\r')
					continue;
				if(c == '\n')
					return line;
				line += (char)c;
			}
			return line;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return null;
		}
	}

	public String read() throws RTLRuntimeException{
		try{
			int c = this.reader.read();
		  	return c == -1 ? null : ((char)c)+"";
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return null;
		}
	}

	public String readLength(int length) throws RTLRuntimeException{
		byte[] buffer = new byte[length];
		try{
			this.reader.read(buffer, 0, length);
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return null;
		}
		return new String(buffer, StandardCharsets.UTF_8);
	}

	public boolean writeln(String message) throws RTLRuntimeException{
		return this.write(message+"\r\n");
	}

	public boolean write(String message) throws RTLRuntimeException{
		try{
			this.writer.write(message.getBytes());
			return true;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return false;
		}
	}

	public boolean writeBytes(byte[] buffer) throws RTLRuntimeException{
		try{
			this.writer.write(buffer, 0, buffer.length);
			return true;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return false;
		}
	}

	public boolean flush() throws RTLRuntimeException{
		try{
			this.writer.flush();
			return true;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return false;
		}
	}

	public String ip(){
		return this.socket.getInetAddress().getHostAddress();
	}

	public boolean close() throws RTLRuntimeException{
		try{
			this.writer.close();
			this.reader.close();
			this.socket.close();
			return true;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return false;
		}
	}
	
	public boolean ready(){
		try{
			return this.reader.available() > 0;
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return false;
		}
	}
	
	public byte readByte(){
		try{
			byte[] b = new byte[1];
			this.reader.read(b);
			return b[0];
		}catch(IOException e){
			this.errmsg = e.getMessage();
			return 0;
		}
	}
}
