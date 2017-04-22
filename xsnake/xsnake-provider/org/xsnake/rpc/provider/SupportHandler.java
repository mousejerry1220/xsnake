package org.xsnake.rpc.provider;

import org.springframework.beans.factory.BeanCreationException;

public abstract class SupportHandler {

	abstract public void init(XSnakeProviderContext context) throws BeanCreationException;
	
}
