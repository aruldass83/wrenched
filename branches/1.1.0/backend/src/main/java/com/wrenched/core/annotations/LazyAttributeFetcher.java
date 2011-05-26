package com.wrenched.core.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD})
@Retention(RUNTIME)
public @interface LazyAttributeFetcher {
	public static final String SELF = "self";
	Class<?> targetClass() default Void.class;
	String idName() default SELF;
	String attributeName() default "";
}
