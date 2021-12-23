package rtl.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;

import rtl.Program;
import rtl.exception.*;

public class Plugin{
	private PluginContainer[] plugins = new PluginContainer[50];
	private int plength = 0;
	private boolean isInilized = false;
	private URLClassLoader loader;
	
	public boolean shouldInlize(){
		return !this.isInilized;
	}
	
	public void inilize() throws RTLException{
		try{
			File baseFile = new File(new File(Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "api");
			if(!baseFile.exists()){
				this.isInilized = true;
				return;
			}
			//wee should get all native api and append this in a list wee can controle.
			
			ArrayList<URL> list = new ArrayList<URL>();
			try{
				for(String current : baseFile.list()){
					File jarfile = new File(baseFile, current);
					Attributes attri = this.getManifest(jarfile);
					String isplugin = attri.getValue("RTLPLUGIN");
					if(isplugin == null || !isplugin.equals("true"))
						continue;
				  
					list.add(jarfile.toURI().toURL());
				
					String cp = attri.getValue("Class-Path");
					if(cp != null){
						for(String p : cp.split(";")){
							boolean isJar = p.indexOf("jar/") == 0;
							File ccp = new File(isJar ?  new File(Program.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile() : baseFile, p+".jar");
							if(!ccp.exists())
								throw new RTLRuntimeException("Failed to load native sub plugin: "+p+".jar");
							if(isJar)
								list.add(ccp.toURI().toURL());
						}
					}
			    
					String p = current.substring(0, current.indexOf("."));
					this.plugins[this.plength] = new PluginContainer(p+"."+attri.getValue("file"), attri.getValue("include-path"));
					this.plength++;
				}
			}catch(Exception e){
				throw new RTLRuntimeException("Failed to load plugin reason: "+e.getMessage());
			}
			this.loader = new URLClassLoader(list.toArray(new URL[0]), this.getClass().getClassLoader());
			this.isInilized = true;
		}catch(URISyntaxException e){
			throw new RTLRuntimeException("Failed to containe a list of native api");
		}
	}
	
	public IPlugin getPlugin(String path) throws RTLRuntimeException{
		for(int i=0;i<this.plength;i++){
			PluginContainer plugin = this.plugins[i];
			if(plugin.getIncludePath().equals(path)){
				if(!plugin.isLoaded())
					plugin.load(this.loader);
				return plugin.getPlugin();
			}
		}
		return null;
	}
	
	public boolean isFinish(){
		for(int i=0;i<this.plength;i++){
			if(!this.plugins[i].isFinish())
				return false;
		}
		return true;
	}

    public void close(){
		for(int i=0;i<this.plength;i++)
		  this.plugins[i].close();
	}
	
	private Attributes getManifest(File path) throws IOException{
		    JarInputStream jarStream = new JarInputStream(new FileInputStream(path));
		    Manifest mf = jarStream.getManifest();
		    return mf.getMainAttributes();
	}
	
	private URL[] addUrl(URL[] url, URL current){
		URL[] buffer = new URL[url.length+1];
		for(int i=0;i<url.length;i++){
			buffer[i] = url[i];
		}
		buffer[buffer.length-1] = current;
		return buffer;
	}
}
