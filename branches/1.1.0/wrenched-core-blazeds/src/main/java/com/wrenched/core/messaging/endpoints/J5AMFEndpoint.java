package com.wrenched.core.messaging.endpoints;

import com.wrenched.core.domain.EnumHolder;
import com.wrenched.core.domain.ExternalizableDecorator;
import com.wrenched.core.externalization.BlazeDSConfiguration;
import com.wrenched.core.externalization.Externalizer;
import com.wrenched.core.messaging.io.EnumPropertyProxy;
import com.wrenched.core.messaging.io.amf.J5AmfMessageDeserializer;
import com.wrenched.core.messaging.io.amf.J5AmfMessageSerializer;

import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.io.PropertyProxyRegistry;
/**
 * Java endpoint that manages custom externalization.
 * must be configured in BlaseDS's services-config.xml
 * @author konkere
 *
 */
public class J5AMFEndpoint extends AMFEndpoint {
	static {
		BlazeDSConfiguration.registerDecoratorFor(ExternalizableDecorator.class);
		PropertyProxyRegistry.getRegistry().register(EnumHolder.class,
				new EnumPropertyProxy());
		PropertyProxyRegistry.getRegistry().register(Enum.class,
				new EnumPropertyProxy());
	}

	@Override
	protected String getDeserializerClassName() {
		return J5AmfMessageDeserializer.class.getCanonicalName();
	}

	@Override
	protected String getSerializerJava15ClassName() {
		return J5AmfMessageSerializer.class.getCanonicalName();
	}
}
