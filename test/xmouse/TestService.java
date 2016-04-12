package xmouse;

import org.springframework.stereotype.Service;
import org.xsnake.remote.server.Remote;

@Service
@Remote(version = 17)
public class TestService implements ITestServiceA{

}
