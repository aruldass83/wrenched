package com.wrenched.core.lazy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.wrenched.core.domain.LazyAttribute;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

public class LazyInterceptor implements MethodInterceptor, MethodHandler {
	private static final String LOAD = "__load";
	private static final String LOADING = "__loading";

	private String clazz;
	private Object id;
	private boolean started = false;
	
	private Map<String, Boolean> loading = new HashMap<String, Boolean>();
	private Map<String, Boolean> loaded = new HashMap<String, Boolean>();
	

	LazyInterceptor(String className, Object id, String[] attributes) {
		this.clazz = className;
		this.id = id;
		
		for (String attributeName : attributes) {
			this.createLoader(attributeName);
		}
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		this.load(invocation.getThis(), invocation.getMethod());
        return invocation.proceed();                  
	}

	@Override
	public Object invoke(Object arg0, Method arg1, Method arg2, Object[] arg3) throws Throwable {
		this.load(arg0, arg1);
		return arg1.invoke(arg0, arg3);
	}

	private void load(Object arg0, Method arg1) throws Throwable {
	    if (this.started) {
	        String attributeName = getAttributeName(arg1);
	
	        if ((attributeName) != null && !this.isAttributeLoaded(attributeName) && !this.isLoading(attributeName)) {
	            this.loading.put(getLoadingFlagName(attributeName), true);
	                           
	            //if the attribute has data, perhaps it's better to keep it
	            if (isEmpty(getAttributeValue(arg0, attributeName))) {
					this.process(arg0,
							LazyAttributeRegistry.getInstance().load(this.clazz, this.id, attributeName));
	            }
	            else {
	                this.deleteLoader(attributeName);
	            }
	        }
	    }
	}
	
	
	void start() {
		started = true;
	}

	void stop() {
		started = false;
	}
	
	/**
	 * checks if an attribute must be lazy loaded (or in other terms
	 * has a loader function)
	 */
	private boolean isAttributeLoaded(String attributeName) {
		return this.loaded.get(getLoaderName(attributeName));
	}
	
	/**
	 * checks if at attribute is being loaded already (safety as
	 * flex tends to call attribute getters hundreds of times).
	 */
	private boolean isLoading(String attributeName) {
		return this.loading.containsKey(getLoadingFlagName(attributeName)) &&
			this.loading.get(getLoadingFlagName(attributeName));
	}
	
	/**
	 * creates a dynamic loader function for an attribute
	 */
	private void createLoader(String attributeName) {
		this.loaded.put(getLoaderName(attributeName), false);
		this.loading.put(getLoadingFlagName(attributeName), false);
	}
	
	/**
	 * deletes loader function
	 */
	private void deleteLoader(String attributeName) {
		this.loaded.remove(getLoaderName(attributeName));
		this.loading.remove(getLoadingFlagName(attributeName));
	}
	
    private void process(Object target, LazyAttribute la) { 
        setAttributeValue(target, la.getAttributeName(), la.getAttributeValue());
        this.deleteLoader(la.getAttributeName()); 
    } 
	
	private static String getLoaderName(String attributeName) {
		return LOAD + "_" + attributeName;
	}
	
	private static String getLoadingFlagName(String attributeName) {
		return LOADING + "_" + attributeName;
	}
	
	private static boolean isEmpty(Object value) {
		return (value == null) || (value instanceof Collection && ((Collection)value).size() == 0);
	}
}