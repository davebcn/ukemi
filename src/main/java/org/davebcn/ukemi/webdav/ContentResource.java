package org.davebcn.ukemi.webdav;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.davebcn.ukemi.Content;
import org.davebcn.ukemi.Identifier;
import org.davebcn.ukemi.StoreException;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;

/**
 * WebDAV wrapper over the content
 * @author dave
 *
 */
public class ContentResource extends BrowseableContent implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource {
	 
	Logger logger = Logger.getLogger(ContentResource.class); 

	private Content content;
	protected Identifier identifier;
	private String url;
	
	protected ContentResource(){}
	
	public ContentResource(Content content, Identifier identifier, String url) {
		super();
		this.content = content;
		this.identifier = identifier;
		this.url = url;
	} 

	public String getName() {
		String []tokens = this.identifier.toString().split("/");
		return tokens[tokens.length-1];
	}

	public String getUniqueId() {
		return identifier.toString();
	}

	public int compareTo(Resource o) {
		if (o.getUniqueId().equals(this.getUniqueId())) return 0;
		else return -1;
	}

	public Long getContentLength() {
		if (this.content == null) throw new RuntimeException("Content with id " + this.identifier + " is null ");
		return (long)this.content.asArray().length;
	}

	public void sendContent(OutputStream output, Range range, Map<String, String> arg2, String method) throws IOException {
		int start, end;
		byte[] content = this.content.asArray();
		if (range != null){
			logger.debug("Sending content , range " + range.getStart() + " to " + range.getFinish());
			start = (int)range.getStart();
			end = (int)range.getFinish();
		}else{
			start = 0;
			end = content.length;
		}
		output.write(content, start, end - start);
		output.flush();
	}
	
	@Override
	public void moveTo(CollectionResource arg0, String to) {
		System.out.println("Moving " + this.getUniqueId() + " to " + to);
		
		try {
			StorageWebDAVEndpoint.getStore().move(this.getUniqueId(), to);
		} catch (StoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void delete() {
		try {
			StorageWebDAVEndpoint.getStore().delete(this.identifier);
		} catch (StoreException e) {
			throw new RuntimeException("Cannot delete content with id " + this.identifier, e);
		}
	}

	@Override
	protected String getHref() {
		 return this.url;
    }	
	
	public String getContentType(){
		return "application/binary";
	}

	public Long getMaxAgeSeconds(Auth arg0) {
		return null;
	}
	
	@Override
	public Date getModifiedDate() {
		return this.content.getLastModified();
	}
}
