package org.xsnake.plug;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xsnake.common.BaseDaoUtil;
import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeAbstactInterceptor;

import com.google.gson.Gson;

//for mysql
public class InvokeLogsInterceptor extends XSnakeAbstactInterceptor{
	
	private final static Logger LOG = LoggerFactory.getLogger(InvokeLogsInterceptor.class) ;
	
	private ExecutorService service =  Executors.newFixedThreadPool(10);
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	private Gson gson = new Gson();
	
	public static String CREATE_TABLE_LOGS_INVOKE =" CREATE TABLE `logs_invoke` ( " +
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
	
	@Override
	public void after(ServerInfo info, InvokeInfo invokeInfo) {
		String date = sdf.format(new Date());
		final String table = date + "logs_invoke";
		final String sql = "INSERT INTO " + table +
				" (SERVER_ID,HOST,PORT,INTERFACE,METHOD,ARGS,RESULT,USE_TIME,INVOKE_TIME) "
				+ "VALUES (?,?,?,?,?,?,?,?,?)" ;
		final Object[] args = new Object[]{info.getServerId(),info.getHost(),info.getPort(),
				invokeInfo.getTarget().getClass().getName(),invokeInfo.getMethod().getName(),gson.toJson(invokeInfo.getArgs()),
				gson.toJson(invokeInfo.getResult()),invokeInfo.getUseTime(),new Date()};
		Thread t = new Thread(){
			public void run() {
				try {
					BaseDaoUtil.executeUpdate(sql, args);
				} catch (SQLException e) {
					if(e.getErrorCode() == 1146){
						try {
							BaseDaoUtil.executeUpdate("CREATE TABLE "+table+" SELECT * FROM logs_invoke WHERE 1=0",null);
							BaseDaoUtil.executeUpdate(sql, args);
						} catch (SQLException e1) {
							if(e1.getErrorCode() == 1146){
								try {
									BaseDaoUtil.executeUpdate(CREATE_TABLE_LOGS_INVOKE,null);
									BaseDaoUtil.executeUpdate(sql, args);
								} catch (SQLException e2) {
								}
							}
						}
					}else{
						LOG.warn(e.getMessage());
					}
				}
			};
		};
		service.execute(t);
	}
}
