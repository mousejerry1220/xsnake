package org.xsnake.rpc.provider.rmi.monitor;

import java.io.Serializable;
import java.util.Date;

public class MonitorInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	//当前访问次数
	int times = 0;
	
	Date datetime = new Date();

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
	
}
