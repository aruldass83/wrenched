package com.wrenched.core.services.support;

import java.util.Collection;

import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

/**
 * LAL provider that will "provide" a lazy attribute value of an entity
 * from a certain domain
 * @author konkere
 *
 */
public interface LazyAttributeProvider {
	/**
	 * @param entityClass
	 * @param entityId
	 * @param attributeName
	 * @return value of a lazy {@code attributeName} on an entity instance of {@code entityClass} identified by {@code entityId}
	 * @throws IllegalAccessException if the attribute is inaccessible or han no legal accessors
	 */
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName) throws IllegalAccessException;
	
	/**
	 * @return name of a package that contains entities this provider manages
	 */
	public String getDomain();
	
	/**
	 * @return collection of class descriptors that this provider manages
	 */
	public Collection<LazyAttributeRegistryDescriptor> getManagedClasses();
}
