package org.xsnake.logs.mysql;

import java.net.Socket;

public abstract class MysqlRejectLogs extends MysqlDatabaseLogs{

	Socket socket;
	
	public MysqlRejectLogs(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	String getCreateTableSQL() {
		return " CREATE TABLE `"+getTemplateTableName()+"` ( "+
			" `SERVER_ID` varchar(50) DEFAULT NULL, "+
			" `HOST` varchar(16) DEFAULT NULL, "+
			" `PORT` int(11) DEFAULT NULL, "+
			" `TYPE` varchar(20) DEFAULT NULL, "+
			" `CLIENT_HOST` varchar(16) DEFAULT NULL, "+
			" `CLIENT_PORT` int(11) DEFAULT NULL, "+
			" `DATETIME` datetime DEFAULT NULL "+
			" ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ";
	}

	@Override
	String getInsertSQL() {
		return "INSERT INTO `"+getCurrentTableName()+"`(`SERVER_ID`,`HOST`,`PORT`,`TYPE`,`CLIENT_HOST`,`CLIENT_PORT`,`DATETIME`) VALUES (? , ? , ? , ? , ? , ? , ?)";
	}

	@Override
	String getCurrentTableName() {
		return getTemplateTableName();
	}

	@Override
	String getTemplateTableName() {
		return "xsnake_logs_reject";
	}
	
}
