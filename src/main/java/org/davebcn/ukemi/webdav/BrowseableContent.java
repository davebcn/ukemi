package org.davebcn.ukemi.webdav;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.LockInfo.LockDepth;
import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;


/**
 * Common methods defined for files and directories
 * @author dave
 *
 */
public abstract class BrowseableContent implements Resource, MoveableResource, CopyableResource, LockableResource{
	
	static Map<String,LockToken> locks = new HashMap<String,LockToken>();
	
    //TODO No auth implemented
	public Object authenticate(String arg0, String arg1) {
		return true;
	}

	public String getContentType(String accept) {
        return Response.ContentType.HTTP.toString();
    }
	
	//TODO No auth implemented
	public boolean authorise(Request arg0, Request.Method arg1, Auth arg2) {
		return true;
	}

	public String checkRedirect(Request arg0) {
		return null;
	}

	public Date getModifiedDate() {
		return new Date();
	}

	public String getRealm() {
		return "Tractis content storage";
	}
	
	public void copyTo(CollectionResource arg0, String arg1) {
		System.out.println("COPY not implemented");
		throw new RuntimeException("Method not implemented");
	}

	public void delete() {
		System.out.println("DELETE not implemented");
		throw new RuntimeException("Method not implemented");
	}

    //TODO determine a max age
	public Long getMaxAgeSeconds() {
		return (long)1;
	}
	
	public void moveTo(CollectionResource arg0, String to) {
		throw new RuntimeException("Method not implemented");
	}

	public String processForm(Map<String, String> arg0, Map<String, FileItem> arg1) {
		System.out.println("UPLOAD not implemented");
		throw new RuntimeException("Method not implemented");
	}

	//TODO better way to get creation date
	public Date getCreateDate() {
		return new Date();
	}
	
	protected abstract String getHref();
	
	public abstract String getName();
    
    public List<? extends Resource> getChildren() {
    	System.out.println("CHILDREN not implemented");
    	return Collections.emptyList();
    }

    public int compareTo(Resource o) {
        if( o instanceof BrowseableContent ) {
        	BrowseableContent res = (BrowseableContent)o;
            return this.getName().compareTo(res.getName());
        } else {
            return -1;
        } 
    }

	public void unlock(String tokenId) {
		locks.remove(tokenId);
	}
	
	   public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
		   
		   
	    	LockToken token = new LockToken();
	    	token.timeout = timeout;
	    	token.info = lockInfo;
	    	token.tokenId = this.getName();
	    	
	    	locks.put(this.getUniqueId(), token);
	    	
			return new LockResult(null, token);
		}

		public LockResult refreshLock(String tokenId) {
			
			LockToken token = locks.get(tokenId);
			token.timeout = LockTimeout.parseTimeout("120");
			
			return new LockResult(null,token);
			
			//120 secs more
			/*LockTimeout timeout = LockTimeout.parseTimeout("120");
			LockInfo info = new LockInfo(LockScope.NONE, LockType.WRITE, "none", LockDepth.INFINITY);
			
			LockToken token = new LockToken();
	    	token.timeout =  timeout;
	    	token.info = info;
	    	token.tokenId = this.getName();
	    	
			return new LockResult(null,token);*/
		}
	
	public String toString(){
		return "Resource class:"+this.getClass().getName()+" Resource name:"+this.getName();
	}
	
	public LockToken getCurrentLock() {
		String id = this.getUniqueId();
		return locks.get(id);
	}
}
