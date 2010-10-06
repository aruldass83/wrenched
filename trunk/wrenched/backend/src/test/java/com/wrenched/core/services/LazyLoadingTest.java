package com.wrenched.core.services;

import java.util.List;
import java.util.Set;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.services.LazyAttributeLoader;


public class LazyLoadingTest extends AbstractDependencyInjectionSpringContextTests {
	private LazyAttributeLoader loader;
	
	public String[] getConfigLocations() {
		return new String[] {"classpath:testLALContext.xml"}; 
	}
	
	public void testDirectLoading() throws IllegalAccessException {
		LazyAttribute attribute11 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(1), "attribute1");
		LazyAttribute attribute12 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(1), "attribute2");
		LazyAttribute attribute21 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(2), "attribute1");
		LazyAttribute attribute22 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(2), "attribute2");
		
		assertEquals("blahblah", attribute11.getAttributeValue());
		assertEquals(666d, attribute12.getAttributeValue());
		assertTrue(attribute21.getAttributeValue() instanceof byte[]);
		assertEquals("test", attribute22.getAttributeValue());
	}

	public void testMethodLoading() throws IllegalAccessException {
		LazyAttribute attribute = loader.loadAttribute("com.wrenched.core.domain2.TestEntity", new Integer(1), "children");
		
		assertTrue(attribute.getAttributeValue() instanceof List);
	}

	public void setLazyAttributeLoaderService(LazyAttributeLoader s) {
		this.loader = s;
	}
	
}
