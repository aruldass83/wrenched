package com.wrenched.core.services;

import java.util.Collection;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

public interface LazyAttributeLoaderService {

	/**
	 * loads an {@code attributeName} which is declared lazy-loaded on {@code entityName} and returns an
	 * externalizable wrapper to be transferred outside. 
	 * @param entityName
	 * @param entityId
	 * @param attributeName
	 * @return
	 * @throws IllegalAccessException
	 */
	public LazyAttribute loadAttribute(String entityName, Object entityId,
			String attributeName) throws IllegalAccessException;

	public Collection<LazyAttributeRegistryDescriptor> getManagedClasses();

}