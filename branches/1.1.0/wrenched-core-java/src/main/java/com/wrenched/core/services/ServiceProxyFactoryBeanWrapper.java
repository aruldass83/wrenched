package com.wrenched.core.services;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

import com.wrenched.core.lazy.LazyAttributeRegistry;

public class ServiceProxyFactoryBeanWrapper implements FactoryBean, InitializingBean, MethodInterceptor {
	private FactoryBean delegate;
	private Object serviceProxy;
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void afterPropertiesSet() throws Exception {
		if (delegate instanceof InitializingBean) {
			((InitializingBean)delegate).afterPropertiesSet();
		}
		
		this.serviceProxy = new ProxyFactory(getObjectType(), this).getProxy(this.beanClassLoader);
	}

	public Object getObject() throws Exception {
		return serviceProxy;
	}

	public Class getObjectType() {
		return delegate.getObjectType();
	}

	public boolean isSingleton() {
		return delegate.isSingleton();
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		return LazyAttributeRegistry.getInstance().createProxy(invocation.proceed());
	}
}
