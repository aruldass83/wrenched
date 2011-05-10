package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.services.MetadataLoader;

/**
 * convenient method-invoking provider that would call certain methods to fetch lazy attributes,
 * instead of loading them directly from entities. 
 * 
 * @author konkere
 *
 */
public class MethodBasedAttributeProvider extends AbstractAttributeProvider {
	public static final String SEPARATOR = "#";
	
	@Override
	public void init() throws Exception {
		super.init();
		MetadataLoader.getInstance().loadClasses(this.getDomain(), this.delegate.getClass());
	}
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#loadAttribute(java.lang.Class, java.lang.Object, java.lang.String)
	 */
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName)
	throws IllegalAccessException {
		try {
			return this.access(entityClass.getCanonicalName() + SEPARATOR + attributeName, true, entityId);
		}
		catch (NoSuchMethodException nsme) {
			return new RuntimeException(nsme.getMessage(), nsme);
		}
		catch (InvocationTargetException ite) {
			return new RuntimeException(ite.getMessage(), ite);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#getManagedClasses()
	 */
	/**
	 * a map of loader methods per domain className#idName#attributeName that
	 * take a single parameter of type Object.
	 * class names will be prefixed using {@code LazyAttributeProvider#getDomain()}.
	 * @param ms
	 */
	public void setMethods(Map<String, String> ms) {
		for (String name : ms.keySet()) {
			String qualifiedName = this.getDomain() + "." + name;
			String[] z = qualifiedName.split(SEPARATOR);
			
			if (z.length == 2) {
				this.addMethod(qualifiedName, ms.get(name));
			}
			else if (z.length == 3) {
				this.addMethod(z[0] + SEPARATOR + z[2], ms.get(name));
			}
			else {
				throw new RuntimeException("fetcher [" + ms.get(name) + "] is not properly configured!");
			}
		}
	}
/*	
	@Override
	public void setDelegate(Object delegate) {
		super.setDelegate(delegate);
		
		for (Method m : this.delegate.getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(LazyAttributeFetcher.class)) {
				String className = m.getAnnotation(LazyAttributeFetcher.class).targetClass().getCanonicalName();
				
				if (className.startsWith(this.getDomain())) {
					this.methods.put(className +
							SEPARATOR + m.getAnnotation(LazyAttributeFetcher.class).attributeName(),
							m.getName());
				}
			}
		}
	}
*/	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#isSuitable(com.wrenched.core.annotations.LazyAttributeFetcher, java.lang.reflect.Method)
	 */
	@Override
	protected boolean isDeclarationSuitable(LazyAttributeFetcher metadata, Method m) {
		return metadata.targetClass().getCanonicalName().startsWith(this.getDomain());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#getKey(com.wrenched.core.annotations.LazyAttributeFetcher, java.lang.reflect.Method)
	 */
	@Override
	protected String getKey(LazyAttributeFetcher metadata, Method m) {
		return metadata.targetClass().getCanonicalName() + SEPARATOR + metadata.attributeName();
	}
}
