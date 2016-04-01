package org.xsnake.remote.connector;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.springframework.util.StringUtils;
 

/**
 * 如果设置timeout <= 0 那么客户端会一直阻塞直至连接到ZooKeeper为止。
 * 如果设置timeout > 0 那么客户端会在设置时间后还没有连接到ZooKeeper而抛出java.net.ConnectException的连接异常
 * @author Jerry.Zhao
 *
 */
public class ZookeeperConnector {
	
	private static ZookeeperConnector connector;

	private ZooKeeper zooKeeper = null;
	
	public int ZK_SESSION_TIMEOUT = 5000;

	private String address;
	
	private int timeout = 0;
	
	private Watcher watcher;
	
	private FutureTask<Boolean> ft = new FutureTask<Boolean>(
			new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					long _timeout = System.currentTimeMillis() + (timeout * 1000) ; 
					while (_timeout > System.currentTimeMillis()) {
						if (zooKeeper != null &&  (zooKeeper.getState() == States.CONNECTED || zooKeeper .getState() == States.CONNECTEDREADONLY) ) {
							return false; //超出了超时时间还没有连接上服务器，抛出异常
						}
						TimeUnit.MICROSECONDS.sleep(50);
					}
					return true;
				}
			});

	public static ZookeeperConnector getConnector(String address,int timeout,Watcher watcher) {
		if (connector == null) {
			connector = new ZookeeperConnector();
		}
		connector.address = address;
		connector.timeout = timeout;
		connector.watcher = watcher;
		connector.connectServer();
		return connector;
	}
	
	/**
	 * 连接服务器
	 * @return 返回ZooKeeper
	 */
	private ZooKeeper connectServer() {
		
		//单个计数协作器，当计数后则阻塞方法向下执行
		CountDownLatch latch = new CountDownLatch(1);
		
		//如果zooKeeper对象已经实例过了则直接返回
		if(zooKeeper !=null && zooKeeper.getState() == States.CONNECTED){
			return zooKeeper;
		}
		
		try {
			//实例化ZooKeeper
			zooKeeper = new ZooKeeper(address, ZK_SESSION_TIMEOUT, new Watcher() {
	            @Override
	            public void process(WatchedEvent event) {
	                if (event.getState() == Event.KeeperState.SyncConnected) {
	                    latch.countDown();
	                }
	                
	                if(watcher !=null){
	                	watcher.process(event);
	                }
	            }
	        });
			
			//如果设置超时时间，超时后抛出异常
			if(timeout > 0){
				waitConnect();
			}
			
			//阻塞等待连接为止
			else{
				latch.await();
			}
			//否则一直等待，直到watcher收到连接成功事件唤醒	
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return zooKeeper;
	}

	
	/**
	 * 等待连接,开启一个子线程判断连接是否超时，超时则抛出错误
	 * @throws InterruptedException
	 * @throws ConnectException
	 */
	private void waitConnect() throws InterruptedException, ConnectException {
		new Thread(ft).start();
		boolean isTimeout = false;
		try {
			isTimeout = ft.get(); //阻塞方法，直至子线程返回连接结果
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if(isTimeout){
			throw new ConnectException("connect to zookeeper time out ");
		}
	}
	
	
	
	public String publish(final String node, final String url,int version) throws Exception {
		
		if(StringUtils.isEmpty(node)){
			throw new Exception("node name must be not null");
		}
		
		if(StringUtils.isEmpty(url)){
			throw new Exception("url must be not null");
		}
		
		String rootNode = "/"+node;
		
		ZooKeeper zk = connectServer(); // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象

		if(zk == null){
			throw new Exception(" zookeeper connect failed !");
		}
		
		if(zk.exists(rootNode, null)==null){
			zk.create(rootNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		
		String versionNode = rootNode + "/"+version;
		String maxVersionNode = rootNode + "/maxVersion";
		String maxVersion = null;
		if(zk.exists(versionNode, null) == null){
			zk.create(versionNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			
			if(zk.exists(maxVersionNode, null) == null){
				zk.create(maxVersionNode, String.valueOf(version).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			
			maxVersion = getStringData(maxVersionNode);
			if(Integer.parseInt(maxVersion) < version){
				zk.create(maxVersionNode, String.valueOf(version).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
		
		String path = zk.create(versionNode + "/TEMP_", url.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		return path;
	}
	
	public String getData(String node,int version) {
		try {
			
			String rootNode = "/"+node;
			String maxVersionNode = rootNode + "/maxVersion";
			String versionNode = null;
			String maxVersion = getStringData(maxVersionNode);
			if(version == 0){
				versionNode = rootNode + "/" + maxVersion;
			}else{
				versionNode = rootNode + "/" + version;
			}
			
			List<String> list = zooKeeper.getChildren(versionNode, null);
			if(list.size() == 0){
				return null;
			}
			String path = list.get(RandomUtils.nextInt(list.size()));
			String data = getStringData(versionNode+"/"+path);
			return data;
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getStringData(String node) throws KeeperException, InterruptedException{
		byte[] data = zooKeeper.getData(node, false, null);
		if(data == null){
			return null;
		}
		return new String(data);
	} 
	
}
