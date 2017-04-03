package org.xsnake.rpc.provider.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

public class XSnakeClientSocketFactory implements RMIClientSocketFactory ,Serializable{

	private static final long serialVersionUID = 1L;

	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host, port);
	}

}
