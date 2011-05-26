package com.wrenched.core.messaging.io;

import com.wrenched.core.domain.ExternalizableDecorator;

import flex.messaging.io.BeanProxy;
import flex.messaging.util.ClassUtil;

/**
 * custom bean proxy for wrapping ExternalizableDecorators
 * @see ExternalizableDecorator
 * @author konkere
 *
 */
public class ExternalizableDecoratingPropertyProxy extends BeanProxy {
    public ExternalizableDecoratingPropertyProxy() {
    	super();
	}

	public ExternalizableDecoratingPropertyProxy(final Object defaultValue) {
		this(ExternalizableDecorator.getInstance(defaultValue));
	}
	
	public ExternalizableDecoratingPropertyProxy(final ExternalizableDecorator<?> defaultValue) {
		super(defaultValue);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected String getClassName(final Object instance) {
    	if (instance instanceof ExternalizableDecorator) {
    		final ExternalizableDecorator decorator = (ExternalizableDecorator)instance;
    		return decorator.getDelegateClass().getName();
    	}
    	else {
    		return super.getClassName(instance);
    	}
    }
   
    @Override
	@SuppressWarnings("unchecked")
	public Object createInstance(final String className) {
        if ((className != null) && (className.length() > 0) &&
        		!className.startsWith(">") && !className.startsWith("flex.") &&
        		getSerializationContext().instantiateTypes) {
        	return ExternalizableDecorator.getInstance(ClassUtil.createClass(className));
        }
        else {
        	return super.createInstance(className);
        }
    }
}
