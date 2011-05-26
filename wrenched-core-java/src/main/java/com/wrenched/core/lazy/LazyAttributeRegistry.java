package com.wrenched.core.lazy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.LazyAttributeLoader;
import com.wrenched.util.ReflectionUtil.CallbackCopyOperation;

import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

public class LazyAttributeRegistry {
	static final Map<Class, LazyAttributeRegistryDescriptor> classes =
		new HashMap<Class, LazyAttributeRegistryDescriptor>();
	
	final LazyAttributeLoader loader;
	
	private static LazyAttributeRegistry instance;
	
	public static LazyAttributeRegistry getInstance() {
		return instance;
	}

	public static LazyAttributeRegistry getInstance(LazyAttributeLoader loader) {
		if (instance == null && loader != null) {
			instance = new LazyAttributeRegistry(loader);
		}
		
		return instance;
	}
	
	private LazyAttributeRegistry(LazyAttributeLoader l) {
		loader = l;
		for (LazyAttributeRegistryDescriptor def : loader.getManagedClasses()) {
			try {
				classes.put(Class.forName(def.className), def);
			}
			catch (ClassNotFoundException cnfe) {
				continue;
			}
		}
	}
	
	LazyAttribute load(String className, Object id, String attributeName) throws IllegalAccessException {
		return loader.loadAttribute(className, id, attributeName);
	}

	public static Object createProxy(Object obj) {
		return createProxy(obj, new HashMap());
	}
	
	private static Object createProxy(Object obj, final Map context) {
		if (obj == null || obj instanceof Enum || obj instanceof String || obj instanceof Number) {
		}
		else if (context.containsKey(obj)) {
			return context.get(obj);
		}
		else if (obj.getClass().isArray()) {
			return createProxy(Arrays.asList((Object[])obj), context);
		}
		else if (obj instanceof Collection) {
			//TODO: copy collections?
//			Collection list = (Collection)obj;
//			
//			for (int i = 0; i < list.size(); i++) {
//				list.setItemAt(createProxy(list.getItemAt(i), context), i);
//			}
		}
		else if (obj instanceof Map) {
			Map map = (Map)obj;
			Collection keys = map.keySet();
			
			for (Entry element : (Set<Entry>)map.entrySet()) {
				element.setValue(createProxy(element.getValue(), context));
			}
		}
		else {
			LazyAttributeRegistryDescriptor def = classes.get(obj.getClass());

			context.put(obj, obj);

			if (def != null) {
				Object id = resolveId(def, obj);

				try {
					LazyInterceptor interceptor =
						new LazyInterceptor(def.className,
								id,
								def.attributes.toArray(new String[def.attributes.size()]));
					Object proxy = null;
					//TODO: create proxy here

					context.put(proxy, proxy);
					context.put(obj, proxy);

					new CallbackCopyOperation(proxy, false) {
						@Override
						protected Object callback(String attributeName, Object attributeValue) {
							return createProxy(attributeValue, context);
						}
					}.introspect(obj);
					
					interceptor.start();
				}
				catch(Exception e) {
					//can't create proxy, skip
				}
			}
			else {
				new CallbackCopyOperation(obj, false) {
					@Override
					protected Object callback(String attributeName, Object attributeValue) {
						return createProxy(attributeValue, context);
					}
				}.introspect(obj);
			}
			
			return context.get(obj);
		}

		return obj;
	} 

	private static Object resolveId(LazyAttributeRegistryDescriptor def, Object obj) {
		if (def.idName instanceof String) {			
			if ("self".equals(def.idName)) {
				return obj;
			}
			else {
				return getAttributeValue(obj, (String)def.idName);
			}
		}	
		else if (def.idName instanceof LazyAttributeRegistryDescriptor) {
			LazyAttributeRegistryDescriptor idDef =
				(LazyAttributeRegistryDescriptor)def.idName;
			//composite id
			try {
				Class idClass = Class.forName(idDef.className);
				Object id = idClass.newInstance();
	
				for (String idName: idDef.attributes) {
					setAttributeValue(id, idName, getAttributeValue(obj, idName));
				}
	
				return id;
			}
			catch (ClassNotFoundException cnfe) {
				return null;
			}
			catch (InstantiationException e) {
				return null;
			}
			catch (IllegalAccessException e) {
				return null;
			}
		}
		else {
			//don't accept anything else
			return null;
		}
	}
}
