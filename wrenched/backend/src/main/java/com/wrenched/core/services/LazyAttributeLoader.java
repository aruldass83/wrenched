package com.wrenched.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.services.support.LazyAttributeProvider;


/**
 * simple lazy attribute loader (LAL) service that in fact delegates loading
 * to providers, registered per domain.
 * @author konkere
 *
 */
public class LazyAttributeLoader {
	private Map<String, LazyAttributeProvider> providers =
		new HashMap<String, LazyAttributeProvider>();
	
	/**
	 * loads an {@code attributeName} which is declared lazy-loaded on {@code entityName} and returns an
	 * externalizable wrapper to be transferred outside. 
	 * @param entityName
	 * @param entityId
	 * @param attributeName
	 * @return
	 * @throws IllegalAccessException
	 */
	public LazyAttribute loadAttribute(String entityName, Object entityId, String attributeName) throws IllegalAccessException {
		LazyAttributeProvider provider = this.findProvider(entityName);
		
		if (provider == null) {
			throw new RuntimeException(entityName + " has no attribute provider registered");
		}
		
		try {
			return new LazyAttribute(entityName, entityId, attributeName,
					provider.loadAttribute(Class.forName(entityName), entityId, attributeName));
		}
		catch (ClassNotFoundException e) {
			throw new IllegalAccessException(e.getMessage());
		}
	}
	
	private LazyAttributeProvider findProvider(String entityName) {
		String domain = entityName;

		while (domain.indexOf('.') > 0 && !this.providers.containsKey(domain)) {
			domain = domain.substring(0, domain.lastIndexOf('.'));
		}
		
		return this.providers.get(domain);
	}

	/**
	 * setter for lazy attribute providers that cover certain domains
	 * @param providers
	 */
	public void setProviders(List<LazyAttributeProvider> providers) {
		for (LazyAttributeProvider provider : providers) {
			this.providers.put(provider.getDomain(), provider);
		}
	}
}
