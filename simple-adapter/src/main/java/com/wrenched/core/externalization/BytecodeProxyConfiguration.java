package com.wrenched.core.externalization;

import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Factory;

import javassist.util.proxy.ProxyObject;

import com.wrenched.core.externalization.DefaultConfiguration;
import com.wrenched.core.externalization.Externalizer.Converter;
import com.wrenched.core.instrumentation.ProxyInstrumentor;
import com.wrenched.core.instrumentation.adapters.BytecodeProxyAdapter;

public class BytecodeProxyConfiguration extends DefaultConfiguration {
	private static final Converter<Object, Object> INSTRUMENTING_CONVERTER =
		new Converter<Object, Object>() {
			private final ProxyInstrumentor instrumentor = new BytecodeProxyAdapter();
	
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
		converters.put(ProxyObject.class, INSTRUMENTING_CONVERTER);
		converters.put(Proxy.class, INSTRUMENTING_CONVERTER);
		converters.put(Factory.class, INSTRUMENTING_CONVERTER);
	}
}
