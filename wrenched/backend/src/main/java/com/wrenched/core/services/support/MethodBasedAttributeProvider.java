package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

/**
 * convenient method-invoking provider that would call certain methods to fetch lazy attributes,
 * instead of loading them directly from entities. 
 * 
 * @author konkere
 *
 */
public class MethodBasedAttributeProvider extends AbstractAttributeProvider {
	public static final String SEPARATOR = "#";
	
	private final Map<String, String> managedDefinitions = new HashMap<String, String>();
	
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
	public Collection<LazyAttributeRegistryDescriptor> getManagedClasses() {
		Map<String, LazyAttributeRegistryDescriptor> classes =
			new HashMap<String, LazyAttributeRegistryDescriptor>();
		
		for (String qualifiedName : this.methods.keySet()) {
			String[] z = qualifiedName.split(SEPARATOR);

			if (!classes.containsKey(z[0])) {
				classes.put(z[0], new LazyAttributeRegistryDescriptor());
			}
			
			classes.get(z[0]).className = z[0];
			classes.get(z[0]).idName = this.managedDefinitions.get(z[0]);
			
			if (classes.get(z[0]).attributes == null) {
				classes.get(z[0]).attributes = new ArrayList<String>();
			}
			
			classes.get(z[0]).attributes.add(z[2]);
		}

		return classes.values();
	}

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
				this.managedDefinitions.put(z[0], LazyAttributeFetcher.SELF);
				this.methods.put(qualifiedName, ms.get(name));
			}
			else if (z.length == 3) {
				this.managedDefinitions.put(z[0], z[1]);
				this.methods.put(z[0] + SEPARATOR + z[2], ms.get(name));
			}
			else {
				throw new RuntimeException("fetcher [" + ms.get(name) + "] is not properly configured!");
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.AbstractAttributeProvider#setDelegate(java.lang.Object)
	 */
	public void setDelegate(Object delegate) {
		super.setDelegate(delegate);

		for (Method m : this.delegate.getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(LazyAttributeFetcher.class)) {
				String className = m.getAnnotation(LazyAttributeFetcher.class).targetClass().getCanonicalName();
				
				if (className.startsWith(this.getDomain())) {
					this.managedDefinitions.put(className, m.getAnnotation(LazyAttributeFetcher.class).idName());
					this.methods.put(className +
							SEPARATOR + m.getAnnotation(LazyAttributeFetcher.class).attributeName(),
							m.getName());
				}
			}
		}
	}
}
