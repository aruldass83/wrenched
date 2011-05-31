package com.wrenched.core.externalization;

import java.util.HashMap;
import java.util.Map;

import com.wrenched.core.externalization.Externalizer.Configuration;
import com.wrenched.core.externalization.Externalizer.Converter;
import com.wrenched.core.services.support.ClassIntrospectionUtil.HierarchyTraverser;

/**
 * 
 * convenient superclass for Externalizer configurations that provides means
 * of searching converters
 * @author konkere
 *
 */
public abstract class AbstractConfiguration implements Configuration {
	protected static final Map<Class, Converter> converters = new HashMap<Class, Converter>();

	private static final HierarchyTraverser<Converter> TRAVERSER = new HierarchyTraverser<Converter>() {
		@Override
		protected Converter processMatch(Class clazz) {
			return converters.get(clazz);
		}
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.externalization.Externalizer.Configuration#findConverter(java.lang.Class)
	 */
	public Converter findConverter(Class clazz) {
		return TRAVERSER.traverse(clazz);
	}
}