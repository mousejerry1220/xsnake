package org.xsnake.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Rest {
	
	String value();
	
	RequestMethod[] method() default RequestMethod.get;
	
	/**
	 * 如果请求参数内容过大，可以考虑压缩，减少网络压力
	 * 建议分析清楚是否存在该瓶颈再做设置，否则反而增加了服务器压力，及网络压力
	 * @return
	 */
	boolean compressRequest() default false;
	
	/**
	 * 如果返回值内容过大，可以考虑压缩，减少网络压力
	 * 建议分析清楚是否存在该瓶颈再做设置，否则反而增加了服务器压力，及网络压力
	 * @return
	 */
	boolean compressResponse() default false;
	
	/**
	 * 只读操作下使用缓存
	 * @return
	 */
	boolean cache() default false;
	
	/**
	 * 只读操作下,必须cache = true时生效
	 * 缓存的最大空闲时间，超出被清理
	 * @return
	 */
	long cacheMaxIdleTime() default 60*20;
	
	/**
	 * 非只读操作下，如果方法被执行，通知其他接口更新缓存数据
	 * 比如，用户修改了个人介绍信息，该用户的缓存信息需要被更新，值必须为Rest的value
	 * 如：updateCache = {"/user/{id}"}
	 * @return
	 */
	String[] updateCache() default {};
	
}
