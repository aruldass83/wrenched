package com.wrenched.core.externalization.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import com.wrenched.core.domain.ExternalizableDecorator;

public class ExternalizingObjectInputStream extends ObjectInputStream {

	public ExternalizingObjectInputStream(InputStream in) throws IOException {
		super(in);
		super.enableResolveObject(true);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,	ClassNotFoundException {
		return this.readBoolean() ? ExternalizableDecorator.class : super.resolveClass(desc);
	}

	@Override
	protected Object resolveObject(Object obj) throws IOException {
		if (ExternalizableDecorator.class.isAssignableFrom(obj.getClass())) {
			return ((ExternalizableDecorator)obj).getDelegate();
		}
		return super.resolveObject(obj);
	}
}
