package org.davebcn.ukemi;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.davebcn.ukemi.store.FileStore;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.bradmcevoy.http.MiltonServlet;

/**
 * Launches webdav server 
 * @author dave
 *
 */
public class UkemiServer {
   private static final int LISTEN_PORT = 8080;
   private static final int PROBE_PORT = 8081;
   
	static Logger logger = Logger.getLogger(UkemiServer.class);

   
   public static final void main(String[] args) throws Exception {
	   if (args.length != 2){
		   logger.error("Usage ./start.sh baseDir password");
		   return;
	   }
	   
	   ((FileStore)Store.defaultStore).setBase(args[0]);
	   ((FileStore)Store.defaultStore).setPassword(args[1]);

	   new UkemiServer().start(LISTEN_PORT);
   }
   
   public void startProbe() throws Exception{
	   start(PROBE_PORT);
	   new LoggerServer().start();
   }
   
   public void start(int port) throws Exception{
	   Server server = new Server(port);
	   
	   Context root = new Context(server,"/",Context.SESSIONS);
	   MiltonServlet servlet = new com.bradmcevoy.http.MiltonServlet();
	  
	   ServletHolder servletHolder = new ServletHolder(servlet);
	   servletHolder.setInitParameter("resource.factory.class", "org.davebcn.ukemi.webdav.StorageWebDAVEndpoint");
	   root.addServlet(servletHolder, "/");
	   
	   logger.info("Starting UKEMI server at http://localhost:" + LISTEN_PORT);
	   logger.info("Base dir is " + ((FileStore)Store.defaultStore).getBase());
	   
	   server.start();   
   }
   
}

class LoggerServer{
	   private static final int LISTEN_PORT = 8080;
	   private ServerSocket ss;
	   
	   public LoggerServer() throws IOException{
		   this.ss = new ServerSocket(LISTEN_PORT);
	   }
	   
	   public void start() throws IOException{
		   while(true){
			   Socket client = this.ss.accept();
			   
			   InputStream inputStream = client.getInputStream();
			   OutputStream outputStream = client.getOutputStream();
			   
			   Socket serverConnec = new Socket("localhost", 8081);
			   InputStream serverInputStream = serverConnec.getInputStream();
			   OutputStream serverOutputStream = serverConnec.getOutputStream();
			   
			   new Thread(new Worker(inputStream, serverOutputStream)).start();
			   new Thread(new Worker(serverInputStream, outputStream)).start();
			   

		   }
	   }
	   
	   class Worker implements Runnable{
		   private InputStream inputStream;
		   private OutputStream outputStream; 
		   
			public Worker(InputStream is, OutputStream os) {
			super();
			this.inputStream = is;
			this.outputStream = os;
		}

			public void run() {
				try{
					 int read;
				     byte[] buff = new byte[1024];

					 while((read = inputStream.read(buff)) > 0){
						   System.out.write(buff, 0, read);
						   outputStream.write(buff, 0, read);
					 }
					 
					 inputStream.close();
					 outputStream.close();
				}catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		   
	   }
}
