package org.xsnake.rpc.connector;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnector extends RabbitMQWrapper {

	static RabbitMQConnector rabbitMQ;

	public RabbitMQConnector(RabbitMQConfig config) throws IOException, TimeoutException {
		if (rabbitMQ != null) {
			return;
		}
		rabbitMQ = this;
		initRabbitMQ(config);
	}

	private void initRabbitMQ(RabbitMQConfig rabbitMQConfig) throws IOException, TimeoutException {
		String host = rabbitMQConfig.getHost();
		int port = rabbitMQConfig.getPort();
		String username = rabbitMQConfig.getUsername();
		String password = rabbitMQConfig.getPassword();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		if (rabbitMQConfig.getPort() > 0) {
			factory.setPort(port);
		}
		if (!StringUtils.isEmpty(username)) {
			factory.setUsername(username);
		}
		if (!StringUtils.isEmpty(password)) {
			factory.setUsername(password);
		}
		connection = factory.newConnection();
	}

}
