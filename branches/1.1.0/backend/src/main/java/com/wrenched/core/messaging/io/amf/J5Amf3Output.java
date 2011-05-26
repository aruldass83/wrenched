package com.wrenched.core.messaging.io.amf;

import java.io.IOException;

import java.util.IdentityHashMap;

import com.wrenched.core.annotations.Externalizable;
import com.wrenched.core.domain.EnumHolder;
import com.wrenched.core.domain.ExternalizableDecorator;
import com.wrenched.core.messaging.io.ExternalizableDecoratingPropertyProxy;

import flex.messaging.io.PropertyProxy;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;

public class J5Amf3Output extends Amf3Output {
	
	public J5Amf3Output(final SerializationContext context) { 
		super(context); 
	}
	
	@Override public void reset() {
		super.reset();
		enumTable.clear();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void writeObject(final Object o) throws IOException {
		if (o instanceof Enum) {
			final Enum<?> e = (Enum<?>)o;
			if (!enumTable.containsKey(e)) {
				enumTable.put(e, new EnumHolder(e));
			}
			super.writeObject(enumTable.get(e));
		}
		//k: just a way to distinguish the classes that have to be decorated for externalization
		//   without creating a dedicated registry just for that. will see how it goes.
		else if ((o != null) &&
				((PropertyProxyRegistry.getRegistry().getProxy(o.getClass()) instanceof ExternalizableDecoratingPropertyProxy) ||
						o.getClass().isAnnotationPresent(Externalizable.class))) {
			super.writeObject(ExternalizableDecorator.getInstance(o));
		}
		else {
			super.writeObject(o);
		}
	}
	
	final private IdentityHashMap<Enum<?>, EnumHolder> enumTable = new IdentityHashMap<Enum<?>, EnumHolder>();
}
