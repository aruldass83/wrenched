package com.wrenched.core.services.support;

import com.wrenched.core.annotations.LazyAttributeDomain;

public abstract class AbstractAttributeProvider extends MethodBasedAccessor implements LazyAttributeProvider {
	protected String domain;
	
	AbstractAttributeProvider() {
		if (this.getClass().isAnnotationPresent(LazyAttributeDomain.class)) {
			this.setDomain(getClass().getAnnotation(LazyAttributeDomain.class).value());
		}
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
}
