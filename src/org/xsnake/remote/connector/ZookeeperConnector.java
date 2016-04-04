package org.xsnake.remote.connector;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.UUID;
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
					return true;//连接成功
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
			createZooKeeper(latch);
			
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

	private void createZooKeeper(CountDownLatch latch) throws IOException {
		
		zooKeeper = new ZooKeeper(address, ZK_SESSION_TIMEOUT, new Watcher() {
		    @Override
		    public void process(WatchedEvent event) {
		        if (event.getState() == Event.KeeperState.SyncConnected) {
		            latch.countDown();
		        }
		        
		        if(event.getState() == Event.KeeperState.Expired){
		        	connectServer();
		        }
		        
		        if(watcher !=null){
		        	watcher.process(event);
		        }
		    }
		});
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
	
	
	/**
	 * 发布服务信息到ZooKeeper
	 * @param node
	 * @param url
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public String publish(final String node, final String url,int version) throws Exception {
		
		if(StringUtils.isEmpty(node)){
			throw new Exception("node name must be not null");
		}
		
		if(StringUtils.isEmpty(url)){
			throw new Exception("url must be not null");
		}

		
		ZooKeeper zk = connectServer(); // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
		String xsnakeNode = "/xsnake";
		String serviceNode = xsnakeNode +"/service";
		String rootNode = serviceNode+ "/"+node;
		String versionNode = rootNode + "/"+version;
		String maxVersionNode = rootNode + "/maxVersion";
		String maxVersion = null;
		
		createDirNode(xsnakeNode,null,CreateMode.PERSISTENT);
		createDirNode(serviceNode,null,CreateMode.PERSISTENT);
		createDirNode(rootNode,null,CreateMode.PERSISTENT);
		createDirNode(versionNode,null,CreateMode.PERSISTENT);
		createDirNode(maxVersionNode,String.valueOf(version).getBytes(),CreateMode.EPHEMERAL);
		
		maxVersion = getStringData(maxVersionNode);
		if(Integer.parseInt(maxVersion) < version){
			createNode(maxVersionNode, String.valueOf(version).getBytes(), CreateMode.EPHEMERAL);
		}
		
		String path = zk.create(versionNode + "/"+UUID.randomUUID().toString(), url.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println(path);
		return path;
	}
	
	//创建一个Node，如果存在则删除后创建
	private void createNode(String node,byte[] data,CreateMode mode) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		if(zk.exists(node, null)!=null){
			zk.delete(node,-1);
		}
		zk.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
	}
	
	//如果不存在则创建，存在则不创建
	private void createDirNode(String node,byte[] data,CreateMode mode) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		if(zk.exists(node, null)==null){
			zk.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		}
	}
	
	public String getData(String node,int version) {
		try {
			String xsnakeNode = "/xsnake";
			String serviceNode = xsnakeNode +"/service";
			String rootNode = serviceNode+"/"+node;
			String maxVersionNode = rootNode + "/maxVersion";
			String maxVersion = getStringData(maxVersionNode);
			String versionNode = ( version == 0 ? rootNode + "/" + maxVersion : rootNode + "/" + version);

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
