package com.wrenched.core.externalization;

import java.util.HashMap;
import java.util.Map;

import com.wrenched.core.externalization.Externalizer.Configuration;
import com.wrenched.core.externalization.Externalizer.Converter;

/**
 * 
 * convenient superclass for Externalizer configurations that provides means
 * of searching converters
 * @author konkere
 *
 */
public abstract class AbstractConfiguration implements Configuration {
	protected static final Map<Class, Converter> converters = new HashMap<Class, Converter>();

	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.externalization.Externalizer.Configuration#findConverter(java.lang.Class)
	 */
	public Converter findConverter(Class clazz) {
		/*
		Converter c = null;
		Class tmp = clazz;
		
		while (c == null && tmp != null) {
			c = converters.get(tmp);
			tmp = tmp.getSuperclass();
		}
		
		return c;
		*/
		
		return traverseSuperclass(clazz);
	}

	/**
	 * iteratively searches for a converter by going up the superclasses
	 * @param clazz
	 * @return
	 */
	protected Converter traverseSuperclass(Class clazz) {
		Converter c = null;
		Class tmp = clazz;
		
		while (c == null && tmp != null) {
			c = traverseInterfaces(tmp);
			tmp = tmp.getSuperclass();
		}
		
		return c;
	}

	/**
	 * recursively searches for a converter amongst the
	 * tree of interfaces. note that there's no particular order
	 * of interfaces and therefore no guaranty of finding a particular one
	 * if several converters potentially exist.
	 * @param clazz
	 * @return
	 */
	protected Converter traverseInterfaces(Class clazz) {
		Converter c = converters.get(clazz);
		
		if (c != null) {
			return c;
		}
		
		Class[] is = clazz.getInterfaces();
		
		for (int i = 0; i < is.length && c == null; i++) {
			c = traverseInterfaces(is[i]);
		}
		
		return c;
	}
}