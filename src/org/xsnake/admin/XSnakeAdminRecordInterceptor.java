package org.xsnake.admin;

import java.sql.SQLException;

import org.xsnake.admin.dao.BaseDaoUtil;
import org.xsnake.admin.dao.ConnectionPool;
import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeAbstactInterceptor;

public class XSnakeAdminRecordInterceptor extends XSnakeAbstactInterceptor{

	@Override
	public void after(ServerInfo info, InvokeInfo invokeInfo) {
		
		//TODO 在这里添加代码
		for(int i =0;i<100;i++){
			Thread t = new Thread(){
				public void run() {
					try {
						BaseDaoUtil.executeUpdate("SQL", null);
						System.out.println(ConnectionPool.getInstance().pool.size());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				};
			};
			t.start();
		}
	}
	
}
