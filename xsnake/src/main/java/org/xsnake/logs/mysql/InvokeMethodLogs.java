package org.xsnake.logs.mysql;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;

import com.google.gson.Gson;

public class InvokeMethodLogs extends DatabaseLogs{
	
	private Gson gson = new Gson();
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	ServerInfo info;
	
	InvokeInfo invokeInfo;
	
	public InvokeMethodLogs(ServerInfo info, InvokeInfo invokeInfo) {
		
		this.info = info;
		
		this.invokeInfo = invokeInfo;
	}

	@Override
	String getCreateTableSQL() {
		return " CREATE TABLE `logs_invoke` ( " +
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
		Object[] args = new Object[]{info.getServerId(),info.getHost(),info.getPort(),
				invokeInfo.getTarget().getClass().getName(),invokeInfo.getMethod().getName(),gson.toJson(invokeInfo.getArgs()),
				gson.toJson(invokeInfo.getResult()),invokeInfo.getUseTime(),new Date()};
		return args;
	}

	@Override
	String getCurrentTableName() {
		String date = sdf.format(new Date());
		String table = date + "_logs_invoke";
		return table;
	}

	@Override
	String getTemplateTableName() {
		return "logs_invoke";
	}

}
