package com.wrenched.core.services.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.services.MetadataLoader;

import static com.wrenched.core.services.support.ClassIntrospectionUtil.toUpperCaseFirst;

/**
 * attribute provider implementation that is based on ORM. use it to
 * load entity attributes that are {@code FetchType.LAZY}. 
 * notice that {@code #loadAttribute(Class, Object, String, boolean)}
 * must be executed within a transaction.
 * @author konkere
 *
 */
public class PersistenceBasedAttributeProvider extends AbstractAttributeProvider {
	private static final String DEFAULT = "DEFAULT";
	private boolean fieldAccess = false;
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#init()
	 */
	public void init() throws Exception {
		super.init();
		MetadataLoader.getInstance().loadClasses(this.getDomain(), this.fieldAccess);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.LazyAttributeProvider#loadAttribute(java.lang.Class, java.lang.Object, java.lang.String, boolean)
	 */
	@Override
	public Object loadAttribute(Class<?> entityClass, Object entityId, String attributeName)
	throws IllegalAccessException {
		try {
			Object entity = this.access(DEFAULT, false, entityClass, entityId);
			
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

	/**
	 * typical find-by-id method, that is usually provided by
	 * most ORM frameworks. must take two parameters: a class and an object.
	 * @param loaderMethodName
	 */
	public void setLoaderMethodName(String loaderMethodName) {
		this.addMethod(DEFAULT, loaderMethodName);
	}
	
	/**
	 * if provider has to use field or method access
	 * @param flag
	 */
	public void setFieldAccess(boolean flag) {
		this.fieldAccess = flag;
	}
	/*	
	@Override
	public void setDelegate(Object delegate) {
		super.setDelegate(delegate);
		
		for (Method m : this.delegate.getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(LazyAttributeFetcher.class)) {
				if ((m.getParameterTypes().length == 2) &&
						(m.getParameterTypes()[0]).equals(Class.class) &&
						(m.getParameterTypes()[1]).equals(Object.class)) {
					this.setLoaderMethodName(m.getName());
					break;
				}
			}
		}
	}
*/

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#isDeclarationSuitable(com.wrenched.core.annotations.LazyAttributeFetcher, java.lang.reflect.Method)
	 */
	@Override
	protected boolean isDeclarationSuitable(LazyAttributeFetcher metadata, Method m) {
		return (m.getParameterTypes().length == 2) &&
				(m.getParameterTypes()[0]).equals(Class.class) &&
				(m.getParameterTypes()[1]).equals(Object.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.MethodBasedAccessor#getKey(com.wrenched.core.annotations.LazyAttributeFetcher, java.lang.reflect.Method)
	 */
	@Override
	protected String getKey(LazyAttributeFetcher metadata, Method m) {
		return DEFAULT;
	}
}
