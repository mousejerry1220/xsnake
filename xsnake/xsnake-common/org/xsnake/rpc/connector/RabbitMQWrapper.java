package org.xsnake.rpc.connector;

import java.io.IOException;

import com.rabbitmq.client.Connection;

public class RabbitMQWrapper {
	
	protected Connection connection;
	
	public void close() throws IOException {
		connection.close();
	}

	public Connection getConnection() {
		return connection;
	}
	
}
