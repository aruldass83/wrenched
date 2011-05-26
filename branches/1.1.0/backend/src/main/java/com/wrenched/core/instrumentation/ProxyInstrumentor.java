package com.wrenched.core.instrumentation;

/**
 * 
 * an instrumentor that removes proxies from object
 * @author konkere
 *
 */
public interface ProxyInstrumentor {
	/**
	 * unwrap an object from being proxied
	 * @param o potentially proxified object
	 * @return proxy target
	 * @throws NoSuchFieldException
	 */
	public Object unwrap(Object o) throws NoSuchFieldException;
}
