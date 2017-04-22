package org.xsnake.rpc.provider.rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public class XSnakeServerSocketFactory implements RMIServerSocketFactory{
	
	public ServerSocket createServerSocket(int port) throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		return serverSocket;
	}

}
