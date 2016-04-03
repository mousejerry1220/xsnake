package org.xsnake.remote;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;
import java.util.List;

public class XSnakeSocketFactory extends RMISocketFactory implements Serializable{

	private static final long serialVersionUID = 1L;

	List<String> trustAddress = null;
	public XSnakeSocketFactory(List<String> trustAddress) {
		this.trustAddress = trustAddress;
	}

	public XSnakeSocketFactory() {}
	
	public ServerSocket createServerSocket(int port) throws IOException {
			return new XSnakeServerSocket(port,trustAddress);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host,port);
	}
}
