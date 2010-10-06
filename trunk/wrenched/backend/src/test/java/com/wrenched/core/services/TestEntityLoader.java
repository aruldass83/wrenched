package com.wrenched.core.services;

import java.util.HashMap;
import java.util.Map;

import com.wrenched.core.domain1.TestEntity;


public class TestEntityLoader {
	private Map<Integer, TestEntity> cache = new HashMap<Integer, TestEntity>();
	
	public TestEntityLoader() {
		TestEntity te1 = new TestEntity();
		te1.id = 1;
		te1.setAttribute1("blahblah");
		te1.setAttribute2(new Double(666));
		
		TestEntity te2 = new TestEntity();
		te2.id = 2;
		te2.setAttribute1(new byte[] {});
		te2.setAttribute2("test");
		
		cache.put(1, te1);
		cache.put(2, te2);
	}
	
	public Object load(Class<?> clazz, Object id) {
		return cache.get(id);
	}
}
