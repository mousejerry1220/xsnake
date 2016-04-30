package org.xsnake.remote.connector;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/*
 * 如果设置timeout <= 0 那么客户端会一直阻塞直至连接到ZooKeeper为止。
 * 如果设置timeout > 0 那么客户端会在设置时间后还没有连接到ZooKeeper而抛出java.net.ConnectException的连接异常
 * @author Jerry.Zhao
 *
 */
public class ZookeeperConnector {
	
	private final static Logger LOG = LoggerFactory.getLogger(ZookeeperConnector.class) ; 
	
	private static ZookeeperConnector connector;

	private ZooKeeper zooKeeper = null;
	
	public int ZK_SESSION_TIMEOUT = 5000;

	private String address;
	
	private int timeout = 0;
	
	private Watcher watcher;
	
	private FutureTask<Boolean> ft = new FutureTask<Boolean>(
		new Callable<Boolean>() {
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

	public synchronized static ZookeeperConnector getConnector(String address,int timeout,Watcher watcher) {
		if (connector == null) {
			connector = new ZookeeperConnector();
		}
		connector.address = address;
		connector.timeout = timeout;
		connector.watcher = watcher;
		Thread thread = new Thread(){
			public void run() {
				connector.connectServer();
			};
		};
		thread.start();
		return connector;
	}
	
	/**
	 * 连接服务器
	 * @return 返回ZooKeeper
	 */
	private synchronized ZooKeeper connectServer() {
		
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
		} catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return zooKeeper;
	}

	private synchronized void createZooKeeper(final CountDownLatch latch) throws IOException {
		
		zooKeeper = new ZooKeeper(address, ZK_SESSION_TIMEOUT, new Watcher() {
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
	
	//创建一个Node，如果存在则删除后创建，只能用于最后一级节点
	public void createOrUpdateNode(String node,byte[] data,CreateMode mode) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		if(zk.exists(node, null)!=null){
			zk.setData(node, data,-1);
		}else{
			zk.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		}
		LOG.info("创建节点数据："+node + " 内容： "+new String(data));
	}
	
	public void updateDateData(String node,byte[] data) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		zk.setData(node, data,-1);
	}
	
	
	//如果不存在则创建，存在则不创建
	public void createDirNode(String node,byte[] data,CreateMode mode) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		if(zk.exists(node, null)==null){
			zk.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
			LOG.info("创建目录："+node + (data == null ? "" : " 节点数据："+ new String(data)));
		}
	}
	
	public boolean exists(String node) throws KeeperException, InterruptedException{
		ZooKeeper zk = connectServer();
		return zk.exists(node, null) != null;
	}
	
	public Map<String, Object> getMapData(String rootNode) throws KeeperException, InterruptedException {
		ZooKeeper zk = connectServer();
		byte[] data = zk.getData(rootNode, null, null);
		if(data == null){
			return new HashMap<String, Object>();
		}
		String interfaceInfo = new String(data);
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String,Object> interfaceInfoData = gson.fromJson(interfaceInfo, Map.class);
		return interfaceInfoData;
	}
	
	public String getStringData(String node) throws InterruptedException{
		ZooKeeper zk = connectServer();
		try{
			byte[] data = zk.getData(node, false, null);
			if(data == null){
				return null;
			}
			return new String(data);
		}catch(KeeperException e){
			return null;
		}
	}

	public void close() {
		try {
			zooKeeper.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		return zooKeeper.getChildren(path, null);
	}
	
}
