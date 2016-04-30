package org.xsnake.logs.mysql;

import java.util.Date;

import org.xsnake.common.SingletonUtil;
import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeContext;

public class MysqlInvokeMethodLogs extends MysqlDatabaseLogs{
	
	InvokeInfo invokeInfo;
	
	public MysqlInvokeMethodLogs( InvokeInfo invokeInfo) {
		this.invokeInfo = invokeInfo;
	}

	@Override
	String getCreateTableSQL() {
		return " CREATE TABLE `"+getTemplateTableName()+"` ( " +
				" `SERVER_ID` varchar(50) DEFAULT NULL," +
				"  `HOST` varchar(16) DEFAULT NULL," +
				"  `PORT` int(11) DEFAULT NULL," +
				"   `INTERFACE` varchar(200) DEFAULT NULL," +
				"   `METHOD` varchar(50) DEFAULT NULL," +
				"   `ARGS` varchar(500) DEFAULT NULL," +
				"   `RESULT` varchar(500) DEFAULT NULL," +
				"   `USE_TIME` int(11) DEFAULT NULL," +
				"   `INVOKE_TIME` datetime DEFAULT NULL" +
				"  ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
	}

	@Override
	String getInsertSQL() {
		String sql = "INSERT INTO " + getCurrentTableName() +
				" (SERVER_ID,HOST,PORT,INTERFACE,METHOD,ARGS,RESULT,USE_TIME,INVOKE_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)" ;
		return sql;
	}

	@Override
	Object[] getArgs() {
		ServerInfo info = XSnakeContext.getServerInfo();
		Object[] args = new Object[]{info.getServerId(),info.getHost(),info.getPort(),
				invokeInfo.getTarget().getClass().getName(),invokeInfo.getMethod().getName(),SingletonUtil.getGson().toJson(invokeInfo.getArgs()),
				SingletonUtil.getGson().toJson(invokeInfo.getResult()),invokeInfo.getUseTime(),new Date()};
		return args;
	}

	@Override
	String getCurrentTableName() {
		String date = SingletonUtil.getSimpleDateFormat().format(new Date());
		String table = getTemplateTableName()  + "_"+ date;
		return table;
	}

	@Override
	String getTemplateTableName() {
		return "xsnake_logs_invoke";
	}

}
