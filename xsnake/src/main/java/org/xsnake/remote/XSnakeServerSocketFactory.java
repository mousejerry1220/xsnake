package org.xsnake.remote;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.List;

public class XSnakeServerSocketFactory implements RMIServerSocketFactory , Serializable{

	private static final long serialVersionUID = 1L;

	List<String> trustAddress = null;
	XSnakeRMIAuthentication authentication=null;
	
	public XSnakeServerSocketFactory(List<String> trustAddress,XSnakeRMIAuthentication authentication) {
		this.trustAddress = trustAddress;
		this.authentication = authentication;
	}
	
	public ServerSocket createServerSocket(int port) throws IOException {
		ServerSocket serverSocket = new XSnakeServerSocketX(port,trustAddress,authentication);
		return serverSocket;
	}


}
