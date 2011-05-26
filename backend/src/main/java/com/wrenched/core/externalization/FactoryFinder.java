package com.wrenched.core.externalization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
	static Object[] find(String factoryId) throws ConfigurationError {
		// Try Jar Service Provider Mechanism
		String[] providers = findJarServiceProvider(factoryId);
		Object[] factories = new Object[providers.length];

		if (providers != null && providers.length > 0) {
			// Note: here we do not want to fall back to the current
			// ClassLoader because we want to avoid the case where the
			// resource file was found using one ClassLoader and the
			// provider class was instantiated using a different one.
			
			ClassLoader cl = ss.getContextClassLoader();
			if (cl == null) {
				throw new ConfigurationError("", new ClassNotFoundException(factoryId));
			}
			else {
				for (int i = 0; i < providers.length; i++) {
					try {
						if (providers[i] != null) {
							factories[i] = cl.loadClass(providers[i]).newInstance();
						}
					}
					catch (ClassNotFoundException x) {
						throw new ConfigurationError(
								"Provider " + providers[i] + " not found", x);
					}
					catch (Exception x) {
						throw new ConfigurationError("Provider " + providers[i]
								+ " could not be instantiated: " + x, x);
					}
				}
				
				return factories;
			}
		}
		else {
			return null;
		}
	}

	private static String[] findJarServiceProvider(String factoryId) throws ConfigurationError {
		String serviceId = "META-INF/services/" + factoryId;
		
		// First try the Context ClassLoader
		ClassLoader cl = ss.getContextClassLoader();
		if (cl == null) {
			// No Context ClassLoader, try the current ClassLoader
			cl = FactoryFinder.class.getClassLoader();
		}
		try {
			Resource[] resources =
				new PathMatchingResourcePatternResolver(cl).getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + serviceId);
			String[] factories = new String[resources.length];
			
			for (int i = 0; i < resources.length; i++) {
				try {
					factories[i] = readFactoryClassName(resources[i].getInputStream());
				}
				catch (IOException ioe) {
					factories[i] = null;
					continue;
				}
			}

			return factories;
		}
		catch (IOException ioe) {
			return null;
		}
	}
	
	private static String readFactoryClassName(InputStream is) {
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
