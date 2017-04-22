package org.xsnake.rpc.rest;

import java.io.IOException;
import java.net.ServerSocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.Compression;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestServer {

	static RestServer restServer;
	
	public RestServer(){
		restServer = this;
	}
	
	int port;
	
	public static void run(){
		SpringApplication.run(RestServer.class);
	}
	
	public static RestServer getRestServer() {
		return restServer;
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer(){
		port = getPort(12345);
		AbstractEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
		factory.setPort(port);
		Compression c = new Compression();
		c.setEnabled(true);
		c.setMimeTypes(new String[]{"text/json","text/html","text/xml","text/javascript","text/css","text/plain"});
		c.setMinResponseSize(256);
		factory.setCompression(c);
		return factory;
	}
	
	protected int getPort(int port) {
		ServerSocket ss = null;
		try{
			 ss = new ServerSocket(port);
		}catch(Exception e){
			port = port + 1;
			return getPort(port);
		}finally{
			try {
				if(ss!=null){
					ss.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return port;
	}

	public int getPort() {
		return port;
	}
	
}
