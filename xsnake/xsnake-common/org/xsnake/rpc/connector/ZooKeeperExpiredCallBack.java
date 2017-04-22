package org.xsnake.rpc.connector;

/**
 * ZooKeeper客户端连接时发生过期时（Expired）会丢失所有连接信息，需要重新初始化恢复客户端正常功能
 * 如：调整系统时间会导致过期
 * 本接口用于过期发生时回调
 * @author Administrator
 *
 */
public interface ZooKeeperExpiredCallBack {

	void callback();
	
}
