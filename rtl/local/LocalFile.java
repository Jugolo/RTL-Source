package rtl.local;

import rtl.exception.RTLRuntimeException;

import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalFile {
	private RandomAccessFile file;
	private String path;
	
	public LocalFile(String path, String mode) throws RTLRuntimeException{
		this.path = path;
		try{
			this.file = new RandomAccessFile(path, mode);
		}catch(FileNotFoundException | IllegalArgumentException e){
			throw new RTLRuntimeException(e.getMessage());
		}
	}

	public String getPath(){
		return this.path;
	}

	public int getSize(){
		try{
			return (int)this.file.length();
		}catch(IOException e){
			return -1;
		}
	}

	public String readLength(int index, int length) throws RTLRuntimeException{
		return new String(this.readBytes(index, length));
	}

	public byte[] readBytes(int index, int length) throws RTLRuntimeException{
		byte[] buffer = new byte[length];
		try{
			this.file.read(buffer, index, length);
		}catch(IOException e){
			throw new RTLRuntimeException(e.getMessage());
		}
		return buffer;
	}

	public void write(String message) throws RTLRuntimeException{
        try{
			this.file.write(message.getBytes(), 0, message.length());
        }catch(IOException e){
        	throw new RTLRuntimeException(e.getMessage());
        }
	}
}
