package com.wrenched.core.instrumentation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.MetadataLoader;
import com.wrenched.core.services.support.ClassIntrospectionUtil.Operation;

/**
 * convenient implementation of unwrapping proxies and exposing
 * underlying objects
 * @author konkere
 *
 */
public abstract class AbstractUnwrappingInstrumentor implements ProxyInstrumentor {
	/*
	 * (non-Javadoc)
	 * @see com.wrenched.core.services.support.PersistentProxyInstrumentor#unwrap(java.lang.Object)
	 */
	public Object unwrap(Object o) throws NoSuchFieldException {
		if (o == null) {
			return o;
		}
		else if (this.isSimpleProxy(o)) {
    		return evictProxies(this.getProxyTarget(o));
    	}
    	else if (this.isCollectionProxy(o)) {
    		Iterator i = ((Collection)o).iterator();
    		Collection implList = new ArrayList();
    		
    		while (i.hasNext()) {
    			implList.add(evictProxies(i.next()));
    		}
    		return implList;
    	}
    	else {
    		return evictProxies(o);
    	}
    }
    
    /**
     * simple way of eliminating proxies by forcing them to <code>null</code>.
     * @param o
     * @return
     * @throws NoSuchFieldException
     */
    protected Object evictProxies(Object o) throws NoSuchFieldException {
    	final LazyAttributeRegistryDescriptor def = MetadataLoader.getInstance().getManagedClass(o.getClass());
    	
    	if (def != null) {
    		new Operation<Field>() {
				@Override
				public void process(Object t, Field f) throws IllegalAccessException {
					f.set(t, null);
				}

				@Override
				public boolean isRelevant(Field f) {
					return def.attributes.contains(f.getName());
				}

				@Override
				public Field[] getFields(Object t) {
					return t.getClass().getDeclaredFields();
				}
    		}.introspect(o);
    		
//	    	for (String attributeName : def.attributes) {
//				try {
//		    		Field f = o.getClass().getDeclaredField(attributeName);
//		    		f.setAccessible(true);
//		    		f.set(o, null);
//				}
//				catch (IllegalArgumentException e) {
//					//not possible
//				}
//				catch (IllegalAccessException e) {
//					//not possible
//				}
//	    	}
    	}    	

    	return o;
    }
    
    /**
     * determine if an object is proxied
     * @param o
     * @return
     */
    protected abstract boolean isSimpleProxy(Object o);
    
    /**
     * determine of an object is a proxied collection
     * @param o
     * @return
     */
    protected abstract boolean isCollectionProxy(Object o);
    
    /**
     * get the object behind the proxy
     * @param o
     * @return
     */
    protected abstract Object getProxyTarget(Object o);
}
