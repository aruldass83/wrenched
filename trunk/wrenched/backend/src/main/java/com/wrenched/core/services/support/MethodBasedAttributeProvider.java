package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * convenient method-invoking provider that would call certain methods to fetch lazy attributes,
 * instead of loading them directly from entities. 
 * 
 * @author konkere
 *
 */
public abstract class MethodBasedAttributeProvider extends AbstractAttributeProvider {
	public static final String SEPARATOR = "#";
	private final Map<String, String> methods = new HashMap<String, String>();
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#loadAttribute(java.lang.Class, java.lang.Object, java.lang.String)
	 */
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName)
	throws IllegalAccessException {
		String methodName = methods.get(entityClass.getCanonicalName() + SEPARATOR + attributeName);
		try {
			Method method = this.delegate.getClass().getMethod(methodName, new Class[]{entityId.getClass()});
			return method.invoke(this.delegate,	new Object[]{entityId});
		}
		catch (NoSuchMethodException nsme) {
			return new RuntimeException(nsme.getMessage(), nsme);
		}
		catch (InvocationTargetException ite) {
			return new RuntimeException(ite.getMessage(), ite);
		}
	}
	
	public void init() throws Exception {
		if (this.delegate != null && !this.methods.isEmpty()) {
			for (Method m : this.delegate.getClass().getMethods()) {
				assert this.methods.values().contains(m.getName());
				assert (m.getParameterTypes().length == 1);
			}
		}
	}

	/**
	 * a map of loader methods per domain className#attributeName that
	 * take a single parameter of type Object.
	 * class names will be prefixed using {@code LazyAttributeProvider#getDomain()}.
	 * @param ms
	 */
	public void setMethods(Map<String, String> ms) {
		for (String name : ms.keySet()) {
			this.methods.put(this.getDomain() + "." + name, ms.get(name));
		}
	}
}
