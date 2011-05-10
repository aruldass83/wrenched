package com.wrenched.core.instrumentation.adapters;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Proxy;
import java.util.Collection;

import net.sf.cglib.proxy.Enhancer;

import javassist.util.proxy.ProxyFactory;

import com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor;

/**
 * convenient adapter implementation for pure proxies like Javassist, CGLIB, jdk, etc
 * @author konkere
 *
 */
public class BytecodeProxyAdapter extends AbstractUnwrappingInstrumentor {
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#isSimpleProxy(java.lang.Object)
	 */
	@Override
	public boolean isSimpleProxy(Object o) {
		return ProxyFactory.isProxyClass(o.getClass()) ||
			Proxy.isProxyClass(o.getClass()) ||
			Enhancer.isEnhanced(o.getClass());
	}

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#isCollectionProxy(java.lang.Object)
	 */
	@Override
	public boolean isCollectionProxy(Object o) {
		return isSimpleProxy(o) && Collection.class.isAssignableFrom(o.getClass());
	}

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#getProxyTarget(java.lang.Object)
	 */
	@Override
	public Object getProxyTarget(Object o) {
        Class targetClass = o.getClass().getSuperclass();
        
        try {
	        Object instance = targetClass.newInstance();
	        
	        for (PropertyDescriptor pd : Introspector.getBeanInfo(targetClass).getPropertyDescriptors()) {
	        	if ((pd.getReadMethod() != null) && (pd.getWriteMethod() != null)) {
	        		pd.getWriteMethod().invoke(instance, new Object[] {pd.getReadMethod().invoke(o, (Object[])null)});
	        	}
	        }
	        
	        return instance;
        }
        catch (Exception e) {
        	return o;
        }
	}
}
