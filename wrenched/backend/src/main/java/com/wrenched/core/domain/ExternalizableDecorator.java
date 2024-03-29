package com.wrenched.core.domain;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.wrenched.core.externalization.Externalizer;


/**
 * a decorator class to be used to successfully employ non-externalizable entities
 * in flex-to-java remote calls. mainly used in conjunction with blazeds bean proxies.
 * @author zhariil
 *
 * @param <T>
 */
public abstract class ExternalizableDecorator<T> extends AbstractExternalizableEntity {
	private final transient T delegate;
	
	/**
	 * returns a decorator for a given class of type W
	 * @param <W>
	 * @param desiredClass
	 * @return
	 */
	public static <W> ExternalizableDecorator<W> getInstance(final Class<W> desiredClass) {
		return new ExternalizableDecorator<W>() {
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
	
	protected ExternalizableDecorator() {
		this.delegate = this.newInstance();
	}
	
	protected ExternalizableDecorator(T item) {
		this.delegate = item;
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
	public final Object proxy() {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { this.getDelegateClass() },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return method.invoke(getDelegate(), args);
					}
				});
	}
	
	/*
	 * (non-Javadoc)
	 * notice that the input stream has the delegate serialized, not a decorator instance
	 * @see com.wrenched.core.exchange.AbstractExternalizableEntity#readExternal(java.io.ObjectInput)
	 */
	public final void readExternal(ObjectInput in) throws IOException {
		Externalizer.getInstance().readExternal(this.delegate, in);
	}

	/*
	 * (non-Javadoc)
	 * notice that the delegate should be serialized, instead of current instance
	 * @see com.wrenched.core.exchange.AbstractExternalizableEntity#writeExternal(java.io.ObjectOutput)
	 */
	public final void writeExternal(ObjectOutput out) throws IOException {
		Externalizer.getInstance().writeExternal(this.delegate, out);
	}
}
