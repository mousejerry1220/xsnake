package org.xsnake.xmouse;

import org.xsnake.remote.XSnakeRMIAuthentication;

public class TestAuth implements XSnakeRMIAuthentication{

	@Override
	public boolean login(String username, String password) {
		if("Jerry".equals(username) && "123456".equals(password)){
			System.out.println("----验证成功");
			return true;
		}
		System.out.println("----验证失败");
		return false;
	}

}
