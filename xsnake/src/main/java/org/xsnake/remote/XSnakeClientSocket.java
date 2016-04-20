package org.xsnake.remote;

import java.io.IOException;
import java.net.Socket;

public class XSnakeClientSocket extends Socket {

	public XSnakeClientSocket(String host,int port) throws IOException{
		super(host,port);
	}
	
}
