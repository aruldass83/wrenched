package com.wrenched.core.lazy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
import com.wrenched.core.services.LazyAttributeLoaderService;
import com.wrenched.util.ReflectionUtil.CallbackCopyOperation;

import static com.wrenched.util.ReflectionUtil.*;
import static com.wrenched.core.services.support.ClassIntrospectionUtil.*;

/**
 * 
 * @author konkere
 *
 */
public class LazyAttributeRegistry {
	static final Map<Class, LazyAttributeRegistryDescriptor> classes =
		new HashMap<Class, LazyAttributeRegistryDescriptor>();
	
	private static final MethodFilter GETSET_FILTER = new MethodFilter() {
		public boolean isHandled(Method m) {
			return isGetter(m) || isSetter(m);
		}
	};

	private static LazyAttributeRegistry instance;

	private final LazyAttributeLoaderService loader;
	
	public static LazyAttributeRegistry getInstance() {
		return instance;
	}

	public static LazyAttributeRegistry newInstance(LazyAttributeLoaderService loader) {
		if (instance == null && loader != null) {
			instance = new LazyAttributeRegistry(loader);
		}
		
		return instance;
	}
	
	private LazyAttributeRegistry(LazyAttributeLoaderService l) {
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
//		return createProxy(obj, new HashMap());
		return new ObjectProcessor() {
			@Override
			protected Object doProcess(Object obj, ObjectProcessor processor) {
				return createProxy(obj, processor);
			}
		}.process(obj);
	}
/*	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object createProxy(Object obj, final Map context) {
		if (obj == null || obj.getClass().isPrimitive() || obj instanceof Enum || obj instanceof String || obj instanceof Number) {
		}
		else if (context.containsKey(obj)) {
			return context.get(obj);
		}
		else if (obj.getClass().isArray()) {
			Object[] a = (Object[]) obj;
			
			for (int i = 0; i < a.length; i++) {
				a[i] = createProxy(a[i], context);
			}
		}
		else if (obj instanceof Collection) {
			if (obj instanceof List) {
				List l = (List) obj;
				
				for (int i = 0; i < l.size(); i++) {
					l.set(i, createProxy(l.get(i), context));
				}
			}
			else if (obj instanceof Set) {
				Set s = (Set)obj;
				
				for (Object element : s.toArray(new Object[s.size()])) {
					s.remove(element);
					s.add(createProxy(element, context));
				}
			}
		}
		else if (obj instanceof Map) {
			for (Entry element : (Set<Entry>)((Map)obj).entrySet()) {
				element.setValue(createProxy(element.getValue(), context));
			}
		}
		else {
			LazyAttributeRegistryDescriptor def = classes.get(obj.getClass());
			
			context.put(obj, obj);

			if (def != null) {
				Object id = resolveId(obj, def.idName);

				try {
//						new LazyInterceptor(def.className,
//								id,
//								def.attributes.toArray(new String[def.attributes.size()]));
					LazyInterceptor interceptor = new LazyInterceptor(def, id);
					Object proxy = createProxy(Class.forName(def.className), interceptor);

					context.put(proxy, proxy);
					context.put(obj, proxy);

					new CallbackCopyOperation(proxy, false) {
						@Override
						protected Object callback(String attributeName, Object attributeValue) {
							return createProxy(attributeValue, context);
						}
					}.introspect(obj);
					
					interceptor.start();
					
					return proxy;
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
*/
	private static Object createProxy(Object obj, final ObjectProcessor processor) {
		LazyAttributeRegistryDescriptor def = classes.get(obj.getClass());
		
		if (def != null) {
			Object id = resolveId(obj, def.idName);

			try {
				LazyInterceptor interceptor = new LazyInterceptor(def, id);
				Object proxy = createProxy(Class.forName(def.className), interceptor);

				new CallbackCopyOperation(proxy, false) {
					@Override
					protected Object callback(String attributeName, Object attributeValue) {
						return processor.process/*createProxy*/(attributeValue/*, context*/);
					}
				}.introspect(obj);
				
				interceptor.start();
				
				return proxy;
			}
			catch(Exception e) {
				//can't create proxy, skip
			}
		}
		else {
			new CallbackCopyOperation(obj, false) {
				@Override
				protected Object callback(String attributeName, Object attributeValue) {
					return processor.process/*createProxy*/(attributeValue/*, context*/);
				}
			}.introspect(obj);
		}
		
		return obj;
	}
	
	static Object createProxy(Class clazz, MethodHandler handler) throws InstantiationException, IllegalAccessException {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(clazz);
		factory.setFilter(GETSET_FILTER);
		
		ProxyObject proxy = (ProxyObject)factory.createClass().newInstance();
		proxy.setHandler(handler);
		return proxy;
	}
	
	private static Object resolveId(Object obj, Object idName) {
		if (idName instanceof String) {			
			if ("self".equals(idName)) {
				return obj;
			}
			else {
				return getAttributeValue(obj, (String)idName);
			}
		}	
		else if (idName instanceof LazyAttributeRegistryDescriptor) {
			LazyAttributeRegistryDescriptor idDef =
				(LazyAttributeRegistryDescriptor)idName;
			//composite id
			try {
				Class idClass = Class.forName(idDef.className);
				Object id = idClass.newInstance();
	
				for (String idCompositeName: idDef.attributes) {
					setAttributeValue(id, idCompositeName, getAttributeValue(obj, idCompositeName));
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
