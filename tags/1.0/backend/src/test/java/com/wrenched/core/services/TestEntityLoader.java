package com.wrenched.core.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeDomain;
import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.annotations.LazyAttributeProvider;
import com.wrenched.core.annotations.LazyAttributeProviderType;
import com.wrenched.core.domain1.TestEntity;

@LazyAttributeProvider(LazyAttributeProviderType.METHOD)
@LazyAttributeDomain("com.wrenched.core.domain1")
public class TestEntityLoader {
	private Map<Object, Object> cache = new HashMap<Object, Object>();
	
	public TestEntityLoader() {
		TestEntity te1 = new TestEntity();
		te1.id = 1;
		te1.setAttribute1("blahblah");
		te1.setAttribute2(new Double(666));
		
		TestEntity te2 = new TestEntity();
		te2.id = 2;
		te2.setAttribute1(new byte[] {});
		te2.setAttribute2("test1");

		TestEntity te3 = new TestEntity();
		te3.id = 3;
		te3.setAttribute1(new byte[] {});
		te3.setAttribute2("test2");
		te3.setChildren(Arrays.asList(new TestEntity[] {te1, te2}));

		te1.setParent(te3);
		te2.setParent(te3);
		
		cache.put(te1.id, te1);
		cache.put(te2.id, te2);
		cache.put(te3.id, te3);
	}
	
	public Object load(Class<?> clazz, Object id) {
		return cache.get(id);
	}
	
	@LazyAttributeFetcher(targetClass=TestEntity.class, idName="id", attributeName="parent")
	public Object getTestParent(Integer id) {
		return ((TestEntity)cache.get(id)).getParent();
	}
	
	@LazyAttributeFetcher(targetClass=TestEntity.class, idName="id", attributeName="attribute1")
	public Object getAttribute1(Integer id) {
		return ((TestEntity)cache.get(id)).getAttribute1();
	}

	@LazyAttributeFetcher(targetClass=TestEntity.class, idName="id", attributeName="attribute2")
	public Object getAttribute2(Integer id) {
		return ((TestEntity)cache.get(id)).getAttribute2();
	}

	@LazyAttributeFetcher(targetClass=TestEntity.class, idName="id", attributeName="children")
	public List<TestEntity> getTestChildren(Integer id) {
		return ((TestEntity)cache.get(id)).getChildren();
	}
}
