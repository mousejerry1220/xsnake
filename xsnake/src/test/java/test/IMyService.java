package test;

import org.xsnake.rpc.annotation.Remote;

@Remote
public interface IMyService {
	String hello (String name);
}
