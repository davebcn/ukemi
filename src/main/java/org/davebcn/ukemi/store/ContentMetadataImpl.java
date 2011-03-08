package org.davebcn.ukemi.store;

import java.util.Date;

import org.davebcn.ukemi.ContentMetadata;
import org.davebcn.ukemi.Identifier;


/**
 * Java bean impl of metadata
 * @author dave
 *
 */
public class ContentMetadataImpl implements ContentMetadata {

	private Date modificationDate = null;
	private long size;
	private Identifier identifier;
	
	public ContentMetadataImpl(Date modificationDate, Long size, Identifier identifier) {
		super();
		this.modificationDate = modificationDate;
		this.size = size;
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return this.identifier;
	}

	public Date getModificationDate() {
		return this.modificationDate;
	}

	public long getSize() {
		return this.size;
	}

}
