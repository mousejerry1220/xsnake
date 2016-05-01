package org.xsnake.logs.mysql;

import java.util.Date;

import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeContext;

public class MysqlXSnakeExceptionLogs extends MysqlDatabaseLogs {

	Throwable throwable;
	
	InvokeInfo invokeInfo;
	
	MysqlXSnakeExceptionLogs(InvokeInfo invokeInfo,Throwable throwable){
		this.throwable = throwable;
		this.invokeInfo = invokeInfo;
	}
	
	@Override
	String getCreateTableSQL() {
		return "CREATE TABLE `"+getTemplateTableName()+"` ( "+
				  " `SERVER_ID` varchar(50) DEFAULT NULL,"+
				  " `HOST` varchar(16) DEFAULT NULL,"+
				  " `port` int(11) DEFAULT NULL,"+
				  " `CREATE_DATE` date DEFAULT NULL,"+
				  " `ERROR_MESSAGE` varchar(500) DEFAULT NULL,"+
				  " `INTERFACE` varchar(200) DEFAULT NULL,"+
				  " `METHOD` varchar(50) DEFAULT NULL "+
				 ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
	}

	@Override
	String getInsertSQL() {
		return "INSERT INTO `"+getCurrentTableName()+"`(`SERVER_ID`,`HOST`,`port`,`CREATE_DATE`,`ERROR_MESSAGE`,`INTERFACE`,`METHOD`) VALUES ( ? , ? , ? , ? , ? , ? , ?)";
	}

	@Override
	Object[] getArgs() {
		ServerInfo serverInfo = XSnakeContext.getServerInfo();
		return new Object[]{serverInfo.getServerId(),serverInfo.getHost(),serverInfo.getPort(),new Date(),throwable.getMessage(),invokeInfo.getTarget().getClass().getName(),invokeInfo.getMethod().getName()};
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
