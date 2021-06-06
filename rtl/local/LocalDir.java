package rtl.local;

import rtl.Array;
import rtl.exception.RTLRuntimeException;

import java.io.File;

public class LocalDir{
	private File current;
	
	public LocalDir(File current){
		this.current = current;
	}
	
	public boolean exists(){
		return this.current.exists() && this.current.isDirectory();
	}
	
	public boolean create(){
		if(this.current.exists() && this.current.isDirectory())
			return false;
		return this.current.mkdir();
	}
	
	public String path(){
		return this.current.getAbsolutePath();
	}
	
	public Array item() throws RTLRuntimeException{
		if(!this.exists())
			return null;
			
		Array array = new Array();
		String[] item = this.current.list();
		for(int i=0;i<item.length;i++){
			String path = this.current.getAbsoluteFile()+"/"+item[i];
			File f = new File(path);
			if(f.isFile()){
				array.add(new LocalFile(path, "rw"));
			}else{
				array.add(new LocalDir(f));
			}
		}
		return array;
	}
}
