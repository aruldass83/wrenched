package com.wrenched.core.externalization;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wrenched.core.externalization.Externalizer.Converter;

/**
 * default externalization configuration, which covers most of the
 * out-of-box type conversion cases. 
 * @author konkere
 *
 */
public class DefaultConfiguration extends AbstractConfiguration {
	public boolean useGAS3() {
		return false;
	}

	static {
		converters.put(ArrayList.class, new Converter<List, Set>() {
			public Set convert(List source,  Class<? extends Set> returnType) {
				return new HashSet(source);
			}
		});
		converters.put(HashSet.class, new Converter<Set, List>() {
			public List convert(Set source,  Class<? extends List> returnType) {
				return new ArrayList(source);
			}
		});
		converters.put(Object[].class, new Converter<Object[], Object>() {
			public Object convert(Object[] source,  Class<?> returnType) {
				if (returnType.isArray()) {
					return Array.newInstance(returnType.getComponentType(), source.length);
				}
				else {
					return source;
				}
			}
		});
		converters.put(Object.class, PRESERVING_CONVERTER);
	}
}
