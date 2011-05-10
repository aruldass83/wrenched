package com.wrenched.core.externalization;

import org.hibernate.collection.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.wrenched.core.externalization.DefaultConfiguration;
import com.wrenched.core.externalization.Externalizer.Converter;
import com.wrenched.core.instrumentation.ProxyInstrumentor;
import com.wrenched.core.instrumentation.adapters.HibernateProxyAdapter;

public class HibernateConfiguration extends DefaultConfiguration {
	static {
		converters.put(HibernateProxy.class,
				new Converter<HibernateProxy, Object>() {
					private final ProxyInstrumentor instrumentor = 
						new HibernateProxyAdapter();

					public Object convert(HibernateProxy source, Class<?> returnType) {
						try {
							return instrumentor.unwrap(source);
						}
						catch (NoSuchFieldException nsfe) {
							return null;
						}
					}
				});
		converters.put(PersistentCollection.class,
				new Converter<PersistentCollection, Object>() {
					private final ProxyInstrumentor instrumentor = 
						new HibernateProxyAdapter();

					public Object convert(PersistentCollection source, Class<?> returnType) {
						try {
							return instrumentor.unwrap(source);
						}
						catch (NoSuchFieldException nsfe) {
							return null;
						}
					}
				});
	}
}
