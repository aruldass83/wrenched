package com.wrenched.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.SerializerFactory;
import com.wrenched.core.externalization.io.HessianInputStream;
import com.wrenched.core.externalization.io.HessianOutputStream;
import com.wrenched.core.instrumentation.ProxyInstrumentor;

/**
 * convenience implementation of HTTP-invoker remoting that uses Hessian instead of
 * Java Serialization API
 * @author konkere
 *
 */
public class HttpInvokerServiceExporterWrapper extends HttpInvokerServiceExporter {
	private final HessianProxyFactory proxyFactory = new HessianProxyFactory();
	private final ProxyInstrumentor instrumentor;

	public HttpInvokerServiceExporterWrapper() {
		//find an appropriate instrumentor using jar discovery
		instrumentor = (ProxyInstrumentor)FactoryFinder.find(ProxyInstrumentor.class.getCanonicalName());				
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		SerializerFactory sf = new SerializerFactory();
		sf.setAllowNonSerializable(true);
		
		this.proxyFactory.setSerializerFactory(sf);
		proxyFactory.setHessian2Request(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter#readRemoteInvocation(javax.servlet.http.HttpServletRequest, java.io.InputStream)
	 */
	@Override
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is)
	throws IOException, ClassNotFoundException {
		ObjectInput oi = new HessianInputStream(proxyFactory.getHessianInput(is));
		try {
			Object obj = oi.readObject();
			if (!(obj instanceof RemoteInvocation)) {
				throw new RemoteException("Deserialized object needs to be assignable to type ["
								+ RemoteInvocation.class.getName() + "]: " + obj);
			}
			return (RemoteInvocation) obj;
		} finally {
			oi.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter#writeRemoteInvocationResult(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.remoting.support.RemoteInvocationResult, java.io.OutputStream)
	 */
	@Override
	protected final void writeRemoteInvocationResult(HttpServletRequest request, HttpServletResponse response,
			RemoteInvocationResult result, OutputStream os)
	throws IOException {
		ObjectOutput oo = new HessianOutputStream(proxyFactory.getHessianOutput(os));
		RemoteInvocationResult unwrap = result;

		//instrument the result of invocation and remove proxies
		try {
			if (instrumentor != null) {
				try {
					unwrap = new RemoteInvocationResult(instrumentor.unwrap(unwrap.getValue()));
				}
				catch (NoSuchFieldException nsfe) {
				}
			}
			
			oo.writeObject(unwrap);
			oo.flush();
		}
		finally {
			oo.close();
		}
	}
}
