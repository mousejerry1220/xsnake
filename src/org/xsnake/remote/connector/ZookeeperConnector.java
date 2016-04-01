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

public class ZookeeperConnector {

	private static ZookeeperConnector connector;

	private ZooKeeper zk = null;
	
	public int ZK_SESSION_TIMEOUT = 5000;

	private String address;
	
	private int timeout = 0;
	
	private FutureTask<Boolean> ft = new FutureTask<Boolean>(new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			long now = System.currentTimeMillis();
			while((now + timeout*1000) > System.currentTimeMillis() ){
				if(zk != null && (zk.getState() == States.CONNECTED || zk.getState() == States.CONNECTEDREADONLY)){
					return false;
				}
				TimeUnit.MICROSECONDS.sleep(50);
				System.out.println("---------------------");
			}
			return true;
		}
	});
	

	public static ZookeeperConnector getConnector(String address,int timeout) {
		if (connector == null) {
			connector = new ZookeeperConnector();
		}
		connector.address = address;
		connector.timeout = timeout;
		connector.connectServer();
		return connector;
	}
	
	public static ZookeeperConnector getConnector(String address) {
		if (connector == null) {
			connector = new ZookeeperConnector();
		}
		connector.address = address;
		connector.connectServer();
		return connector;
	}
	

	private CountDownLatch latch = new CountDownLatch(1);
	
	//timeout 超时时间
	private ZooKeeper connectServer() {
		
		if(zk !=null && zk.getState() == States.CONNECTED){
			return zk;
		}
		
		try {
			zk = new ZooKeeper(address, ZK_SESSION_TIMEOUT, new Watcher() {
	            @Override
	            public void process(WatchedEvent event) {
	                if (event.getState() == Event.KeeperState.SyncConnected) {
	                    latch.countDown();
	                    System.out.println("zookeeper SyncConnected");
	                }else if(event.getState() == Event.KeeperState.Disconnected){
	                	System.out.println("zookeeper Disconnected");
	                }
	            }
	        });
			
			//如果设置超时时间，超时后抛出异常
			if(timeout > 0){
				new Thread(ft).start();
				boolean isTimeout = false;
				try {
					isTimeout = ft.get();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
				if(isTimeout){
					throw new ConnectException("connect to zookeeper time out ");
				}
			}else{
				//阻塞等待连接为止
				latch.await();
			}
			//否则一直等待，直到watcher收到连接成功事件唤醒	
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return zk;
	}

	public void publish(final String node, final String url,int version) throws Exception {
		
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
//		System.out.println(path);
		
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
			
			List<String> list = zk.getChildren(versionNode, null);
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
		byte[] data = zk.getData(node, false, null);
		if(data == null){
			return null;
		}
		return new String(data);
	} 
	
}
