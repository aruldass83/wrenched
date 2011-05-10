package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeFetcher;

/**
 * convenience class that allows mapping delegate methods to
 * string keys and then invoke it.
 * @author konkere
 *
 */
public abstract class MethodBasedAccessor {
	protected Object delegate;
	private final Map<String, String> methods = new HashMap<String, String>();
	
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
	 * read method metadata if applicable and check that at least the delegate has methods with specified declaration
	 * (doesn't check on the argument classes, as it is known only during runtime).
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (this.delegate != null) {
			for (Method m : this.delegate.getClass().getDeclaredMethods()) {
				if (m.isAnnotationPresent(LazyAttributeFetcher.class)) {
					if (this.isDeclarationSuitable(m.getAnnotation(LazyAttributeFetcher.class), m)) {
						this.methods.put(this.getKey(m.getAnnotation(LazyAttributeFetcher.class), m), m.getName());
					}
				}
			}
			
			if (!this.methods.isEmpty()) {
				Collection<String> names = ClassIntrospectionUtil.getMethodNames(this.delegate.getClass());
				
				for (String methodName : this.methods.values()) {
					assert names.contains(methodName);
				}
			}
		}
	}
	
	public void setDelegate(Object delegate) {
		this.delegate = delegate;
	}

	public Map<String, String> getMethods() {
		return this.methods;
	}
	
	protected void addMethod(String key, String name) {
		this.methods.put(key, name);
	}
	
	/**
	 * subclasses must provide convenient keys for fetcher methods
	 * @param metadata
	 * @param m
	 * @return
	 */
	protected abstract String getKey(LazyAttributeFetcher metadata, Method m);
	
	/**
	 * subclasses must determine if a fetcher method is suitable for them to use
	 * @param metadata
	 * @param m
	 * @return
	 */
	protected abstract boolean isDeclarationSuitable(LazyAttributeFetcher metadata, Method m);
}
