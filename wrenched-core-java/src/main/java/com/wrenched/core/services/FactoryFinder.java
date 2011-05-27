package com.wrenched.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class FactoryFinder {
	static SecuritySupport ss = new SecuritySupport();

	/**
	 * Finds the implementation Class object in the specified order. Main entry
	 * point.
	 * 
	 * @return Class object of factory, never null
	 * 
	 * @param factoryId
	 *            Name of the factory to find, same as a property name
	 * 
	 *            Package private so this code can be shared.
	 */
	static Object find(String factoryId) throws ConfigurationError {
		// Try Jar Service Provider Mechanism
		String provider = findJarServiceProvider(factoryId);

		if (provider != null && !"".equals(provider)) {
			// Note: here we do not want to fall back to the current
			// ClassLoader because we want to avoid the case where the
			// resource file was found using one ClassLoader and the
			// provider class was instantiated using a different one.
			
			ClassLoader cl = ss.getContextClassLoader();
			if (cl == null) {
				throw new ConfigurationError("", new ClassNotFoundException(factoryId));
			}
			else {
				try {
					return cl.loadClass(provider).newInstance();
				}
				catch (ClassNotFoundException x) {
					throw new ConfigurationError(
							"Provider " + provider + " not found", x);
				}
				catch (Exception x) {
					throw new ConfigurationError("Provider " + provider
							+ " could not be instantiated: " + x, x);
				}
			}
		}
		else {
			return null;
		}
	}

	/*
	 * Try to find provider using Jar Service Provider Mechanism
	 * 
	 * @return instance of provider class if found or null
	 */
	private static String findJarServiceProvider(String factoryId) throws ConfigurationError {
		String serviceId = "META-INF/services/" + factoryId;
		InputStream is = null;

		// First try the Context ClassLoader
		ClassLoader cl = ss.getContextClassLoader();
		if (cl != null) {
			is = ss.getResourceAsStream(cl, serviceId);

			// If no provider found then try the current ClassLoader
			if (is == null) {
				cl = FactoryFinder.class.getClassLoader();
				is = ss.getResourceAsStream(cl, serviceId);
			}
		}
		else {
			// No Context ClassLoader, try the current ClassLoader
			cl = FactoryFinder.class.getClassLoader();
			is = ss.getResourceAsStream(cl, serviceId);
		}

		if (is == null) {
			// No provider found
			return null;
		}

		BufferedReader rd;
		try {
			rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		}
		catch (java.io.UnsupportedEncodingException e) {
			rd = new BufferedReader(new InputStreamReader(is));
		}

		String factoryClassName = null;
		try {
			// XXX Does not handle all possible input as specified by the
			// Jar Service Provider specification
			factoryClassName = rd.readLine();
			rd.close();
		}
		catch (IOException x) {
			// No provider found
			return null;
		}

		return factoryClassName;
	}

	static class ConfigurationError extends Error {
		private Exception exception;

		/**
		 * Construct a new instance with the specified detail string and
		 * exception.
		 */
		ConfigurationError(String msg, Exception x) {
			super(msg);
			this.exception = x;
		}

		Exception getException() {
			return exception;
		}
	}

}
