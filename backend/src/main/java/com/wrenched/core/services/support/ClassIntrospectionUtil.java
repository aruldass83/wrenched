package com.wrenched.core.services.support;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;

public class ClassIntrospectionUtil {
	private static abstract class ClassOperation<T> {
		T result;
		Class<?> clazz;

		ClassOperation(T arg0, Class<?> arg1) {
			result = arg0;
			clazz = arg1;
		}

		abstract boolean isSatisfied(AccessibleObject arg0);

		abstract void process(String arg0);

		T introspect(boolean arg2) {
			if (arg2) {
				for (Field f : clazz.getDeclaredFields()) {
					if (isSatisfied(f)) {
						process(f.getName());
					}
				}
			}
			else {
				for (Method m : clazz.getMethods()) {
					if (isSatisfied(m)) {
						process(toLowerCaseFirst(m.getName().replaceFirst("get", "")));
					}
				}
			}

			return result;
		}
	}
	
	public static Collection<String> getMethodNames(Class clazz) {
		Collection<String> names = new ArrayList<String>();
		for (Method m : clazz.getMethods()) {
			names.add(m.getName());
		}
		return names;
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
				new ClassOperation<Collection<String>>(new ArrayList<String>(), cls) {
					@Override
					public boolean isSatisfied(AccessibleObject arg0) {
						return isDeclaredLazy(arg0);
					}

					@Override
					public void process(String arg0) {
						result.add(arg0);
					}
				}.introspect(fieldAccess);

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
				new ClassOperation<Collection<String>>(new ArrayList<String>(), clazz) {
					@Override
					public boolean isSatisfied(AccessibleObject arg0) {
						return isDeclaredId(arg0);
					}
	
					@Override
					public void process(String arg0) {
						result.add(arg0);
					}
				}.introspect(fieldAccess);
			return d;
		}
		else {
			return new ClassOperation<String>("", clazz) {
				@Override
				public boolean isSatisfied(AccessibleObject arg0) {
					return isDeclaredId(arg0);
				}

				@Override
				public void process(String arg0) {
					result = arg0;
				}
			}.introspect(fieldAccess);
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

	static String toUpperCaseFirst(String s) {
		return String.valueOf(s.charAt(0)).toUpperCase().concat(s.substring(1, s.length()));
	}

	static String toLowerCaseFirst(String s) {
		return String.valueOf(s.charAt(0)).toLowerCase().concat(s.substring(1, s.length()));
	}
}