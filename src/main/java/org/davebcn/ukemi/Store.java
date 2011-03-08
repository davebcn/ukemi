package org.davebcn.ukemi;

import java.util.List;

import org.davebcn.ukemi.store.FileStore;

/**
 * Basic interface for stores implementing basic put/get/delete/list operations
 * @author dave
 *
 */
public interface Store {

	public Content get(Identifier id)throws StoreException;
	
	public void put(Identifier id, Content content) throws StoreException;
	
	public void delete(Identifier id) throws StoreException;
	
	public ContentMetadata fetch(Identifier id)throws StoreException;
	
	public void move(String path, String to) throws StoreException;
	
	public List<ContentMetadata> list()throws StoreException;
	
	public List<ContentMetadata> list(String prefix)throws StoreException;
	
	static Store defaultStore = new FileStore();

	public void createDirectory(Identifier dirPath);

	public boolean isDirectory(Identifier relativePath);
	
	public void moveDirectory(String thisId, String to);
}
