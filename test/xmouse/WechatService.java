package xmouse;

import org.springframework.stereotype.Service;
import org.xsnake.remote.server.Remote;

@Service
@Remote
public class WechatService implements IWechatService {

	@Override
	public void xxx() {
		System.out.println("==================");
	}

}
