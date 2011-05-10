package com.wrenched.core.services;

import static com.wrenched.core.services.support.ClassIntrospectionUtil.findClasses;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.support.MethodBasedAttributeProvider;

/**
 * 
 * convenient domain metadata scanner that sustains declaration registry for
 * different purposes 
 * @author konkere
 *
 */
public class MetadataLoader {
	private final Map<String, LazyAttributeRegistryDescriptor> managedDefinitions =
		new HashMap<String, LazyAttributeRegistryDescriptor>();

	private static MetadataLoader instance;
	
	public static MetadataLoader getInstance() {
		if (instance == null) {
			instance = new MetadataLoader();
		}
		
		return instance;
	}
	
	public Collection<LazyAttributeRegistryDescriptor> getManagedClasses() {
		return managedDefinitions.values();
	}
	
	public Collection<LazyAttributeRegistryDescriptor> getManagedClasses(String domain) {
		Collection<LazyAttributeRegistryDescriptor> defs =
			new ArrayList<LazyAttributeRegistryDescriptor>();
		
		for (String className : managedDefinitions.keySet()) {
			if (className.startsWith(domain)) {
				defs.add(managedDefinitions.get(className));
			}
		}
		
		return defs;
	}
	
	public LazyAttributeRegistryDescriptor getManagedClass(Class clazz) {
		return this.managedDefinitions.get(clazz.getCanonicalName());
	}
	
	public void loadClasses(String domain, boolean fieldAccess) {
		for (LazyAttributeRegistryDescriptor def : findClasses(domain, fieldAccess)) {
			managedDefinitions.put(def.className, def);
		}
	}
	
	public void loadClasses(String domain, Map<String, String> metadata) {
		for (String name : metadata.keySet()) {
			String[] z = (domain + "." + name).split(MethodBasedAttributeProvider.SEPARATOR);
			String className = z[0];

			if (!managedDefinitions.containsKey(className)) {
				managedDefinitions.put(className, new LazyAttributeRegistryDescriptor());
			}
			
			LazyAttributeRegistryDescriptor d = managedDefinitions.get(className);
			d.className = className;
			
			if (d.attributes == null) {
				d.attributes = new ArrayList<String>();
			}
			
			if (z.length == 2) {
				d.idName = LazyAttributeFetcher.SELF;
			}
			else if (z.length == 3) {
				d.idName = z[1];
				d.attributes.add(z[2]);
			}
			else {
				throw new RuntimeException("fetcher [" + metadata.get(name) + "] is not properly configured!");
			}
		}
	}

	public void loadClasses(String domain, Class<?> targetClass) {
		for (Method m : targetClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(LazyAttributeFetcher.class)) {
				String className = m.getAnnotation(LazyAttributeFetcher.class).targetClass().getCanonicalName();
				
				if (className.startsWith(domain)) {
					String attributeName = m.getAnnotation(LazyAttributeFetcher.class).attributeName();
					
					if (!managedDefinitions.containsKey(className)) {
						managedDefinitions.put(className, new LazyAttributeRegistryDescriptor());
					}
					
					LazyAttributeRegistryDescriptor d = managedDefinitions.get(className);
					d.className = className;
					d.idName = m.getAnnotation(LazyAttributeFetcher.class).idName();

					if (d.attributes == null) {
						d.attributes = new ArrayList<String>();
					}
					
					d.attributes.add(attributeName);
				}
			}
		}
	}
}
