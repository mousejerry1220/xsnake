package org.xsnake.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
 
class XSnakeServerSocket extends ServerSocket {

	List<String> trustAddress = new ArrayList<String>();
	boolean trust = false;
	
    public XSnakeServerSocket(int port,List<String> trustAddress) throws IOException {
        super(port);
        if(trustAddress!=null){
        	this.trustAddress.addAll(trustAddress);
        	this.trust = true;
        }
    }
    
    public Socket accept() throws IOException {
        Socket s = new Socket();
        implAccept(s);
//        System.out.println(s);
        String serverAddress = InetAddress.getLocalHost().getHostAddress();
        String clientAddress = s.getInetAddress().getHostAddress();
        if(trust && !serverAddress.equals(clientAddress)){
        	String socketAddress = s.getInetAddress().getHostAddress();
        	for(String address :trustAddress){
                boolean pass = Pattern.compile("^"+address).matcher(socketAddress).find();
                if(!pass){
        			s.close();
        		}
        	}
        }
        return s;
    }
    
}
