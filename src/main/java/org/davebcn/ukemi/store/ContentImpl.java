package org.davebcn.ukemi.store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import org.davebcn.ukemi.Content;
import org.davebcn.ukemi.utils.StreamUtils;

/**
 * Basic implementation using byte arrays to hold data
 * @author dave
 *
 */
public class ContentImpl implements Content{

	private static final long serialVersionUID = -3105804819756797495L;
	
	private byte[] content;
	private Date lastModified;
	
	public Date getLastModified() {
		return lastModified;
	}

	public ContentImpl(byte[] content, Date lastModified){
		this.content = content;
		this.lastModified = lastModified;
	}
	
	public ContentImpl(InputStream content, Date lastModified){
		try {
			this.content = StreamUtils.getBytesFromStream(content);
			this.lastModified = lastModified;
		} catch (IOException e) {
			throw new RuntimeException("Cannot instantiate content");
		}
	}

	public byte[] asArray() {
		byte[] dest = new byte[this.content.length];
		System.arraycopy(this.content, 0, dest, 0, this.content.length);
		
		return dest;
	}

	public InputStream asStream() {
		return new ByteArrayInputStream(this.asArray());
	}
	
	public boolean equals(Object o){
		return o instanceof Content && Arrays.equals(((Content)o).asArray(), this.content);
	}
	
	/**
	 * returns a copy of the current object, used before put on many layers
	 */
	public Content clone(){
		byte[] copy = new byte[this.content.length];
		System.arraycopy(this.content, 0, copy, 0, this.content.length);
		return new ContentImpl(copy, this.lastModified);
	}
}
