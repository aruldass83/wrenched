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
			setAttributeValue(this.target, f.getName(), getAttributeValue(source, f.getName()));
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
			if (source.getClass().isAssignableFrom(this.target.getClass())) {
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
			setAttributeValue(this.target, f.getName(), callback(f.getName(), getAttributeValue(source, f.getName())));
		}
	}
}
