package com.wrenched.core.instrumentation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.MetadataLoader;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

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
	public Object unwrap(Object o) {
		return new ObjectProcessor() {
			@Override
			protected Object doProcess(Object obj, ObjectProcessor processor) {
				return evictProxies(obj, processor);
			}
			
			@SuppressWarnings({"rawtypes", "unchecked"})
			@Override
			public Object process(Object obj) {
				Object t = obj;
				if (t == null) {
					//just a safety check
				}
				else if (isSimpleProxy(t)) {
		    		t = super.process(getProxyTarget(t));
		    	}
		    	else if (isCollectionProxy(t)) {
		    		//TODO: only works with lists
					Iterator i = ((Collection)t).iterator();
		    		Collection implList = new ArrayList();
		    		
		    		while (i.hasNext()) {
		    			implList.add(super.process(i.next()));
		    		}
		    		t = implList;
		    	}
				
				return super.process(t);
			}
		}.process(o);
	}
    
    /**
     * simple way of eliminating proxies by forcing them to <code>null</code>.
     * @param o
     * @return
     * @throws NoSuchFieldException
     */
    protected Object evictProxies(Object o, final ObjectProcessor processor) {
    	final LazyAttributeRegistryDescriptor def = MetadataLoader.getInstance().getManagedClass(o.getClass());
    	
		new Operation<Field>() {
			@Override
			public void process(Object t, Field f) throws IllegalAccessException {
				f.set(t, ((def != null) ? null : processor.process(f.get(t))));
			}

			@Override
			public boolean isRelevant(Field f) {
				return (def == null) || def.attributes.contains(f.getName());
			}

			@Override
			public Field[] getFields(Object t) {
				Collection<Field> fields = getAllFields(t.getClass(), false);
				return fields.toArray(new Field[fields.size()]);
			}
		}.introspect(o);

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
