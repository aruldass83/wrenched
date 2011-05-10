package com.wrenched.core.externalization;

import java.io.Externalizable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wrenched.core.domain.ExternalizableMap;
import com.wrenched.core.externalization.Externalizer.Converter;

import flex.messaging.io.ArrayCollection;

/**
 * default externalization configuration, which covers most of the
 * out-of-box type conversion cases. 
 * @author konkere
 *
 */
public class DefaultConfiguration extends AbstractConfiguration {
	public boolean useGAS3() {
		return true;
	}

	static {
		converters.put(AbstractMap.class, new Converter<Map, Externalizable>() {
			public Externalizable convert(Map source, Class<? extends Externalizable> returnType) {
				return ExternalizableMap.newInstance(source);
			}
		});
		converters.put(ExternalizableMap.class, new Converter<ExternalizableMap, Map>() {
			public Map convert(ExternalizableMap source, Class<? extends Map> returnType) {
				return source.toMap();
			}
		});
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
		converters.put(ArrayCollection.class, new Converter<ArrayCollection, Object>() {
			public Object convert(ArrayCollection source,  Class<?> returnType) {
				if (returnType.isArray()) {
					return source.toArray((Object[])Array.newInstance(returnType.getComponentType(), source.size()));
				}
				if (returnType.equals(List.class)) {
					return new ArrayList(source);
				}
				else if (returnType.equals(Set.class)) {
					return new HashSet(source);
				}
				else {
					return source;
				}
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
		converters.put(Float.class, new Converter<Float, Number>() {
			public Number convert(Float source , Class<? extends Number> returnType) {
				if (Float.compare(Float.NaN, source.floatValue()) != 0 ){
					return new Float(source.floatValue());
				}else{
					return null;
				}
			}
		});
		converters.put(Double.class, new Converter<Double, Number>() {
			public Number convert(Double source , Class<? extends Number> returnType) {
				if (Double.compare(Double.NaN, source.doubleValue()) != 0 ){
					return new Double(source.doubleValue());
				}else{
					return null;
				}
			}
		});
		converters.put(BigDecimal.class, new Converter<Float, Number>() {
			public Number convert(Float source , Class<? extends Number> returnType) {
				if (Double.compare(Double.NaN, source.doubleValue()) != 0 ){
					return new BigDecimal(source.doubleValue());
				}else{
					return null;
				}
			}
		});
		converters.put(Number.class, new Converter<Number, Number>() {
			public Number convert(Number source , Class<? extends Number> returnType) {
				if (returnType.equals(BigDecimal.class) ){
					System.out.println(source.doubleValue());
					if (Double.compare(Double.NaN, source.doubleValue()) != 0 ){
						return new BigDecimal(source.doubleValue());
					}else{
						return null;
					}
				}
				if (returnType.equals(Double.class) ){
					if (Double.compare(Double.NaN, source.doubleValue()) != 0 ){
						return new Double(source.doubleValue());
					}else{
						return null;
					}
				}
				if (returnType.equals(Float.class) ){
					if (Float.compare(Float.NaN, source.floatValue()) != 0 ){
						return new Float(source.floatValue());
					}else{
						return null;
					}
				}
				return  source.intValue();
			}
		});
		converters.put(Object.class, PRESERVING_CONVERTER);
	}
}
