package rtl.plugin;

import rtl.exception.RTLRuntimeException;

import java.net.URLClassLoader;

class PluginContainer{
	private String nativePath;
	private String includePath;
	private boolean isLoaded = false;
	private IPlugin plugin;
	
	public PluginContainer(String nativePath, String includePath){
		this.nativePath = nativePath;
		this.includePath = includePath;
	}
	
	public String getIncludePath(){
		return this.includePath;
	}
	
	public boolean isLoaded(){
		return this.isLoaded;
	}
	
	public void load(URLClassLoader loader) throws RTLRuntimeException{
		try{
			Class plugin = Class.forName(this.nativePath, true, loader);
			if(IPlugin.class.isAssignableFrom(plugin)){
				this.plugin = (IPlugin)plugin.newInstance();
				this.isLoaded = true;
			}
		 }catch(Exception e){
			 throw new RTLRuntimeException("Failed to load "+this.includePath+" becuse "+e.getMessage());
		 }
	}
	
	public IPlugin getPlugin(){
		return this.plugin;
	}
	
	public boolean isFinish(){
		if(this.isLoaded)
			return this.plugin.isFinish();
		return true;
	}
	
	public void close(){
		if(this.isLoaded)
			this.plugin.close();
	}
}
