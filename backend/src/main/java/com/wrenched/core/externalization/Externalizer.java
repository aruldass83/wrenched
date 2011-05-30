package com.wrenched.core.externalization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wrenched.core.services.support.ClassIntrospectionUtil.Operation;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.getAllFields;

/**
 * Externalization support
 * @author konkere
 *
 */
public class Externalizer {
	private static final Log logger = LogFactory.getLog(Externalizer.class);
	
	private static Externalizer instance;
	
	private final Configuration configuration;
	
	public static Externalizer getInstance() {
		Configuration config = null;
		
		try {
			config = new AggregatingConfiguration();
		}
		catch (InstantiationException ie) {
			config = null;
		}
		return getInstance(config == null ? new DefaultConfiguration() : config);
	}
	
	public static Externalizer getInstance(Configuration config) {
		if (instance == null) {
			instance = new Externalizer(config);
		}
		return instance;
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
	 * helper class which encapsulates operations on two types of fields: primitive and object
	 * @author konkere
	 *
	 * @param <T>
	 */
	private abstract class SelectiveOperation<T> extends Operation<Field> {
		
		/**
		 * performs an operation on a primitive {@code f} of {@code t}
		 * @param t target object
		 * @param f target field
		 * @throws IllegalAccessException
		 */
		public abstract void doPrimitive(Object t, Field f) throws IllegalAccessException;
		
		/**
		 * performs an operation on an object {@code f} of {@code t}
		 * @param t
		 * @param f
		 * @throws IllegalAccessException
		 */
		public abstract void doObject(Object t, Field f) throws IllegalAccessException;

		/*
		 * (non-Javadoc)
		 * @see com.wrenched.core.services.support.ClassIntrospectionUtil.Operation#process(java.lang.Object, java.lang.reflect.AccessibleObject)
		 */
		@Override
		public final void process(Object t, Field f) throws IllegalAccessException {
			if (f.getType().isPrimitive()) {
				this.doPrimitive(t, f);
			}
			else {
				this.doObject(t, f);
			}
		}
	}
	
	/**
	 * get all declared and inherited fields of {@code target} 
	 * and optionally sort them alphabetically as GAS3 does with generated
	 * actionscript classes. note that superclass fields go first. 
	 * @param target
	 * @return
	 */
	private Collection<Field> getDeclaredFields(Object target) {
		Collection<Field> properties = getAllFields(target.getClass(), this.configuration.useGAS3());
		logger.debug("found " + properties + " on " + target.getClass().getCanonicalName());
		return properties;
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
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(Object target, final ObjectInput in) throws IOException {
		logger.debug("reading " + target.getClass().getCanonicalName());
		
		if (this.isEntity(target) && this.configuration.useGAS3()) {
			this.readEntityHeader(in);
		}

		new SelectiveOperation<ObjectInput>() {
			@Override
			public void doObject(Object t, Field f) throws IllegalAccessException {
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
				catch (IOException ioe) {
					f.set(t, null);
					report(t, f, ioe);
				}
			}
	
			@Override
			public void doPrimitive(Object t, Field f) throws IllegalAccessException {
				try {
					readPrimitive(t, f, in);
				}
				catch (IOException ioe) {
					report(t, f, ioe);					
				}
			}
			
			@Override
			public boolean isRelevant(Field f) {
				return Externalizer.this.isRelevant(f);
			}
			
			@Override
			public Field[] getFields(Object t) {
				Collection<Field> result = getDeclaredFields(t);
				return result.toArray(new Field[result.size()]);
			}
		}.introspect(target);
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
		
		new SelectiveOperation<ObjectOutput>() {
			@Override
			public void doObject(Object t, Field f) throws IllegalAccessException {
				Object o = f.get(t);
				
				if (o != null) {
					o = configuration.findConverter(o.getClass()).convert(o, Externalizable.class);
				}
				
				try {
					out.writeObject(o);
				}
				catch (IOException ioe) {
					report(t, f, ioe);
				}
			}
	
			@Override
			public void doPrimitive(Object t, Field f) throws IllegalAccessException {
				try {
					writePrimitive(t, f, out);
				}
				catch (IOException ioe) {
					report(t, f, ioe);
				}
			}
	
			@Override
			public boolean isRelevant(Field f) {
				return Externalizer.this.isRelevant(f);
			}
			
			@Override
			public Field[] getFields(Object t) {
				Collection<Field> result = getDeclaredFields(t);
				return result.toArray(new Field[result.size()]);
			}
		}.introspect(target);
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
			f.setBoolean(target, ((Boolean)value).booleanValue());
		}
		else if (c.isAssignableFrom(Byte.TYPE)) {
			if (value instanceof Number) {
				f.setByte(target, ((Number)value).byteValue());
			}
		}
		else if (c.isAssignableFrom(Short.TYPE)) {
			if (value instanceof Number) {
				f.setShort(target, ((Number)value).shortValue());
			}
		}
		else if (c.isAssignableFrom(Integer.TYPE)) {
			if (value instanceof Number) {
				f.setInt(target, ((Number)value).intValue());
			}
		}
		else if (c.isAssignableFrom(Long.TYPE)) {
			if (value instanceof Number) {
				f.setLong(target, ((Number)value).longValue());
			}
		}
		else if (c.isAssignableFrom(Float.TYPE)) {
			if (value instanceof Number) {
				float tmp = ((Number)value).floatValue();
				if (Float.compare(Float.NaN, tmp) != 0) {
					f.setFloat(target, tmp);
				}
			}
		}
		else if (c.isAssignableFrom(Double.TYPE)) {
			if (value instanceof Number) {
				double tmp = ((Number)value).doubleValue();
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
		}
		else if (c.isAssignableFrom(Byte.TYPE)) {
			value = Byte.valueOf(f.getByte(target));
		}
		else if (c.isAssignableFrom(Short.TYPE)) {
			value = Short.valueOf(f.getShort(target));
		}
		else if (c.isAssignableFrom(Integer.TYPE)) {
			value = Integer.valueOf(f.getInt(target));
		}
		else if (c.isAssignableFrom(Long.TYPE)) {
			value = Long.valueOf(f.getLong(target));
		}
		else if (c.isAssignableFrom(Float.TYPE)) {
			float tmp = f.getFloat(target);
			value = Float.compare(Float.NaN, tmp) != 0 ? tmp : null;
		}
		else if (c.isAssignableFrom(Double.TYPE)) {
			double tmp = f.getDouble(target);
			value = Double.compare(Double.NaN, tmp) != 0 ? tmp : null;
		}
		
		out.writeObject(value);
	}
	
	private static void report(Object o, Field f, Throwable t) {
		logger.error(o.getClass().getCanonicalName() + ": [" + o.toString() + "], " +
				f.toString() + ": ", t);
	}
}