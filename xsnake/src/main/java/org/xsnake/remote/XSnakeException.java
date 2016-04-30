package org.xsnake.remote;



//如果抛出该异常，则为XSnake出来了bug所导致的
public class XSnakeException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public XSnakeException(String msg) {
		super(msg);
	}
	
}
