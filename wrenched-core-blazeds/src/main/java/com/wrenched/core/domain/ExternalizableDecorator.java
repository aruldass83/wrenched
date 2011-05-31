package com.wrenched.core.domain;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.persistence.Entity;

import com.wrenched.core.externalization.Externalizer;

/**
 * a decorator class to be used to successfully employ non-externalizable entities
 * in flex-to-java remote calls. mainly used in conjunction with blazeds bean proxies.
 * @author zhariil
 *
 * @param <T>
 */
public abstract class ExternalizableDecorator<T> extends AbstractExternalizableEntity {
	private transient T delegate;
	
	private static class ExternalizableInvocationHandler implements InvocationHandler {
		private final transient Object delegate;
		
		ExternalizableInvocationHandler(Object arg0) {
			this.delegate = arg0;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("writeExternal")) {
				ExternalizableDecorator.getInstance(this.delegate).writeExternal((ObjectOutput)args[0]);
				return null;
			}
			else if (method.getName().equals("readExternal")) {
				ExternalizableDecorator.getInstance(this.delegate).readExternal((ObjectInput)args[0]);
				return null;
			}
			return method.invoke(this.delegate, args);
		}
	}	
	
	/**
	 * returns a decorator for a given class of type W
	 * @param <W>
	 * @param desiredClass
	 * @return
	 */
	public static <W> ExternalizableDecorator<W> getInstance(final Class<W> desiredClass) {
		return new ExternalizableDecorator<W>(desiredClass) {
			@Override
			public Class<W> getDelegateClass() {
				return desiredClass;
			}
		};
	}

	/**
	 * returns a decorator for a given object of type W
	 * @param <W>
	 * @param defaultValue
	 * @return
	 */
	public static <W> ExternalizableDecorator<W> getInstance(final W defaultValue) {
		return new ExternalizableDecorator<W>(defaultValue) {
			@Override
			@SuppressWarnings("unchecked")
			public Class<? extends W> getDelegateClass() {
				return (Class<W>)defaultValue.getClass();
			}
		};
	}
	
	protected ExternalizableDecorator(Class<T> clazz) {
		this.delegate = this.newInstance();
	}

	protected ExternalizableDecorator(T item) {
		this.delegate = item;
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
		}
	}
	
	protected void writeEntityHeader(ObjectOutput out) throws IOException {
		//__initialized
		out.writeObject(Boolean.TRUE);
		//__detachedState
		out.writeObject("");
	}

	/**
	 * instantiates the delegate according to provided class. assumes that
	 * there's a public no-arg constructor (blazeds requirement anyway).
	 * @return
	 */
	private T newInstance() {
		try {
			return this.getDelegateClass().newInstance();
		}
		catch (IllegalAccessException iae) {
			return null;
		}
		catch (InstantiationException ie) {
			throw new RuntimeException(ie);
		}
	}
	
	public T getDelegate() {
		return this.delegate;
	}
	
	/**
	 * returns the delegate class
	 * @return
	 */
	public abstract Class<? extends T> getDelegateClass();

	/**
	 * convenient proxification in case this class is used outside blazeds bean proxy mechanism.
	 * @return JDK proxy for wrapped delegate
	 */
	public static Object proxy(Object delegate) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] {java.io.Externalizable.class},
				new ExternalizableInvocationHandler(delegate));
	}
	
	/*
	 * (non-Javadoc)
	 * notice that the input stream has the delegate serialized, not a decorator instance
	 * @see com.wrenched.core.exchange.AbstractExternalizableEntity#readExternal(java.io.ObjectInput)
	 */
	public final void readExternal(ObjectInput in) throws IOException {
		//this is necessary to comply to GAS3 generated code
		if (this.isEntity(this.delegate) && Externalizer.getInstance().getConfiguration().useGAS3()) {
			this.readEntityHeader(in);
		}

		Externalizer.getInstance().readExternal(this.delegate, in);
	}

	/*
	 * (non-Javadoc)
	 * notice that the delegate should be serialized, instead of current instance
	 * @see com.wrenched.core.exchange.AbstractExternalizableEntity#writeExternal(java.io.ObjectOutput)
	 */
	public final void writeExternal(ObjectOutput out) throws IOException {
		//this is necessary to comply to GAS3 generated code
		if (this.isEntity(this.delegate) && Externalizer.getInstance().getConfiguration().useGAS3()) {
			this.writeEntityHeader(out);
		}
		
		Externalizer.getInstance().writeExternal(this.delegate, out);
	}
}
