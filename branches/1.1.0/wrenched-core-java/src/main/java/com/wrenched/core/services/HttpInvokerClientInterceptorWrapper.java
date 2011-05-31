package com.wrenched.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.SerializerFactory;
import com.wrenched.core.externalization.io.HessianInputStream;
import com.wrenched.core.externalization.io.HessianOutputStream;

/**
 * convenience implementation of HTTP-invoker remoting that uses Hessian instead of
 * Java Serialization API
 * @author konkere
 *
 */
public class HttpInvokerClientInterceptorWrapper extends HttpInvokerProxyFactoryBean {
	private final HessianProxyFactory proxyFactory = new HessianProxyFactory();

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		SerializerFactory sf = new SerializerFactory();
		sf.setAllowNonSerializable(true);
		
		this.proxyFactory.setSerializerFactory(sf);
		proxyFactory.setHessian2Request(true);
	}
	
	/**
	 * @see org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor
	 */
	private class SimpleHttpInvokerRequestExecutorWrapper extends SimpleHttpInvokerRequestExecutor {
		/*
		 * (non-Javadoc)
		 * @see org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor#readRemoteInvocationResult(java.io.InputStream, java.lang.String)
		 */
		@Override
		protected final RemoteInvocationResult readRemoteInvocationResult(InputStream is, String codebaseUrl)
		throws IOException,	ClassNotFoundException {
			ObjectInput oi = new HessianInputStream(proxyFactory.getHessianInput(is));
			try {
				Object obj = oi.readObject();
				if (!(obj instanceof RemoteInvocationResult)) {
					throw new RemoteException("Deserialized object needs to be assignable to type ["
									+ RemoteInvocationResult.class.getName()
									+ "]: " + obj);
				}
				return (RemoteInvocationResult) obj;
			}
			finally {
				oi.close();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor#writeRemoteInvocation(org.springframework.remoting.support.RemoteInvocation, java.io.OutputStream)
		 */
		@Override
		protected final void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
			ObjectOutput oo = new HessianOutputStream(proxyFactory.getHessianOutput(os));
			try {
				oo.writeObject(invocation);
				oo.flush();
			}
			finally {
				oo.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.httpinvoker.HttpInvokerClientInterceptor#getHttpInvokerRequestExecutor()
	 */
	@Override
	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		SimpleHttpInvokerRequestExecutor executor = new SimpleHttpInvokerRequestExecutorWrapper();
		executor.setBeanClassLoader(getBeanClassLoader());
		
		setHttpInvokerRequestExecutor(executor);
		
		return super.getHttpInvokerRequestExecutor();
	}
}
