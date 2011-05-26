package com.wrenched.core.externalization;

import java.io.Externalizable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import com.wrenched.core.domain.ExternalizableMap;
import com.wrenched.core.externalization.Externalizer.Converter;
import com.wrenched.core.messaging.io.ExternalizableDecoratingPropertyProxy;

import flex.messaging.io.ArrayCollection;
import flex.messaging.io.PropertyProxyRegistry;

/**
 * BlazeDS-specific default externalization configuration
 * @author konkere
 *
 */
public class BlazeDSConfiguration extends DefaultConfiguration {
	public boolean useGAS3() {
		return true;
	}
	
	/**
	 * 
	 * @param targetClassNames
	 */
	public static void registerDecoratorsFor(String[] targetClassNames) {
		for (String className : targetClassNames) {
			try {
				registerDecoratorFor(className);
			}
			catch (ClassNotFoundException e) {
				continue;
			}
		}
	}

	/**
	 * 
	 * @param targetClasses
	 */
	public static void registerDecoratorsFor(Class[] targetClasses) {
		for (Class<?> targetClass : targetClasses) {
			registerDecoratorFor(targetClass);
		}
	}

	/**
	 * 
	 * @param targetClassName
	 * @throws ClassNotFoundException
	 */
	public static void registerDecoratorFor(String targetClassName) throws ClassNotFoundException {
		registerDecoratorFor(Class.forName(targetClassName));
	}

	/**
	 * 
	 * @param targetClass
	 */
	public static void registerDecoratorFor(Class<?> targetClass) {
		PropertyProxyRegistry.getRegistry().register(targetClass,
				new ExternalizableDecoratingPropertyProxy());
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
	}
}
