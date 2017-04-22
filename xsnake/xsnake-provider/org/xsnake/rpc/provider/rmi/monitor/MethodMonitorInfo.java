package org.xsnake.rpc.provider.rmi.monitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MethodMonitorInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	int allTimes = 0;
	
	Date maxCallNumDate;
	
	int maxCallNum = 0;
	
	List<MonitorInfo> list = new ArrayList<MonitorInfo>();

	public int getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(int allTimes) {
		this.allTimes = allTimes;
	}

	public List<MonitorInfo> getList() {
		return list;
	}

	public void setList(List<MonitorInfo> list) {
		this.list = list;
	}

	public Date getMaxCallNumDate() {
		return maxCallNumDate;
	}

	public void setMaxCallNumDate(Date maxCallNumDate) {
		this.maxCallNumDate = maxCallNumDate;
	}

	public int getMaxCallNum() {
		return maxCallNum;
	}

	public void setMaxCallNum(int maxCallNum) {
		this.maxCallNum = maxCallNum;
	}
	
	
}
