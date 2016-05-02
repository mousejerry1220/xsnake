package org.xsnake.logs.mysql;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xsnake.common.BaseDaoUtil;

public abstract class MysqlDatabaseLogs {

	abstract String getCreateTableSQL();
	
	abstract String getInsertSQL();
	
	abstract Object[] getArgs();
	
	abstract String getCurrentTableName();
	
	abstract String getTemplateTableName();
	
	public void log(){
		ExecutorService service =  Executors.newSingleThreadExecutor();
		Thread t = new Thread(){
			public void run() {
				try {
					BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
				} catch (SQLException e) {
					if(e.getErrorCode() == 1146){
						try {
							if(!getTemplateTableName().equals(getCurrentTableName())){
								BaseDaoUtil.executeUpdate("CREATE TABLE "+getCurrentTableName()+" SELECT * FROM "+getTemplateTableName()+" WHERE 1=0 ",null);
							}
							BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
						} catch (SQLException e1) {
							if(e1.getErrorCode() == 1146){
								try {
									BaseDaoUtil.executeUpdate(getCreateTableSQL(),null);
									BaseDaoUtil.executeUpdate(getInsertSQL(), getArgs());
								} catch (SQLException e2) {
								}
							}
						}
					}
				}
			};
		};
		service.execute(t);
		service.shutdown();
	}
}
