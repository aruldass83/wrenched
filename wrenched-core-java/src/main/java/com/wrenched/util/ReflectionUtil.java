package com.wrenched.util;

import java.lang.reflect.Field;
import java.util.Collection;

import com.wrenched.core.services.support.ClassIntrospectionUtil.Operation;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

public class ReflectionUtil {
	private static class CopyOperation extends Operation<Field> {
		final Object target;
		final boolean withSort;
		
		public CopyOperation(Object arg0, boolean arg1) {
			this.target = arg0;
			this.withSort = arg1;
		}
		@Override
		protected void process(Object source, Field f) throws IllegalAccessException {
			setAttributeValue(this.target, f, getAttributeValue(source, f));
		}

		@Override
		protected boolean isRelevant(Field f) {
			return true;
		}

		@Override
		public Field[] getFields(Object source) {
			Collection<Field> result = getAllFields(source.getClass(), this.withSort);
			return result.toArray(new Field[result.size()]);
		}
		
		@Override
		public void introspect(Object source) {
			if (!source.getClass().isAssignableFrom(this.target.getClass())) {
				throw new IllegalArgumentException("");
			}
			
			super.introspect(source);
		}
	}
	
	public abstract static class CallbackCopyOperation extends CopyOperation {
		public CallbackCopyOperation(Object arg0, boolean arg1) {
			super(arg0, arg1);
		}
		
		protected abstract Object callback(String attributeName, Object attributeValue);

		@Override
		protected void process(Object source, Field f) throws IllegalAccessException {
			setAttributeValue(this.target, f, callback(f.getName(), getAttributeValue(source, f)));
		}
	}
	
	public static void setProxyAttributeValue(Object proxy, String className, String attributeName, Object value) {
		try {
			setAttributeValue(proxy, Class.forName(className).getDeclaredField(attributeName), value);
		}
		catch (NoSuchFieldException nsfe) {
		}
		catch (ClassNotFoundException e) {
		}
	}

	public static void setAttributeValue(Object target, String attributeName, Object value) {
		try {
			setAttributeValue(target, target.getClass().getDeclaredField(attributeName), value);
		}
		catch (NoSuchFieldException nsfe) {
			System.out.println();
		}
	}
	
	private static void setAttributeValue(Object target, Field attribute, Object value) {
		try {
			attribute.setAccessible(true);
			attribute.set(target, value);
		}
		catch (IllegalAccessException iae) {
			System.out.println();
		}
	}

	public static Object getProxyAttributeValue(Object proxy, String className, String attributeName) {
		try {
			return getAttributeValue(proxy, Class.forName(className).getDeclaredField(attributeName));
		}
		catch (NoSuchFieldException nsfe) {
			return null;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Object getAttributeValue(Object target, String attributeName) {
		try {
			return getAttributeValue(target, target.getClass().getDeclaredField(attributeName));
		}
		catch (NoSuchFieldException nsfe) {
			return null;
		}
	}

	private static Object getAttributeValue(Object target, Field attribute) {
		try {
			attribute.setAccessible(true);
			return attribute.get(target);
		}
		catch (IllegalAccessException iae) {
			return null;
		}
	}
	
}
