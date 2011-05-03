package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * convenience class that allows mapping delegate methods to
 * string keys and then invoke it.
 * @author konkere
 *
 */
public class MethodBasedAccessor {
	protected Object delegate;
	protected final Map<String, String> methods = new HashMap<String, String>();
	
	private Class[] getArgTypes(Object[] args, boolean useExplicitTypes) {
		List<Class> classes = new ArrayList<Class>();
		
		for (Object arg : args) {
			if (useExplicitTypes) {
				classes.add(arg.getClass());
			}
			else {
				classes.add(arg instanceof Class ? Class.class : Object.class);
			}
		}
		
		return classes.toArray(new Class[classes.size()]);
	}
	
	/**
	 * 
	 * @param key
	 * @param useExplicitTypes
	 * @param args
	 * @return
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	public Object access(String key, boolean useExplicitTypes, Object... args)
	throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, IllegalAccessException { 
		String methodName = methods.get(key);
		return this.delegate.getClass().getMethod(methodName, getArgTypes(args, useExplicitTypes)).invoke(this.delegate, args);
	}
	
	/**
	 * checks that at least the delegate has methods with specified names (doesn't check
	 * on the argument classes, as it is known only during runtime.
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (this.delegate != null && !this.methods.isEmpty()) {
			Collection<String> names = ClassIntrospectionUtil.getMethodNames(this.delegate.getClass());
			
			for (String methodName : this.methods.values()) {
				assert names.contains(methodName);
			}
//			for (Method m : this.delegate.getClass().getMethods()) {
//				assert this.methods.values().contains(m.getName());
//			}
		}
	}
	
	/**
	 * 
	 * @param delegate
	 */
	public void setDelegate(Object delegate) {
		this.delegate = delegate;
	}

	public Map<String, String> getMethods() {
		return this.methods;
	}
	
	/**
	 * a map of loader methods per key
	 * @param ms
	 */
	public void setMethods(Map<String, String> ms) {
		this.methods.putAll(ms);
	}
}
