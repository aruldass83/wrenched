package com.wrenched.core.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.domain2.TestEntity2;
import com.wrenched.core.domain2.TestPK;

public class TestEntityLoader {
	private Map<Object, Object> cache = new HashMap<Object, Object>();
	
	public TestEntityLoader() {
		com.wrenched.core.domain1.TestEntity te1 = new com.wrenched.core.domain1.TestEntity();
		te1.id = 1;
		te1.setAttribute1("blahblah");
		te1.setAttribute2(new Double(666));
		
		com.wrenched.core.domain1.TestEntity te2 = new com.wrenched.core.domain1.TestEntity();
		te2.id = 2;
		te2.setAttribute1(new byte[] {});
		te2.setAttribute2("test");

		com.wrenched.core.domain2.TestEntity te3 = new com.wrenched.core.domain2.TestEntity();
		te3.setId1("0");
		te3.setId2("1");
		te3.setAttribute(new Double(666));

		TestEntity2 te11 = new TestEntity2();
		te11.setId(new TestPK("1", "1"));
		te11.setAttribute1(new byte[] {});
		te11.setAttribute2("test1");
		te11.setParent(te3);
		TestEntity2 te12 = new TestEntity2();
		te12.setId(new TestPK("1", "2"));
		te12.setAttribute1(new byte[] {});
		te12.setAttribute2("test2");
		te12.setParent(te3);
		TestEntity2 te13 = new TestEntity2();
		te13.setId(new TestPK("1", "3"));
		te13.setAttribute1(new byte[] {});
		te13.setAttribute2("test3");
		te13.setParent(te3);
		
		te3.setChildren(Arrays.asList(new TestEntity2[]{te11, te12, te13}));
		
		cache.put(te1.id, te1);
		cache.put(te2.id, te2);
		cache.put(new TestPK(te3.getId1(), te3.getId2()), te3);
		cache.put(te11.getId(), te11);
		cache.put(te12.getId(), te12);
		cache.put(te13.getId(), te13);
	}
	
	public Object load(Class<?> clazz, Object id) {
		return cache.get(id);
	}

	
	@LazyAttributeFetcher(targetClass=TestEntity2.class, attributeName="parent")
	public Object getTestParent(TestPK id) {
		return ((TestEntity2)cache.get(id)).getParent();
	}

	public List<TestEntity2> getTestChildren(TestPK id) {
		return ((com.wrenched.core.domain2.TestEntity)cache.get(id)).getChildren();
	}
}
