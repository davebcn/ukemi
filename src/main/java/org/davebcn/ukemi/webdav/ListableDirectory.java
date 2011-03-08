package org.davebcn.ukemi.webdav;

import java.util.Date;

import org.davebcn.ukemi.Identifier;

/**
 * Listing view of a directory
 * @author dave
 *
 */
public class ListableDirectory extends DirectoryResource {
	private long size;
	private Identifier identifier;
	private Date modificationdate; 
	
	public ListableDirectory(Identifier identifier, long size, Date modificationDate, String relativePath) {
		this.identifier = identifier;
		this.size = size;
		this.modificationdate = modificationDate;
		this.path = relativePath;
	}
	
	public String getName() {
		return identifier.toString();
	}

	public String getUniqueId() {
		return identifier.toString();
	}
	
	public Long getContentLength() {
		return this.size;
	}
	
	@Override
	public Date getCreateDate() {
		return this.modificationdate;
	}
	
	@Override
	public Date getModifiedDate() {
		return this.modificationdate;
	}

}
