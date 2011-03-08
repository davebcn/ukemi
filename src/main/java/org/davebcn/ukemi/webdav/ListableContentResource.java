package org.davebcn.ukemi.webdav;

import java.util.Date;

import org.davebcn.ukemi.Identifier;


/**
 * Content used only for directory listings
 * @author dave
 *
 */
public class ListableContentResource extends ContentResource {

	private long size;
	private org.davebcn.ukemi.Identifier identifier;
	private Date modificationdate;
	
	public ListableContentResource(Identifier identifier, long size, Date modificationDate) {
		this.identifier = identifier;
		this.size = size;
		this.modificationdate = modificationDate;
	}
	
	public String getName() {
		return identifier.toString();
	}

	public String getUniqueId() {
		return identifier.toString();
	}
	
	@Override
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
