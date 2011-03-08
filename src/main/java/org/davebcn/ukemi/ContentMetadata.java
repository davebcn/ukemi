package org.davebcn.ukemi;

import java.util.Date;

/**
 * Represents the metadata from a content on the store
 * @author dave
 *
 */
public interface ContentMetadata {

	Identifier getIdentifier();

	Date getModificationDate();
	
	long getSize();
}
