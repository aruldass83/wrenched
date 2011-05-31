package com.wrenched.core.services;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.wrenched.core.lazy.LazyAttributeRegistry;

/**
 * convenience generic wrapper over Spring-remoting client factory beans
 * that adds lazy-loading functionality
 * @author konkere
 *
 */
public class ServiceProxyFactoryBeanWrapper extends AbstractFactoryBean implements MethodInterceptor {
	private Object delegate;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
	 */
	protected Object createInstance() throws Exception {
		ProxyFactory pf = new ProxyFactory(getObjectType(),
					new SingletonTargetSource(delegate) {
						@Override
						public Class getTargetClass() {
							return getObjectType();
						}
					});
		pf.addAdvice(this);
		return pf.getProxy();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		//FIXME: see how it works
		return delegate.getClass().getInterfaces()[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		return LazyAttributeRegistry.getInstance().createProxy(invocation.proceed());
	}
	
	public void setDelegate(Object arg0) {
		this.delegate = arg0;
	}
}
