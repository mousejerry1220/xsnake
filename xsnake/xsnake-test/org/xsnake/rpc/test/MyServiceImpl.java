package org.xsnake.rpc.test;

import org.springframework.stereotype.Service;

@Service
public class MyServiceImpl implements IMyService{

	@Override
	public String hello(String name) {
		return "hello , " + name;
	}

}
