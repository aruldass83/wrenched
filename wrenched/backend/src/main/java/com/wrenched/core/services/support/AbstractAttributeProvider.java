package com.wrenched.core.services.support;

public abstract class AbstractAttributeProvider implements LazyAttributeProvider {
	protected Object delegate;
	protected String domain;
	
	protected abstract void init() throws Exception;
	
	/**
	 * persistent DAO used to load entities
	 * @param delegate
	 */
	public void setDelegate(Object delegate) {
		this.delegate = delegate;
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
