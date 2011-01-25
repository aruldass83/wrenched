package com.wrenched.core.messaging.io.amf;

import java.io.IOException;

import com.wrenched.core.annotations.Externalizable;
import com.wrenched.core.domain.EnumHolder;
import com.wrenched.core.domain.ExternalizableDecorator;
import com.wrenched.core.domain.ExternalizableMap;
import com.wrenched.core.externalization.Externalizer;

import flex.messaging.io.AbstractProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Input;
import flex.messaging.io.amf.TraitsInfo;

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
    
    @Override
    @SuppressWarnings("unchecked")
    protected TraitsInfo readTraits(int ref) throws IOException {
    	TraitsInfo ti = super.readTraits(ref);
    	
        Class<?> desiredClass = AbstractProxy.getClassFromClassName(ti.getClassName(),
        		context.createASObjectForMissingType);

    	if (desiredClass.isAnnotationPresent(Externalizable.class)) {
    		Externalizer.registerDecoratorFor(desiredClass);
    		//k: just to be sure
    		ti = new TraitsInfo(ti.getClassName(), ti.isDynamic(), true, ti.getProperties());
    		traitsTable.add(ti);
    	}
    	
    	return ti;
    }
    
    @Override
    protected void readExternalizable(String className, Object object) throws ClassNotFoundException, IOException {
        Class<?> desiredClass = AbstractProxy.getClassFromClassName(className,
        		context.createASObjectForMissingType);
        
		super.readExternalizable(className,
				desiredClass.isAnnotationPresent(Externalizable.class) && !(object instanceof ExternalizableDecorator) ?
						ExternalizableDecorator.getInstance(object) :
							object);
    }
}
