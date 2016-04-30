package org.xsnake.logs.mysql;

import java.util.Date;

import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeContext;

public class MysqlXSnakeExceptionLogs extends MysqlDatabaseLogs {

	Throwable throwable;
	
	MysqlXSnakeExceptionLogs(Throwable throwable){
		this.throwable = throwable;
	}
	
	@Override
	String getCreateTableSQL() {
		return "CREATE TABLE `logs_error` ( "+
				  " `SERVER_ID` varchar(50) DEFAULT NULL,"+
				  " `HOST` varchar(16) DEFAULT NULL,"+
				  " `port` int(11) DEFAULT NULL,"+
				  " `CREATE_DATE` date DEFAULT NULL,"+
				  " `ERROR_MESSAGE` varchar(500) DEFAULT NULL"+
				 ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
	}

	@Override
	String getInsertSQL() {
		return "INSERT INTO `"+getCurrentTableName()+"`(`SERVER_ID`,`HOST`,`port`,`CREATE_DATE`,`ERROR_MESSAGE`) VALUES ( ? , ? , ? , ? , ?)";
	}

	@Override
	Object[] getArgs() {
		ServerInfo serverInfo = XSnakeContext.getServerInfo();
		return new Object[]{serverInfo.getServerId(),serverInfo.getHost(),serverInfo.getPort(),new Date(),throwable.getMessage()};
	}

	@Override
	String getCurrentTableName() {
		return getTemplateTableName();
	}

	@Override
	String getTemplateTableName() {
		return "xsnake_logs_error";
	}

}
