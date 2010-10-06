package com.wrenched.core.messaging.io;

import com.wrenched.core.domain.EnumHolder;

import flex.messaging.io.BeanProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.util.ClassUtil;

/**
 * custom bean proxy to wrap enums
 * @author konkere
 *
 */
public class EnumPropertyProxy extends BeanProxy {
	public EnumPropertyProxy() {
		super();
	}
	
	public EnumPropertyProxy(final Enum<?> defaultValue) {
		this(new EnumHolder(defaultValue));
	}
	
	public EnumPropertyProxy(final EnumHolder defaultValue) {
		super(defaultValue);
	}
	
	/* Not-so-obvious name to overwrite alias */
    @Override
    protected String getClassName(final Object instance) {
    	if (instance instanceof EnumHolder) {
    		final EnumHolder enumHolder = (EnumHolder)instance;
    		return enumHolder.getEnumClass().getName();
    	}
    	return super.getClassName(instance);
    }
   
    @Override
	@SuppressWarnings("unchecked")
    public Object createInstance(final String className) {
        if ((className != null) && (className.length() > 0) &&
        		!className.startsWith(">") && !className.startsWith("flex.") &&
        		getSerializationContext().instantiateTypes) {
            final Class<?> desiredClass = ClassUtil.createClass(className);
            if (Enum.class.isAssignableFrom(desiredClass)) {
            	return new EnumHolder((Class<? extends Enum>)desiredClass);
            }
        }
       	return super.createInstance(className);
    }
}
