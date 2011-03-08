package org.davebcn.ukemi.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.davebcn.ukemi.Content;
import org.davebcn.ukemi.ContentMetadata;
import org.davebcn.ukemi.Identifier;
import org.davebcn.ukemi.Store;
import org.davebcn.ukemi.StoreException;
import org.davebcn.ukemi.crypto.ContentCipher;
import org.davebcn.ukemi.utils.StreamUtils;

/**
 * Store implementation over filesystem
 * @author dave
 *
 */
public class FileStore implements Store{
	
	private String base;
	private String password;

	private ContentCipher cipher;
	
	public FileStore(String base, String password){
		this();
		this.base = base;
		this.password = password;
	}
	
	public String getBase() {
		if (base == null){
			throw new RuntimeException("File store must be initialized to a base directory!");
		}
		return base;
	}
	
	public void setBase(String base) {
		this.base = base;
	}
	
	public void setPassword(String p){
		this.password = p;
	}

	public FileStore(){		
	}

	
	public void delete(Identifier id) throws StoreException {
		resolveFile(id).delete();
	}

	
	public Content get(Identifier id) throws StoreException {
		try {
			File resolveFile = resolveFile(id);
			FileInputStream content = new FileInputStream(resolveFile);
			byte[] encipheredContent = StreamUtils.getBytesFromStream(content);
			byte[] decipheredcontent = this.getCipher().decipher(encipheredContent);
			return new ContentImpl(decipheredcontent, new Date(resolveFile.lastModified()));
		} catch (FileNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw new StoreException(e);
		} 	
	}

	public List<ContentMetadata> list() throws StoreException {
		return internalList(this.getBase(),"");
	}

	public List<ContentMetadata> list(String prefix) throws StoreException {
		return internalList(this.getBase() + File.separator + prefix, prefix);
	}
	
	private List<ContentMetadata> internalList(String path, String relativePath){
		File file = new File(path);
		String[] list = file.list();
		
		List<ContentMetadata> result = new ArrayList<ContentMetadata>();
		
		for(String current : list){
			String absoluteFile = path + File.separator + current;
			File currentFile = new File(absoluteFile);
			if (!currentFile.exists()) throw new RuntimeException("File " + absoluteFile + " does not exist");
			ContentMetadata meta = new ContentMetadataImpl(new Date(currentFile.lastModified()), 
					currentFile.getTotalSpace(), 
					new StringIdentifier(relativePath + File.separator + current));
			result.add(meta); 
		}
		
		return result;
	}

	public void put(Identifier id, Content content) throws StoreException {
		try{
			File resolvedFile = resolveFile(id);
			FileOutputStream fos = new FileOutputStream(resolvedFile);
			byte[] decipheredContent = content.asArray();
			byte[] encipheredContent = this.getCipher().encipher(decipheredContent);
			fos.write(encipheredContent);
			fos.flush();
			fos.close();			
		}catch (IOException e) {
			// Problems with underlying file
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	private ContentCipher getCipher() {
		if (this.cipher == null){
			try {
				this.cipher = new ContentCipher("PBEWithMD5AndDES", this.password);
			} catch (Exception e) {
				//noop
			}
		}
		return this.cipher;
	}

	private File resolveFile(Identifier id){
		return new File(this.getBase() + "/" + id.toString());
	}
	
	private File resolveFile(String id){
		return new File(this.getBase() + "/" + id);
	}

	public void createDirectory(Identifier dirPath) {
		File f = resolveFile(dirPath);
		f.mkdir();
	}

	public boolean isDirectory(Identifier relativePath) {
		File f = resolveFile(relativePath);
		return f.isDirectory();
	}


	public ContentMetadata fetch(Identifier id) throws StoreException {
		File resolvedFile = resolveFile(id);
		
		if (!resolvedFile.exists()) return null;
		
		ContentMetadata meta = new ContentMetadataImpl(new Date(resolvedFile.lastModified()), 
				resolvedFile.getTotalSpace(), 
				id);
		return meta;
	}


	public void move(String path, String to) throws StoreException {
		File f = resolveFile(path);
		if (!f.exists()) throw new RuntimeException("File with path " + path + " doesn't exist");
		File destFile = new File(f.getParentFile().getAbsolutePath() + File.separator + to.toString());
		try {
			destFile.createNewFile();
			copyFile(f, destFile);
			f.delete();
		} catch (IOException e) {
			throw new StoreException("Cannot copy file");
		}	
	}
	
	public void moveDirectory(String source, String to) {
		File thisDir = resolveFile(source);
		File destDir = resolveFile(to);
		
		thisDir.renameTo(destDir);
	}
	
	private void copyFile(File sourceFile, File destFile) throws IOException {
		 if(!destFile.exists()) {
		  destFile.createNewFile();
		 }

		 FileChannel source = null;
		 FileChannel destination = null;
		 try {
		  source = new FileInputStream(sourceFile).getChannel();
		  destination = new FileOutputStream(destFile).getChannel();
		  destination.transferFrom(source, 0, source.size());
		 }
		 finally {
		  if(source != null) {
		   source.close();
		  }
		  if(destination != null) {
		   destination.close();
		  }
		}
	}


	
}
