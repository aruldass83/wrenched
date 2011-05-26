package com.wrenched.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.wrenched.core.externalization.io.ExternalizingObjectInputStream;
import com.wrenched.core.externalization.io.ExternalizingObjectOutputStream;

public class HttpInvokerServiceExporterWrapper extends HttpInvokerServiceExporter {
	@Override
	protected InputStream decorateInputStream(HttpServletRequest request,
			InputStream is) throws IOException {
		return new ExternalizingObjectInputStream(is);
	}

	@Override
	protected OutputStream decorateOutputStream(HttpServletRequest request,
			HttpServletResponse response, OutputStream os) throws IOException {
		return new ExternalizingObjectOutputStream(os);
	}

}
