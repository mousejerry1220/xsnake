package org.xsnake.logs.mysql;

import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeContext;

public class MysqlServerStartupLogs extends MysqlDatabaseLogs{

	@Override
	String getCreateTableSQL() {
		return "CREATE TABLE `"+getTemplateTableName()+"` ( "+
				  " `SERVER_ID` varchar(50) DEFAULT NULL,"+
				  " `HOST` varchar(16) DEFAULT NULL,"+
				  " `PORT` int(11) DEFAULT NULL,"+
				  " `STARTUP_DATE` datetime DEFAULT NULL ,"+
				  " `STARTUP_USE_TIME` int(11) DEFAULT NULL"+
				 ") ENGINE=InnoDB DEFAULT CHARSET=utf8 ";
	}

	@Override
	String getInsertSQL() {
		return "INSERT INTO `"+getCurrentTableName()+"`(`SERVER_ID`,`HOST`,`PORT`,`STARTUP_DATE`,`STARTUP_USE_TIME`) VALUES ( ? , ? , ? , ? , ?) ";
	}

	@Override
	Object[] getArgs() {
		ServerInfo serverInfo = XSnakeContext.getServerInfo();
		return new Object[]{serverInfo.getServerId(),serverInfo.getHost(),serverInfo.getPort(),serverInfo.getStartupDate(),serverInfo.getStartupUseTime()};
	}

	@Override
	String getCurrentTableName() {
		return getTemplateTableName();
	}

	@Override
	String getTemplateTableName() {
		return "xsnake_logs_startup";
	}

}
