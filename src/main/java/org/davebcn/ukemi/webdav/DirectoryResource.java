package org.davebcn.ukemi.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.davebcn.ukemi.ContentMetadata;
import org.davebcn.ukemi.Identifier;
import org.davebcn.ukemi.Store;
import org.davebcn.ukemi.StoreException;
import org.davebcn.ukemi.store.ContentImpl;
import org.davebcn.ukemi.store.StringIdentifier;
import org.davebcn.ukemi.utils.StreamUtils;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;

/**
 * Represents all content inside the store, this is a directory
 * @author dave
 *
 */
public class DirectoryResource extends BrowseableContent implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource,  MoveableResource, PropFindableResource, LockingCollectionResource{

	protected String path;
	private String url;
	
	Logger logger = Logger.getLogger(DirectoryResource.class);
	
	protected DirectoryResource(){}
	
	public DirectoryResource(String path, String url) {
		super();
		this.path = path;
		this.url = url;
	}

	public CollectionResource createCollection(String subdir) {
		try {
			if (this.dirExists(subdir)){ 
				throw new RuntimeException("Directory " + subdir + " already exists");
			}else{
				StorageWebDAVEndpoint.getStore().createDirectory(new StringIdentifier(this.getDirPath(subdir)));
			}
		} catch (StoreException e) {
			throw new RuntimeException("Problems creating directory");
		}
		
		//Directory is created using it's absolute path and url
		return new DirectoryResource(this.getDirPath(subdir), getResourceURL(subdir));
	}

	public Resource child(String id) {
		ContentMetadata fetch;
		try {
			fetch = StorageWebDAVEndpoint.getStore().fetch(new StringIdentifier(this.getDirPath(id)));
		} catch (StoreException e) {
			throw new RuntimeException(e);
		}
		if (fetch == null){
			return null;
		}
		//TODO check it for directory renaming under another dir
		return new ListableContentResource(new StringIdentifier(id), fetch.getSize(), fetch.getModificationDate());
	}
	 
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
		try {
			logger.debug("Uploading resource " + newName + " at folder " + this.path);
			byte[] content = StreamUtils.getBytesFromStream(inputStream);
			
			String absolutePath = appendToPath(this.path,newName);
			
			StringIdentifier id = new StringIdentifier(absolutePath);
			
			StorageWebDAVEndpoint.getStore().put(id, new ContentImpl(content, new Date()));
			
			logger.debug("Resource " + newName + " at folder " + this.path + " uploadded");
			return new ContentResource(StorageWebDAVEndpoint.getStore().get(id),id, this.getResourceURL(newName));
		} catch (StoreException e) {
			e.printStackTrace();
			logger.fatal("Resource upload failure for resource " + newName + " at folder " + this.path);
			throw new IOException("Cannot create file, exception ");
		}
		
	}

	@Override
	public List<? extends Resource> getChildren() {
		
		List<Resource> result = new ArrayList<Resource>();
		
		//To check dirs already 
		Set<String> addedDirs = new HashSet<String>();
		List<ContentMetadata> metadata = null;
		
		try {
			metadata = metadataForPath();
		} catch (StoreException e) {
			throw new RuntimeException("Problems getting metadata", e);
		}
		
		for(ContentMetadata currentMetadata : metadata){
				String relativePath = currentMetadata.getIdentifier().toString();
				relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
				//Every directory will be added once only
				if (this.isDirectory(relativePath)){ 
					if (!addedDirs.contains(getLastTokenOnPath(relativePath).toString())){
						Identifier dirName = getLastTokenOnPath(relativePath);
						result.add(new ListableDirectory(dirName, currentMetadata.getSize(), currentMetadata.getModificationDate(), relativePath));
						addedDirs.add(getLastTokenOnPath(relativePath).toString());
					}
				}else{
					//Directory metadata is not listed
					Identifier identifier = this.getLastTokenOnPath(relativePath);
					result.add(new ListableContentResource(identifier, currentMetadata.getSize(), currentMetadata.getModificationDate()));					
				}
				 
		}

		return result;
	} 
	
	@Override
	public void moveTo(CollectionResource parent, String to) {
		String thisId = this.getUniqueId();
		Store.defaultStore.moveDirectory(thisId, ((DirectoryResource)parent).getDirPath(to));
	}

	//Get last token on given path
	private Identifier getLastTokenOnPath(String relativePath){
		String filename;
		filename = relativePath.lastIndexOf("/") >= 0 ? relativePath.substring( relativePath.lastIndexOf("/") + 1) : relativePath;
		return new StringIdentifier(filename);
	}
	
	protected String getRelativePathFromAbsolute(String absolutePath){
		if (this.path.equals("/") || path.equals("")) return absolutePath;
		if (!absolutePath.contains(this.path)) {
			throw new RuntimeException("Given file is not in path " + this.path + " file " +absolutePath);
		}
		//plus 1 to remove slash
		return absolutePath.substring(this.path.length() + 1);
	}
	
	private boolean isDirectory(String relativePath){
		return Store.defaultStore.isDirectory(new StringIdentifier(relativePath));
	}

	private List<ContentMetadata> metadataForPath() throws StoreException {
		List<ContentMetadata> metadata;
		
		if (path.equals("")) {
			 metadata = StorageWebDAVEndpoint.getStore().list();
		 }else{
			 metadata = StorageWebDAVEndpoint.getStore().list(path);
		 }
		
		return metadata;
	}
	
	public String getName() {
		return this.path.indexOf("/") >= 0 ? this.path.substring(this.path.lastIndexOf("/") + 1) : this.path;
	}

	public String getUniqueId() {
		return this.path;
	}

	public Long getContentLength() {
		return null;
	}

	//This must point to resource href!
	@Override
	protected String getHref() {   
        return this.url;
	}
	
	@Override
    public Long getMaxAgeSeconds() {
        return (long)600;
    }
	
    public String getContentType() {
        return null;
    }

	public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) {
		LockToken lockToken = new LockToken(); 
		
		lockToken.timeout = timeout;
		lockToken.info = lockInfo;
		lockToken.tokenId = name;
		
		return lockToken;
	}
	
	private boolean dirExists(String subDir) throws StoreException{
		String emptyDirFileContent = this.getDirPath(subDir); 
		return StorageWebDAVEndpoint.getStore().get(new StringIdentifier(emptyDirFileContent)) != null;
	}
	
	protected String getDirPath(String subDir){ 		
		return appendToPath(this.path,subDir) + "/";
	}
	
	private String getResourceURL(String resourceName){
		return appendToPath(this.getHref(), resourceName);
	}
	
	private String appendToPath(String route, String toBeAppended){
		String base = null;
		if (route.equals("")) base = "";
 		else base= route.endsWith("/") ? route : route + "/";
		
		return base + toBeAppended;
	}
}
