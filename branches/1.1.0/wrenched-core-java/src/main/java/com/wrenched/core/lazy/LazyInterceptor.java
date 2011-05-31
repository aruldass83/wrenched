package com.wrenched.core.lazy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

import static com.wrenched.util.ReflectionUtil.*;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

/**
 * interceptor that handles lazy-loading
 * @author konkere
 *
 */
public class LazyInterceptor implements MethodInterceptor, MethodHandler {
	private static final String LOAD = "__load";
	private static final String LOADING = "__loading";

	private Object id;
	private LazyAttributeRegistryDescriptor def;
	private boolean started = false;
	
	private Map<String, Boolean> loading = new HashMap<String, Boolean>();
	private Map<String, Boolean> loaded = new HashMap<String, Boolean>();
	
	LazyInterceptor(LazyAttributeRegistryDescriptor arg0, Object arg1) {
		this.id = arg1;
		this.def = arg0;
		
		for (String attributeName : def.attributes) {
			this.createLoader(attributeName);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		this.load(invocation.getThis(), invocation.getMethod());
        return invocation.proceed();                  
	}

	/*
	 * (non-Javadoc)
	 * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object arg0, Method arg1, Method arg2, Object[] arg3) throws Throwable {
		this.load(arg0, arg1);
		return arg2.invoke(arg0, arg3);
	}

	/**
	 * runs various checks on {@code arg1} to see if it's a suitable accessor for a lazy-loaded
	 * field and delegates to registry to load it.
	 * @param arg0
	 * @param arg1
	 * @throws Throwable
	 */
	private void load(Object arg0, Method arg1) throws Throwable {
	    if (this.started) {
	        String attributeName = getAttributeName(arg1);
	
	        if ((attributeName) != null && !this.isAttributeLoaded(attributeName) && !this.isLoading(attributeName)) {
	            this.loading.put(getLoadingFlagName(attributeName), true);
	                           
	            //if the attribute has data, perhaps it's better to keep it
	            if (isEmpty(getProxyAttributeValue(arg0, def.className, attributeName))) {
					this.process(arg0,
							LazyAttributeRegistry.getInstance().load(this.def.className,
									this.id,
									attributeName));
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
	
	private boolean isAttributeLoaded(String attributeName) {
		return this.loaded.get(getLoaderName(attributeName));
	}
	
	private boolean isLoading(String attributeName) {
		return this.loading.containsKey(getLoadingFlagName(attributeName)) &&
			this.loading.get(getLoadingFlagName(attributeName));
	}
	
	private void createLoader(String attributeName) {
		this.loaded.put(getLoaderName(attributeName), false);
		this.loading.put(getLoadingFlagName(attributeName), false);
	}
	
	private void deleteLoader(String attributeName) {
		this.loaded.remove(getLoaderName(attributeName));
		this.loading.remove(getLoadingFlagName(attributeName));
	}
	
	/**
	 * attaches lazy attribute value to its target object
	 * @param target
	 * @param la
	 */
    private void process(Object target, LazyAttribute la) { 
        setProxyAttributeValue(target, this.def.className, la.getAttributeName(), la.getAttributeValue());
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