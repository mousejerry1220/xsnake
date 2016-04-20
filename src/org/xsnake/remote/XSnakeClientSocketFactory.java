package org.xsnake.remote;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.URL;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Properties;

public class XSnakeClientSocketFactory implements RMIClientSocketFactory, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		Socket socket = new XSnakeClientSocket(host, port);
		OutputStream out = null;
		URL url = XSnakeClientSocketFactory.class.getResource("/auth.properties");
		String data = null;
		if(url != null){
			String path = url.getFile();
			File file = new File(path);
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream(file);
			try{
				prop.load(fis); 
				String username = prop.getProperty("username");
				String password = prop.getProperty("password");
				data = username + "," + password;
			}finally{
				fis.close();
			}
		}
		DataOutputStream write = null;
		try{
			int length = (data == null ? 0 : data.getBytes().length);
			out	= socket.getOutputStream();
			write= new DataOutputStream(out);
			write.writeInt(length);
			if(data!=null){
				write.writeBytes(data);
			}
			write.flush();
		}finally{
//			write.close();
		}
		return socket;
	}

}
