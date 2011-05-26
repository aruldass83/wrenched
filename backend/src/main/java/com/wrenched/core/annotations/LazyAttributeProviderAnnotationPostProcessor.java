package com.wrenched.core.annotations;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.wrenched.core.services.LazyAttributeLoader;
import com.wrenched.core.services.support.AbstractAttributeProvider;
import com.wrenched.core.services.support.LazyAttributeProvider;
import com.wrenched.core.services.support.MethodBasedAttributeProvider;
import com.wrenched.core.services.support.PersistenceBasedAttributeProvider;

import java.util.Arrays;

public class LazyAttributeProviderAnnotationPostProcessor implements InstantiationAwareBeanPostProcessor, PriorityOrdered, BeanFactoryAware, Serializable {
	private int order = Ordered.LOWEST_PRECEDENCE - 4;
	private transient ListableBeanFactory beanFactory;

	public void setOrder(int order) {
	  this.order = order;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
	  return this.order;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ListableBeanFactory) {
			this.beanFactory = (ListableBeanFactory) beanFactory;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		if (bean.getClass().isAnnotationPresent(com.wrenched.core.annotations.LazyAttributeProvider.class)) {
			Map<String, LazyAttributeLoader> loaders = this.beanFactory.getBeansOfType(LazyAttributeLoader.class);
			AbstractAttributeProvider lap = null;
			
			switch (bean.getClass().getAnnotation(com.wrenched.core.annotations.LazyAttributeProvider.class).value()) {
			case METHOD: lap = new MethodBasedAttributeProvider(); break;
			case PERSISTENCE: lap = new PersistenceBasedAttributeProvider(); break;
			}
			
			lap.setDelegate(bean);

			try {
				lap.init();
			}
			catch (Exception e) {
				throw new BeanInitializationException("can't initialize provider", e);
			}
			
			for (LazyAttributeLoader lal : loaders.values()) {
				lal.setProviders(Arrays.asList(new LazyAttributeProvider[]{lap}));
			}
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
	 * #postProcessPropertyValues(org.springframework.beans.PropertyValues,
	 * java.beans.PropertyDescriptor[], java.lang.Object, java.lang.String)
	 */
	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
		return pvs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
