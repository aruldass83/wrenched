package com.wrenched.core.instrumentation.adapters;

import org.hibernate.collection.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor;

/**
 * convenient adapter implementation for Hibernate
 * @author konkere
 *
 */
public class HibernateProxyAdapter extends AbstractUnwrappingInstrumentor {
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#isSimpleProxy(java.lang.Object)
	 */
	@Override
	public boolean isSimpleProxy(Object o) {
		return o instanceof HibernateProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#isCollectionProxy(java.lang.Object)
	 */
	@Override
	public boolean isCollectionProxy(Object o) {
		return o instanceof PersistentCollection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.instrumentation.AbstractUnwrappingInstrumentor#getProxyTarget(java.lang.Object)
	 */
	@Override
	public Object getProxyTarget(Object o) {
		return ((HibernateProxy)o).getHibernateLazyInitializer().getImplementation();
	}
}
