package com.wrenched.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;

import com.wrenched.core.externalization.io.ExternalizingObjectInputStream;
import com.wrenched.core.externalization.io.ExternalizingObjectOutputStream;

public class HttpInvokerClientInterceptorWrapper extends HttpInvokerProxyFactoryBean {
	private class SimpleHttpInvokerRequestExecutorWrapper extends SimpleHttpInvokerRequestExecutor {
		@Override
		protected OutputStream decorateOutputStream(OutputStream os)
				throws IOException {
			return new ExternalizingObjectOutputStream(os);
		}

		@Override
		protected InputStream decorateInputStream(InputStream is)
				throws IOException {
			return new ExternalizingObjectInputStream(is);
		}
	}

	@Override
	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		SimpleHttpInvokerRequestExecutor executor = new SimpleHttpInvokerRequestExecutorWrapper();
		executor.setBeanClassLoader(getBeanClassLoader());
		
		setHttpInvokerRequestExecutor(executor);
		
		return super.getHttpInvokerRequestExecutor();
	}
}
