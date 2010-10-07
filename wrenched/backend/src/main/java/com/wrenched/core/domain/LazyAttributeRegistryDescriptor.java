package com.wrenched.core.domain;

import java.util.Collection;

/**
 * information container for init-time data exchange. will normally hold
 * class registration data to be used by LazyAttributeRegistry to register
 * proxied classes.
 * @author konkere
 *
 */
public class LazyAttributeRegistryDescriptor {
	public String className;
	public Object idName;
	public Collection<String> attributes;

	public String toString() {
		return className + "#" + (idName != null ? idName.toString() : "")
				+ "#" + attributes.toString();
	}
}