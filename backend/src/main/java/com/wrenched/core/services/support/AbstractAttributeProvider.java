package com.wrenched.core.services.support;

import java.util.Collection;

import com.wrenched.core.annotations.LazyAttributeDomain;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.MetadataLoader;

public abstract class AbstractAttributeProvider extends MethodBasedAccessor implements LazyAttributeProvider {
	protected String domain;
	
	AbstractAttributeProvider() {
		this.setDomainByClass(this.getClass());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#getDomain()
	 */
	@Override
	public String getDomain() {
		return this.domain;
	}
	
	/**
	 * domain this provider manages
	 * @param domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	/**
	 * helper method to set domain from corresponding annotation
	 * @param clazz class to check
	 */
	protected void setDomainByClass(Class<?> clazz) {
		if (clazz.isAnnotationPresent(LazyAttributeDomain.class)) {
			this.setDomain(clazz.getAnnotation(LazyAttributeDomain.class).value());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#getManagedClasses()
	 */
	public final Collection<LazyAttributeRegistryDescriptor> getManagedClasses() {
		return MetadataLoader.getInstance().getManagedClasses(this.getDomain());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#setDelegate(java.lang.Object)
	 */
	@Override
	public void setDelegate(Object delegate) {
		super.setDelegate(delegate);
		this.setDomainByClass(delegate.getClass());
	}
}
