package com.wrenched.core.services;

import java.util.List;
import java.util.Set;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.wrenched.core.domain.LazyAttribute;
import com.wrenched.core.domain2.TestPK;
import com.wrenched.core.services.LazyAttributeLoader;
import com.wrenched.core.services.support.ClassIntrospectionUtil;


public class LazyLoadingTest2 extends AbstractDependencyInjectionSpringContextTests {
	private LazyAttributeLoader loader;
	
	public String[] getConfigLocations() {
		return new String[] {"classpath:testLALContext2.xml"}; 
	}
	
	public void testDomainIntrospection() {
		System.out.println(ClassIntrospectionUtil.findClasses("com.wrenched.core.domain2", false));
	}
	
	public void testDirectLoading() throws IllegalAccessException {
		LazyAttribute attribute11 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(1), "attribute1");
		LazyAttribute attribute12 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(1), "attribute2");
		LazyAttribute attribute21 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(2), "attribute1");
		LazyAttribute attribute22 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(2), "attribute2");
		LazyAttribute attribute = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(3), "children");
		LazyAttribute attribute2 = loader.loadAttribute("com.wrenched.core.domain1.TestEntity", new Integer(2), "parent");
		
		assertTrue(attribute.getAttributeValue() instanceof List);
		assertTrue(attribute2.getAttributeValue() instanceof com.wrenched.core.domain1.TestEntity);
		
		assertEquals("blahblah", attribute11.getAttributeValue());
		assertEquals(666d, attribute12.getAttributeValue());
		assertTrue(attribute21.getAttributeValue() instanceof byte[]);
		assertEquals("test1", attribute22.getAttributeValue());
	}

	public void testMethodLoading() throws IllegalAccessException {
		LazyAttribute attribute = loader.loadAttribute("com.wrenched.core.domain2.TestEntity", new TestPK("0","1"), "children");
		
		assertTrue(attribute.getAttributeValue() instanceof List);

		LazyAttribute attribute2 = loader.loadAttribute("com.wrenched.core.domain2.TestEntity2", new TestPK("1","2"), "parent");
		
		assertTrue(attribute2.getAttributeValue() instanceof com.wrenched.core.domain2.TestEntity);
	}

	public void setLazyAttributeLoaderService(LazyAttributeLoader s) {
		this.loader = s;
	}
	
}
