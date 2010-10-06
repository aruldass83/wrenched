package com.wrenched.core.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wrenched.core.domain2.TestEntity;
import com.wrenched.core.domain2.TestEntity2;


public class TestEntityLoader2 {
	private Map<Integer, TestEntity> cache = new HashMap<Integer, TestEntity>();
	
	public TestEntityLoader2() {
		TestEntity te1 = new TestEntity();
		te1.id = 1;
		te1.setAttribute1("blahblah");
		te1.setAttribute2(new Double(666));
		
		TestEntity2 te11 = new TestEntity2();
		te11.id = 2;
		te11.setAttribute1(new byte[] {});
		te11.setAttribute2("test1");
		te11.setParent(te1);
		TestEntity2 te12 = new TestEntity2();
		te12.id = 3;
		te12.setAttribute1(new byte[] {});
		te12.setAttribute2("test2");
		te12.setParent(te1);
		TestEntity2 te13 = new TestEntity2();
		te13.id = 4;
		te13.setAttribute1(new byte[] {});
		te13.setAttribute2("test3");
		te13.setParent(te1);
		
		te1.setChildren(Arrays.asList(new TestEntity2[]{te11, te12, te13}));
		
		cache.put(1, te1);
	}
	
	public List<TestEntity2> getTestChildren(Integer id) {
		return cache.get(id).getChildren();
	}
}
