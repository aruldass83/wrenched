package com.wrenched.core.externalization.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.wrenched.core.annotations.Externalizable;
import com.wrenched.core.domain.ExternalizableDecorator;

public class ExternalizingObjectOutputStream extends ObjectOutputStream {

	public ExternalizingObjectOutputStream(OutputStream out) throws IOException {
		super(out);
		super.enableReplaceObject(true);
	}

	@Override
	protected void annotateClass(Class<?> cl) throws IOException {
		this.writeBoolean(cl.isAnnotationPresent(Externalizable.class));
	}

	@Override
	protected Object replaceObject(Object obj) throws IOException {
		if (obj.getClass().isAnnotationPresent(Externalizable.class)) {
//			return ExternalizableDecorator.proxy(obj);
			return ExternalizableDecorator.getInstance(obj);
		}
		
		return super.replaceObject(obj);
	}
}
