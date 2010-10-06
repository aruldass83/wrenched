package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * attribute provider implementation that is based on ORM. use it to
 * load entity attributes that are {@code FetchType.LAZY}. 
 * notice that {@code #loadAttribute(Class, Object, String, boolean)}
 * must be executed within a transaction.
 * @author konkere
 *
 */
public abstract class PersistenceBasedAttributeProvider extends AbstractAttributeProvider {
	private String loaderMethodName;
	private boolean fieldAccess = false;
	
	/**
	 * will check if configuration is correct
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (this.delegate != null && this.loaderMethodName != null) {
			this.delegate.getClass().getMethod(this.loaderMethodName, new Class[] {Class.class, Object.class});
		}
	}
	
	/**
	 * will try to load a persistent entity
	 * @param entityClass
	 * @param entityId
	 * @return
	 * @throws IllegalAccessException
	 */
	private Object load(Class<?> entityClass, Object entityId) throws IllegalAccessException {
		try {
			Method loader = this.delegate.getClass().getMethod(this.loaderMethodName, new Class[] {Class.class, Object.class});
			return loader.invoke(this.delegate, new Object[] {entityClass, entityId});
		}
		catch (NoSuchMethodException nsme) {
			return null;
		}
		catch (InvocationTargetException ite) {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#loadAttribute(java.lang.Class, java.lang.Object, java.lang.String, boolean)
	 */
	@Override
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName)
	throws IllegalAccessException {
		try {
			Object entity = this.load(entityClass, entityId);
			
			if (entity == null) {
				return null;
			}
			
			if (fieldAccess) {
				try {
					return this.getByFieldAccess(entityClass, entity, attributeName);
				}
				catch (NoSuchFieldException e) {
					throw new IllegalAccessException(e.getMessage());
				}
			}
			else {
				try {
					return this.getByMethodAccess(entityClass, entity, attributeName);
				}
				catch (NoSuchMethodException e) {
					throw new IllegalAccessException(e.getMessage());
				}
			}
		}
		catch (Exception dae) {
			return new Object();
		}
	}
	
	/**
	 * used to load a lazy attribute in case when the corresponding entity manager employs field access
	 * @param clazz entity class
	 * @param target entity instance
	 * @param attributeName lazy attribute to load
	 * @return
	 * @throws IllegalAccessException when the attribute has no legal accessors
	 * @throws NoSuchFieldException when an invalid attribute was specified
	 */
	private Object getByFieldAccess(Class<?> clazz, Object target, String attributeName) throws IllegalAccessException, NoSuchFieldException {
		try {
			return clazz.getDeclaredField(attributeName).get(target);
		}
		catch (IllegalArgumentException e) {
			//that's a bug really, should never happen
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (SecurityException e) {
			throw new IllegalAccessException(e.getMessage());
		}
	}
	
	/**
	 * used to load a lazy attribute in case when the corresponding entity manager uses property access.
	 * assumes that there's a public no-argument getter on the {@code attributeName} obeying Java
	 * naming convention
	 * @param clazz entity class
	 * @param target entity instance
	 * @param attributeName lazy attribute to load
	 * @return
	 * @throws IllegalAccessException when the attribute getter han no legal accessors
	 * @throws NoSuchMethodException when there's no available getter
	 */
	private Object getByMethodAccess(Class<?> clazz, Object target, String attributeName) throws IllegalAccessException, NoSuchMethodException {
		try {
			return clazz.getDeclaredMethod("get" + toUpperCaseFirst(attributeName), new Class[]{}).invoke(target, new Object[]{});
		}
		catch (IllegalArgumentException e) {
			//that's a bug really, should never happen
			throw new RuntimeException(e.getMessage(), e);
		}
		catch (SecurityException e) {
			throw new IllegalAccessException(e.getMessage());
		}
		catch (InvocationTargetException e) {
			//that's a bug really, should never happen
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static String toUpperCaseFirst(String s) {
		return String.valueOf(s.charAt(0)).toUpperCase().concat(s.substring(1, s.length()));
	}

	/**
	 * typical find-by-id method, that is usually provided by
	 * most ORM frameworks. must take two parameters: a class and an object.
	 * @param loaderMethodName
	 */
	public void setLoaderMethodName(String loaderMethodName) {
		this.loaderMethodName = loaderMethodName;
	}
	
	/**
	 * if provider has to use field or method access
	 * @param flag
	 */
	public void setFieldAccess(boolean flag) {
		this.fieldAccess = flag;
	}
}
