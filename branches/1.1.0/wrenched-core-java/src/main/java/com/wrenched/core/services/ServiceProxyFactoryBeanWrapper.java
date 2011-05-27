package com.wrenched.core.services;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.wrenched.core.lazy.LazyAttributeRegistry;

public class ServiceProxyFactoryBeanWrapper extends AbstractFactoryBean implements MethodInterceptor {
	private Object delegate;

	protected Object createInstance() throws Exception {
		return new ProxyFactory(getObjectType(), this).getProxy();
	}

	public Class getObjectType() {
		//FIXME: see how it works
		return delegate.getClass().getInterfaces()[0];
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return LazyAttributeRegistry.getInstance().createProxy(invocation.proceed());
	}
	
	public void setDelegate(Object arg0) {
		this.delegate = arg0;
	}
}
