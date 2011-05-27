package com.wrenched.core.services;

import java.security.*;
import java.io.*;

class SecuritySupport {
	ClassLoader getContextClassLoader() throws SecurityException {
		return (ClassLoader) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						ClassLoader cl = Thread.currentThread().getContextClassLoader();

						if (cl == null) {
							cl = ClassLoader.getSystemClassLoader();
						}

						return cl;
					}
				});
	}

	String getSystemProperty(final String propName) {
		return (String) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return System.getProperty(propName);
			}
		});
	}

	FileInputStream getFileInputStream(final File file)
			throws FileNotFoundException {
		try {
			return (FileInputStream) AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws FileNotFoundException {
							return new FileInputStream(file);
						}
					});
		}
		catch (PrivilegedActionException e) {
			throw (FileNotFoundException) e.getException();
		}
	}

	InputStream getResourceAsStream(final ClassLoader cl, final String name) {
		return (InputStream) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						InputStream ris;
						if (cl == null) {
							ris = ClassLoader.getSystemResourceAsStream(name);
						}
						else {
							ris = cl.getResourceAsStream(name);
						}
						return ris;
					}
				});
	}

	boolean doesFileExist(final File f) {
		return ((Boolean) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return new Boolean(f.exists());
			}
		})).booleanValue();
	}
}
