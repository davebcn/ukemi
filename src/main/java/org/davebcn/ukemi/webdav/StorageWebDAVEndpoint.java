package org.davebcn.ukemi.webdav;

import org.apache.log4j.Logger;
import org.davebcn.ukemi.Content;
import org.davebcn.ukemi.Identifier;
import org.davebcn.ukemi.Store;
import org.davebcn.ukemi.StoreException;
import org.davebcn.ukemi.store.StringIdentifier;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV endpoint for service storage
 * @author dave
 *
 */
public class StorageWebDAVEndpoint implements ResourceFactory{

	static Logger logger = Logger.getLogger(StorageWebDAVEndpoint.class);
	 
 
	public Resource getResource(String host, String urlString) {
		 
		long start = System.currentTimeMillis();
		try{
			
			
			System.out.println("Requested url " +  urlString);
			
			String relativePath = getPath(urlString);
			Resource res = this.resolveResource(relativePath, urlString);
			
			if (res == null) logger.info("Resource not found");
			else{
				System.out.println("Resource found: " + res.getName());
			}
			
			return res;
		}catch (Exception e) {
			e.printStackTrace();
			logger.fatal("Problems recovering resource " + urlString);
			throw new RuntimeException("Problems recovering resource ", e);
		}finally{
			long end = System.currentTimeMillis();
			logger.info("Request " + urlString + " elapsed " + (end - start));
		}
	}
	
	/**
	 * Resolves path from given url
	 * @param URL
	 * @return
	 */
	private String getPath(String URL){
		
		String pathExp = "/";
		String relativePath = URL.substring(URL.indexOf(pathExp) + pathExp.length());
		//first slash is avoided
		if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
		
		return relativePath;
	}
	
	private Resource resolveResource(String path, String url) throws StoreException{
		//Root dir
		if (path.equals("")) return new DirectoryResource("", url);
		//A directory
		else if (path.endsWith("/")){				
			return getDirectory(path, url);
		}else{
			Resource res = this.getFile(path, url);
			if (res == null){
				//Check if directory is provided but url is not ending on slash
				res = this.getDirectory(path + "/", url);			
			}
			return res;
		}
	}
	
	private ContentResource getFile(String absolutePath, String url) throws StoreException{
		Identifier id = new StringIdentifier(absolutePath);
		Content content = getStore().get(id);
		//Content is a file
		return content == null ? null : new ContentResource(content, id, url);
	}
	 
	/**
	 * Recovers the directory metadata if present
	 * @param path
	 * @return
	 * @throws StoreException
	 */
	private DirectoryResource getDirectory(String path, String url) throws StoreException{
		if (!path.endsWith("/")) throw new RuntimeException("Invalid path provied, expected one MUST end with /");
		
		Identifier dirId = new StringIdentifier(path); 
		
		
		boolean isDir = Store.defaultStore.isDirectory(dirId);
		
		//Last path character is avoided because it's expected to be ALWAYS a slash char
		return isDir ? new DirectoryResource(path.substring(0,path.length() - 1), url) : null;
	}
	
	protected static Store getStore(){
		return Store.defaultStore;
	}
	
	public String getSupportedLevels() {
		return "1,2";
	}

}
