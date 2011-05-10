package com.wrenched.core.externalization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wrenched.core.messaging.io.ExternalizableDecoratingPropertyProxy;

import flex.messaging.io.PropertyProxyRegistry;

/**
 * Externalization support for AMF
 * @author konkere
 *
 */
public class Externalizer {
	private static final Log logger = LogFactory.getLog(Externalizer.class);
	
	private static final Comparator<Field> PROPERTY_COMPARATOR = new Comparator<Field>() {
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	private static Externalizer instance;
	
	private final Configuration configuration;
	
	public static Externalizer getInstance() {
		Configuration config = (Configuration)FactoryFinder.find(Configuration.class.getCanonicalName());
		return getInstance(config == null ? new DefaultConfiguration() : config);
	}
	
	public static Externalizer getInstance(Configuration config) {
		if (instance == null) {
			instance = new Externalizer(config);
		}
		return instance;
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

	
	private Externalizer(Configuration config) {
		this.configuration = config;
	}
	
	/**
	 * configuration support for Externalizer
	 * @author konkere
	 *
	 */
	public interface Configuration {
		static final Converter<Object, Object> PRESERVING_CONVERTER =
			new Converter<Object, Object>() {
				@Override
				public Object convert(Object source,
						Class<? extends Object> returnType) {
					return source;
				}
		};
		
		/**
		 * GAS3 sorts fields alphabetically when generating actionscript. This
		 * should be taken into account during externalization.
		 * @return
		 */
		public boolean useGAS3();
		
		/**
		 * should return a {@code Converter} for specified {@code clazz}
		 * @param clazz
		 * @return
		 */
		public Converter findConverter(Class clazz);
	}
	
	/**
	 * simple converter interface to provide configurable associations
	 * between types
	 * @author konkere
	 *
	 * @param <T> source class
	 * @param <U> destination class
	 */
	public interface Converter<T,U> {
		
		/**
		 * 
		 * @param source object to convert
		 * @return
		 */
		public U convert(T source, Class<? extends U> returnType);
	}
	
	/**
	 * helper interface which encapsulates operations on two types of fields: primitive and object
	 * @author zhariil
	 *
	 * @param <T>
	 */
	private interface Operation<T> {
		/**
		 * performs an operation on a primitive {@code f} of {@code t}
		 * @param t target object
		 * @param f target field
		 * @throws IOException
		 * @throws IllegalAccessException
		 */
		public void doPrimitive(Object t, Field f) throws IOException, IllegalAccessException;
		
		/**
		 * performs an operation on an object {@code f} of {@code t}
		 * @param t
		 * @param f
		 * @throws IOException
		 * @throws IllegalAccessException
		 */
		public void doObject(Object t, Field f) throws IOException, IllegalAccessException;
	}
	
	/**
	 * get all declared and inherited fields of {@code target} 
	 * and optionally sort them alphabetically as GAS3 does with generated
	 * actionscript classes. note that superclass fields go first. 
	 * @param target
	 * @return
	 */
	private List<Field> getDeclaredFields(Object target) {
		List<Field> properties = new ArrayList<Field>();
		Class<?> ss = target.getClass();
		
		do {
			List<Field> currentProps = Arrays.asList(ss.getDeclaredFields());
			
			if (this.configuration.useGAS3()) {
				Collections.sort(currentProps, PROPERTY_COMPARATOR);
			}
			
			properties.addAll(0, currentProps);
			ss = ss.getSuperclass();
		} while (ss != null);

		
		logger.debug("found " + properties + " on " + target.getClass().getCanonicalName());
		 
		return properties;
	}
	
	private void overrideAccess(Field f) {
		if (Modifier.isFinal(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) {
			f.setAccessible(true);
		}
	}
	
	/**
	 * static, synthetic and transient fields are not relevant
	 * (as we externalize the <i>state</i> of an object) and must be omitted
	 * @param f
	 * @return
	 */
	private boolean isRelevant(Field f) {
		return !(Modifier.isStatic(f.getModifiers()) ||
		f.isSynthetic() ||
		Modifier.isTransient(f.getModifiers()));
	}
	
	/**
	 * checks if the object considered is an entity, for which GAS3
	 * generates some metainfo, which normally we don't need, but we have
	 * to deal with it
	 * @param t
	 * @return
	 * @see #readEntityHeader(ObjectInput)
	 * @see #writeEntityHeader(ObjectOutput)
	 */
	private boolean isEntity(Object t) {
		return t.getClass().getAnnotation(Entity.class) != null;
	}

	protected void readEntityHeader(ObjectInput in) throws IOException {
		try {
			//__initialized
			in.readObject();
			//__detachedState
			in.readObject();
		}
		catch (ClassNotFoundException cnfe) {
			//should not happen
			logger.error(cnfe.getMessage(), cnfe);
		}
	}
	
	protected void writeEntityHeader(ObjectOutput out) throws IOException {
		//__initialized
		out.writeObject(Boolean.TRUE);
		//__detachedState
		out.writeObject("");
	}
	
	public void readExternal(Object target, final ObjectInput in) throws IOException {
		logger.debug("reading " + target.getClass().getCanonicalName());
		
		if (this.isEntity(target) && this.configuration.useGAS3()) {
			this.readEntityHeader(in);
		}
		
		this.doOperate(target, new Operation<ObjectInput>() {
			public void doObject(Object t, Field f) throws IllegalAccessException, IOException {
				try {
					Object o = in.readObject();
					
					if (o != null) {
						logger.info("converting " + o.getClass().getCanonicalName() + " to " + f.getType().getCanonicalName());
						o = configuration.findConverter(o.getClass()).convert(o, f.getType());
					}
					
					f.set(t, o);
				}
				catch (ClassNotFoundException e) {
					f.set(t, null);
				}
				catch (IllegalArgumentException iae) {
					f.set(t, null);
					report(t, f, iae);
				}
			}

			public void doPrimitive(Object t, Field f) throws IllegalAccessException, IOException {
				readPrimitive(t, f, in);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(Object target, final ObjectOutput out) throws IOException {
		logger.debug("writing " + target.getClass().getCanonicalName());
		
		if (this.isEntity(target) && this.configuration.useGAS3()) {
			this.writeEntityHeader(out);
		}
		
		this.doOperate(target, new Operation<ObjectOutput>() {
			public void doObject(Object t, Field f) throws IOException, IllegalAccessException {
				Object o = f.get(t);
				
				if (o != null) {
					o = configuration.findConverter(o.getClass()).convert(o, Externalizable.class);
				}
				out.writeObject(o);
			}

			public void doPrimitive(Object t, Field f) throws IOException, IllegalAccessException {
				writePrimitive(t, f, out);
			}
		});
	}

	/**
	 * performs externalization on {@code target} using specified {@code operation}
	 * @param <T>
	 * @param target
	 * @param operation
	 * @throws IOException
	 */
	private <T> void doOperate(Object target, Operation<T> operation) throws IOException {
		for (Field f : this.getDeclaredFields(target)) {
//			this.overrideAccess(f);
			f.setAccessible(true);

			//check if it's not a constant
			if (!this.isRelevant(f)) {
				continue;
			}
			
			logger.debug("processing " + f.toString());
			
			try {
				if (f.getType().isPrimitive()) {
					operation.doPrimitive(target, f);
				}
				else {
					operation.doObject(target, f);
				}
			}
			catch (IllegalAccessException iae) {
				report(target, f, iae);
			}
		}
	}

	/**
	 * reads a primitive {@code f} on {@code in} and sets in on {@code target}.<br><br>
	 * notice that actionscript only has two numeric types: {@code Number} for floating point and {@code int} for integers
	 * and doesn't differentiate between precisions, deserializing them as Double and Integer correspondingly.
	 * @param target
	 * @param f
	 * @param in
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	protected void readPrimitive(Object target, Field f, ObjectInput in) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> c = f.getType();
		Object value = null;
		
		try {
			value = in.readObject();
		}
		catch (ClassNotFoundException e) {
			//not possible ;)
			return;
		}
		
		if (c.isAssignableFrom(Boolean.TYPE)) {
			f.setBoolean(target, ((Boolean)value).booleanValue()/*in.readBoolean()*/);
		}
		else if (c.isAssignableFrom(Byte.TYPE)) {
			if (value instanceof Number) {
				f.setByte(target, ((Number)value).byteValue()/*in.readByte()*/);
			}
		}
		else if (c.isAssignableFrom(Short.TYPE)) {
			if (value instanceof Number) {
				f.setShort(target, ((Number)value).shortValue()/*in.readShort()*/);
			}
		}
		else if (c.isAssignableFrom(Integer.TYPE)) {
			if (value instanceof Number) {
				f.setInt(target, ((Number)value).intValue()/*in.readInt()*/);
			}
		}
		else if (c.isAssignableFrom(Long.TYPE)) {
			if (value instanceof Number) {
				f.setLong(target, ((Number)value).longValue()/*in.readLong()*/);
			}
		}
		else if (c.isAssignableFrom(Float.TYPE)) {
			if (value instanceof Number) {
				float tmp = ((Number)value).floatValue();
//				float tmp = in.readFloat();
				if (Float.compare(Float.NaN, tmp) != 0) {
					f.setFloat(target, tmp);
				}
			}
		}
		else if (c.isAssignableFrom(Double.TYPE)) {
			if (value instanceof Number) {
				double tmp = ((Number)value).doubleValue();
//				double tmp = in.readDouble();
				if (Double.compare(Double.NaN, tmp) != 0) {
					f.setDouble(target, tmp);
				}
			}
		}
	}
	
	/**
	 * writes a {@code f} of {@code target} to {@code out}.<br><br>
	 * notice that AMF can't handle primitives (neither from java, nor from actionscript),
	 * so they have to be written as their "big" wrappers.
	 * @param target
	 * @param f
	 * @param out
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	protected void writePrimitive(Object target, Field f, ObjectOutput out) throws IllegalArgumentException, IllegalAccessException, IOException {
		Class<?> c = f.getType();
		Object value = null;
		
		if (c.isAssignableFrom(Boolean.TYPE)) {
			value = Boolean.valueOf(f.getBoolean(target));
//			out.writeBoolean(f.getBoolean(target));
		}
		else if (c.isAssignableFrom(Byte.TYPE)) {
			value = Byte.valueOf(f.getByte(target));
//			out.writeByte(f.getByte(target));
		}
		else if (c.isAssignableFrom(Short.TYPE)) {
			value = Short.valueOf(f.getShort(target));
//			out.writeShort(f.getShort(target));
		}
		else if (c.isAssignableFrom(Integer.TYPE)) {
			value = Integer.valueOf(f.getInt(target));
//			out.writeInt(f.getInt(target));
		}
		else if (c.isAssignableFrom(Long.TYPE)) {
			value = Long.valueOf(f.getLong(target));
//			out.writeLong(f.getLong(target));
		}
		else if (c.isAssignableFrom(Float.TYPE)) {
			float tmp = f.getFloat(target);
			value = Float.compare(Float.NaN, tmp) != 0 ? tmp : null;
//			if (Float.compare(Float.NaN, tmp) != 0 ){
//				out.writeFloat(tmp);
//			}
//			else{
//				out.writeObject(null);
//			}
			
		}
		else if (c.isAssignableFrom(Double.TYPE)) {
			double tmp = f.getDouble(target);
			value = Double.compare(Double.NaN, tmp) != 0 ? tmp : null;
//			if (Double.compare(Double.NaN, tmp) != 0 ){
//				out.writeDouble(tmp);
//			}else{
//				out.writeObject(null);
//			}
		}
		
		out.writeObject(value);
	}
	
	private static void report(Object o, Field f, Throwable t) {
		logger.error(o.getClass().getCanonicalName() + ": [" + o.toString() + "], " +
				f.toString() + ": ", t);
	}
}