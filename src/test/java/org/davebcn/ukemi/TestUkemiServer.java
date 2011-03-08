package org.davebcn.ukemi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.davebcn.ukemi.store.FileStore;
import org.davebcn.ukemi.utils.StreamUtils;

public class TestUkemiServer extends TestCase{

	static UkemiServer server;
	private HttpClient client ;
	private String baseUrl  = "http://localhost:8081/";
	
	public void setUp(){
		try {
			if (server == null){
				server = new UkemiServer();
				((FileStore)Store.defaultStore).setBase("/tmp/");
				((FileStore)Store.defaultStore).setPassword("changeme");
				server.start(8081);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client = new HttpClient();
	}
	
	public void testBasicRequest() throws Exception{
		havingEmptyRepo();
		find("");
		Collection<String> collection = find("");
		assertTrue(Arrays.equals(new String[]{"http://localhost:8081/"},collection.toArray()));
	}
	
	public void testBasicRepoWithOneFile() throws Exception{
		havingEmptyRepo();
		havingOneFile("boo.txt");
		Collection<String> collection = find("");
		assertTrue(Arrays.equals(new String[]{"http://localhost:8081/", "http://localhost:8081/boo.txt"},collection.toArray()));
	}
	
	public void testPutFileCreatesEncryptedFile() throws Exception{
		havingEmptyRepo();
		String content = "DummyContent";
		
		File f = havingOneDummyFile(content);
		this.put("testing.txt", f);
		
		Collection<String> collection = find("");
		assertTrue(Arrays.equals(new String[]{"http://localhost:8081/", "http://localhost:8081/"+f.getName()},collection.toArray()));
		
		//Now ensure recovered data matches
		byte[] body = get("http://localhost:8081/"+f.getName());
		assertTrue(Arrays.equals(content.getBytes(), body));
		
		//Check file on filesystem is encrypted
		byte[] discContent = StreamUtils.getBytesFromFile(((FileStore)Store.defaultStore).getBase() + File.separator + f.getName());
		assertFalse(Arrays.equals(content.getBytes(), discContent));
	}
	
	private File havingOneDummyFile(String content) throws Exception {
		File f = File.createTempFile("lalala", "temporal");
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(content.getBytes());
		fos.close();
		return f;
	}

	private void havingOneFile(String name) throws IOException {
		File f = new File(((FileStore)Store.defaultStore).getBase() + File.separator + name);
		f.createNewFile();
	}

	private void havingEmptyRepo() {
		File f = new File("/tmp/" + UUID.randomUUID().toString());
		f.mkdir();
		((FileStore)Store.defaultStore).setBase(f.getAbsolutePath());
	}

	private void put(String path, File f){
		try{
		    PutMethod method = new PutMethod(baseUrl + "/" + f.getName());
		    RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(f));
		    method.setRequestEntity(requestEntity);
		    client.executeMethod(method);
		    System.out.println(method.getStatusCode() + " " + method.getStatusText());
		}
		catch(HttpException ex){
		    // Handle Exception
		}
		catch(IOException ex){
		    // Handle Exception
		}
	}
	
	private byte[] get(String uri)throws Exception{
		GetMethod method = new GetMethod(uri);
	    client.executeMethod(method);

	    return method.getResponseBody();
	}
	
	private Collection<String> find(String path) throws DavException{
		try{
		    PropFindMethod method = new PropFindMethod(baseUrl + path);
		    client.executeMethod(method);
		    System.out.println("[HttpStatus:]" + method.getStatusCode());
		    
		    MultiStatus responseBodyAsMultiStatus = method.getResponseBodyAsMultiStatus();
		    
		    Collection<String> res = new ArrayList<String>();
		    
		    for (int i = 0; i < responseBodyAsMultiStatus.getResponses().length; i++) {
	            MultiStatusResponse multiRes = responseBodyAsMultiStatus.getResponses()[i];
	            res.add(multiRes.getHref());
	        }
		    
		    return res;
		}
		catch(HttpException ex){
		    // Handle Exception
			throw new RuntimeException(ex);
		}
		catch(IOException ex){
		    // Handle Exception
			throw new RuntimeException(ex);
		}
	}
	
}
