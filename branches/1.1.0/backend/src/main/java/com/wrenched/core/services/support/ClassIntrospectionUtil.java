package com.wrenched.core.services.support;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

public class ClassIntrospectionUtil {
	public static final String PREFIX_GET = "get";
	public static final String PREFIX_SET = "set";

	private static final Comparator<Field> PROPERTY_COMPARATOR = new Comparator<Field>() {
		public int compare(Field o1, Field o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	/**
	 * helper abstract class that encapsulates object introspection operations
	 * @author konkere
	 *
	 * @param <T>
	 */
	public static abstract class Operation<T extends AccessibleObject> {
		/**
		 * 
		 * @param t
		 * @param f
		 * @throws IllegalAccessException
		 */
		protected abstract void process(Object t, T f) throws IllegalAccessException;
		
		/**
		 * must tell if a field is relevant for introspection or not
		 * @param f
		 * @return
		 */
		protected abstract boolean isRelevant(T f);

		/**
		 * must return an array of fields to introspect 
		 * @param t
		 * @return
		 */
		public abstract T[] getFields(Object t);

		/**
		 * performs introspection on {@code target}
		 * @param <T>
		 * @param target
		 */
		public void introspect(Object target) {
			for (T f : getFields(target)) {
				f.setAccessible(true);

				//check if it's not a constant
				if (!this.isRelevant(f)) {
					continue;
				}
				
				try {
					this.process(target, f);
				}
				catch (IllegalAccessException iae) {
					continue;
				}
			}
		}
	}

	/**
	 * synthetic introspection operation that doesn't require actual reflection access with fields it
	 * introspect.
	 * @author konkere
	 *
	 * @param <T>
	 * @param <U>
	 */
	private static abstract class ClassOperation<T, U> extends Operation<AccessibleObject> {
		T result;
		boolean fieldAccess;

		ClassOperation(T arg0, boolean arg2) {
			result = arg0;
			fieldAccess = arg2;
		}
		
		@Override
		protected boolean isRelevant(AccessibleObject f) {
			return true;
		}
		
		@Override
		public AccessibleObject[] getFields(Object target) {
			Class clazz = (Class)target;
			return fieldAccess ? clazz.getDeclaredFields() : clazz.getMethods();
		}

		T introspect(Class clazz) {
			super.introspect(clazz);
			return result;
		}
		
		protected abstract void process(U arg0);
	}
	
	/**
	 * simple operation that transforms fields into their names
	 * @author konkere
	 *
	 * @param <T>
	 */
	private static abstract class NamingOperation<T> extends ClassOperation<T, String> {
		NamingOperation(T arg0, boolean arg2) {
			super(arg0, arg2);
		}
		
		@Override
		protected void process(Object t, AccessibleObject f) {
			if (this.fieldAccess) {
				process(((Field)f).getName());
			}
			else {
				process(getAttributeName((Method)f));
			}
		}
	}
	
	/**
	 * introspection class that traverses object trees
	 * @author konkere
	 *
	 */
	public static abstract class ObjectProcessor {
		private final Map<Object, Object> context = new HashMap<Object, Object>();
		
		protected abstract Object doProcess(Object obj, ObjectProcessor processor);
	
		@SuppressWarnings({"unchecked", "rawtypes"})
		public Object process(Object obj) {
			if (obj == null || obj.getClass().isPrimitive() || obj instanceof Enum || obj instanceof String || obj instanceof Number) {
			}
			else if (context.containsKey(obj)) {
				return context.get(obj);
			}
			else if (obj.getClass().isArray()) {
				Object[] a = (Object[]) obj;
				
				for (int i = 0; i < a.length; i++) {
					a[i] = process(a[i]);
				}
			}
			else if (obj instanceof Collection) {
				if (obj instanceof List) {
					List l = (List) obj;
					
					for (int i = 0; i < l.size(); i++) {
						l.set(i, process(l.get(i)));
					}
				}
				else if (obj instanceof Set) {
					Set s = (Set)obj;
					
					for (Object element : s.toArray(new Object[s.size()])) {
						s.remove(element);
						s.add(process(element));
					}
				}
			}
			else if (obj instanceof Map) {
				for (Entry element : (Set<Entry>)((Map)obj).entrySet()) {
					element.setValue(process(element.getValue()));
				}
			}
			else {
				Object processed = doProcess(obj, this);
				
				context.put(obj, processed);
				context.put(processed, processed);
				
				return context.get(obj);
			}
	
			return obj;
		}
	}
	
	public static abstract class HierarchyTraverser<T> {
		/**
		 * returns either a desired object or null if
		 * given class is not relevant
		 * @param clazz
		 * @return
		 */
		protected abstract T processMatch(Class clazz);
		
		public T traverse(Class clazz) {
			return traverseSuperclass(clazz);
		}
		
		/**
		 * ascends through superclasses
		 * @param clazz
		 * @return
		 */
		protected T traverseSuperclass(Class clazz) {
			T c = null;
			Class tmp = clazz;
			
			while (c == null && tmp != null) {
				c = traverseInterfaces(tmp);
				tmp = tmp.getSuperclass();
			}
			
			return c;
		}

		/**
		 * recursively traverses interfaces. note that there's no particular order
		 * of interfaces and therefore no guaranty of finding a particular one
		 * if several converters potentially exist.
		 * @param clazz
		 * @return
		 */
		protected T traverseInterfaces(Class clazz) {
			T c = this.processMatch(clazz);
			
			if (c != null) {
				return c;
			}
			
			Class[] is = clazz.getInterfaces();
			
			for (int i = 0; i < is.length && c == null; i++) {
				c = traverseInterfaces(is[i]);
			}
			
			return c;
		}
	}
	
	public static Collection<String> getMethodNames(Class clazz) {
		Collection<String> names = new ArrayList<String>();
		for (Method m : clazz.getMethods()) {
			names.add(m.getName());
		}
		return names;
	}
	
	/**
	 * get all declared and inherited fields of {@code clazz} 
	 * and optionally sort them alphabetically. note that superclass fields go first. 
	 * @param target
	 * @param withSort
	 * @return
	 */
	public static Collection<Field> getAllFields(Class clazz, boolean withSort) {
		List<Field> properties = new ArrayList<Field>();
		Class<?> ss = clazz;
		
		do {
			List<Field> currentProps = Arrays.asList(ss.getDeclaredFields());
			
			if (withSort) {
				Collections.sort(currentProps, PROPERTY_COMPARATOR);
			}
			
			properties.addAll(0, currentProps);
			ss = ss.getSuperclass();
		}
		while (ss != null);
		
		return properties;
	}

	public static Collection<LazyAttributeRegistryDescriptor> findClasses(String domain, boolean fieldAccess) {
		Collection<LazyAttributeRegistryDescriptor> classes =
			new ArrayList<LazyAttributeRegistryDescriptor>();

		ClassPathScanningCandidateComponentProvider provider =
			new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AnnotationTypeFilter(javax.persistence.Entity.class));

		for (BeanDefinition component : provider.findCandidateComponents(domain)) {
			LazyAttributeRegistryDescriptor descriptor = introspect(component.getBeanClassName(), fieldAccess);
			
			if (descriptor != null) {
				classes.add(descriptor);
			}
		}

		return classes;
	}
	
	private static LazyAttributeRegistryDescriptor introspect(String className, boolean fieldAccess) {
		try {
			Class<?> cls = Class.forName(className);

			LazyAttributeRegistryDescriptor d = new LazyAttributeRegistryDescriptor();
			d.className = cls.getCanonicalName();
			d.idName = findEntityIdName(cls, fieldAccess);
			d.attributes =
				new NamingOperation<Collection<String>>(new ArrayList<String>(), fieldAccess) {
					@Override
					public boolean isRelevant(AccessibleObject arg0) {
						return isDeclaredLazy(arg0);
					}

					@Override
					public void process(String arg0) {
						result.add(arg0);
					}					
				}.introspect(cls);

			if (!d.attributes.isEmpty()) {
				return d;
			}
		}
		catch (ClassNotFoundException cnfe) {
		}
		
		return null;
	}

	private static Object findEntityIdName(Class<?> clazz, boolean fieldAccess) {
		if (clazz.isAnnotationPresent(javax.persistence.IdClass.class)) {
			LazyAttributeRegistryDescriptor d = new LazyAttributeRegistryDescriptor();
			d.className = clazz.getAnnotation(javax.persistence.IdClass.class).value().getCanonicalName();
			d.attributes =
				new NamingOperation<Collection<String>>(new ArrayList<String>(), fieldAccess) {
					@Override
					public boolean isRelevant(AccessibleObject arg0) {
						return isDeclaredId(arg0);
					}
	
					@Override
					public void process(String arg0) {
						result.add(arg0);
					}					
				}.introspect(clazz);
			return d;
		}
		else {
			return new NamingOperation<String>("", fieldAccess) {
				@Override
				public boolean isRelevant(AccessibleObject arg0) {
					return isDeclaredId(arg0);
				}

				@Override
				public void process(String arg0) {
					result = arg0;
				}
			}.introspect(clazz);
		}
	}

	private static boolean isDeclaredId(AccessibleObject a) {
		return a.isAnnotationPresent(javax.persistence.Id.class)
				|| a.isAnnotationPresent(javax.persistence.EmbeddedId.class);
	}

	private static boolean isDeclaredLazy(AccessibleObject a) {
		javax.persistence.FetchType ft = null;

		if (a.isAnnotationPresent(javax.persistence.Basic.class)) {
			ft = a.getAnnotation(javax.persistence.Basic.class).fetch();
		}
		else if (a.isAnnotationPresent(javax.persistence.OneToOne.class)) {
			ft = a.getAnnotation(javax.persistence.OneToOne.class).fetch();
		}
		else if (a.isAnnotationPresent(javax.persistence.OneToMany.class)) {
			ft = a.getAnnotation(javax.persistence.OneToMany.class).fetch();
		}
		else if (a.isAnnotationPresent(javax.persistence.ManyToOne.class)) {
			ft = a.getAnnotation(javax.persistence.ManyToOne.class).fetch();
		}
		else if (a.isAnnotationPresent(javax.persistence.ManyToMany.class)) {
			ft = a.getAnnotation(javax.persistence.ManyToMany.class).fetch();
		}

		return (ft != null) && (ft == javax.persistence.FetchType.LAZY);
	}

	public static String toUpperCaseFirst(String s) {
		return String.valueOf(s.charAt(0)).toUpperCase().concat(s.substring(1, s.length()));
	}

	public static String toLowerCaseFirst(String s) {
		return String.valueOf(s.charAt(0)).toLowerCase().concat(s.substring(1, s.length()));
	}

	public static boolean isGetter(Method m) {
		return m.getName().startsWith(PREFIX_GET);
	}

	public static boolean isSetter(Method m) {
		return m.getName().startsWith(PREFIX_SET);
	}
	
	public static String getAttributeName(Method m) {
		if (isGetter(m)) {
			return toLowerCaseFirst(m.getName().substring(m.getName().indexOf(PREFIX_GET) + PREFIX_GET.length()));
		}
		else {
			return null;
		}
	}
}