package com.wrenched.core.messaging.io.amf;

import java.io.IOException;

import com.wrenched.core.domain.EnumHolder;
import com.wrenched.core.domain.ExternalizableDecorator;
import com.wrenched.core.domain.ExternalizableMap;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;

public class J5Amf3Input extends Amf3Input {

	public J5Amf3Input(final SerializationContext context) {
		super(context);
	}
	
    @Override
	@SuppressWarnings("unchecked")
	public Object readObject() throws ClassNotFoundException, IOException {
    	final Object value = super.readObject();
    	if (value instanceof EnumHolder) {
    		final EnumHolder enumHolder = (EnumHolder)value;
    		return enumHolder.enumValue();
    	}
    	else if (value instanceof ExternalizableDecorator) {
    		ExternalizableDecorator d = (ExternalizableDecorator) value;
    		return d.getDelegate()/*d.proxy()*/;
    	}
    	else if (value instanceof ExternalizableMap) {
    		return ((ExternalizableMap) value).toMap();
    	}
    	else {
    		return value;
    	}
    }
}
