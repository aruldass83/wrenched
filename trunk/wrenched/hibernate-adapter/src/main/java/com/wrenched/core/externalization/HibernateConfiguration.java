package com.wrenched.core.externalization;

import org.hibernate.collection.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;

import com.wrenched.core.externalization.DefaultConfiguration;
import com.wrenched.core.externalization.Externalizer.Converter;
import com.wrenched.core.instrumentation.ProxyInstrumentor;
import com.wrenched.core.instrumentation.adapters.HibernateProxyAdapter;

public class HibernateConfiguration extends DefaultConfiguration {
	private static final Converter<Object, Object> INSTRUMENTING_CONVERTER = 
		new Converter<Object, Object>() {
			private final ProxyInstrumentor instrumentor = 
				new HibernateProxyAdapter();
	
			public Object convert(Object source, Class<?> returnType) {
				try {
					return instrumentor.unwrap(source);
				}
				catch (NoSuchFieldException nsfe) {
					return null;
				}
			}
		};

	static {
		converters.put(HibernateProxy.class, INSTRUMENTING_CONVERTER);
		converters.put(PersistentCollection.class, INSTRUMENTING_CONVERTER);
	}
}
