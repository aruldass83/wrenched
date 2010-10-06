package com.wrenched.core.services.support;

/**
 * LAL provider that will "provide" a lazy attribute value of an entity
 * from a certain domain
 * @author konkere
 *
 */
public interface LazyAttributeProvider {
	/**
	 * returns a value of a lazy {@code attributeName} on an entity instance of {@code entityClass} identified by {@code entityId}
	 * @param entityClass
	 * @param entityId
	 * @param attributeName
	 * @return
	 * @throws IllegalAccessException if the attribute is inaccessible or han no legal accessors
	 */
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName) throws IllegalAccessException;
	
	/**
	 * name of a package that contains entities this provider manages
	 * @return
	 */
	public String getDomain();
}
