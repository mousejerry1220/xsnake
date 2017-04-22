package org.xsnake.rpc.connector;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperConnector extends ZooKeeperWrapper{
	
	static ZooKeeperConnector instance;
	
	ZooKeeperExpiredCallBack callback;
	
	public ZooKeeperConnector(String address,long timeout,ZooKeeperExpiredCallBack callback) throws IOException, TimeoutException {
		this.callback = callback;
		instance = this;
		initZooKeeper(address,timeout*1000);
	}
	
	private CountDownLatch timeoutCountDownLatch = null;

	/**
	 * 第一次初始化时执行，如果超时，报错
	 * @param servers
	 * @param _timeout
	 * @throws IOException
	 * @throws TimeoutException 
	 */
	private void initZooKeeper(final String servers, final long _timeout) throws IOException, TimeoutException {
		
		long start = System.currentTimeMillis();
		
		timeoutCountDownLatch = new CountDownLatch(1);
		
		_zooKeeper = new ZooKeeper(servers, 5000, new Watcher() {
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.Expired){
					if(_zooKeeper!=null){
						try {
							_zooKeeper.close();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					callback.callback();
				}
				if(event.getState() == KeeperState.SyncConnected){
					timeoutCountDownLatch.countDown();
				}
			}
		});
		
		new Thread(){
			public void run() {
				try {
					TimeUnit.MILLISECONDS.sleep(_timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				timeoutCountDownLatch.countDown();
			};
		}.start();
		
		try {
			timeoutCountDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(System.currentTimeMillis() - start > _timeout ){
			
			throw new TimeoutException("连接超时");
			
		}
		
	}

	
	/**
	 * 启动后，在使用过程中，如果zooKeeper连接异常（ KeeperState.Expired），重新创建实例
	 * @param servers
	 */
//	private void createZooKeeper(final String servers) {
//		
//		if(_zooKeeper !=null){
//			try {
//				_zooKeeper.close();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		while(true){
//			try {
//				_zooKeeper = new ZooKeeper(servers, 5000, new Watcher() {
//					public void process(WatchedEvent event) {
//						if(event.getState() == KeeperState.Expired){
//							createZooKeeper(servers);
//						}
//					}
//				});
//				break;
//			} catch (IOException e) {
//				try {
//					TimeUnit.SECONDS.sleep(5);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}
//	}
	
}
