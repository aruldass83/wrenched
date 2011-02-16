package com.wrenched.core.domain1;

import java.util.List;

public class TestEntity {
	public Integer id;
	private Object attribute1;
	private Object attribute2;
	private List<TestEntity> children;
	private TestEntity parent;

	public Object getAttribute1() {
		return attribute1;
	}
	public void setAttribute1(Object arrtibute1) {
		this.attribute1 = arrtibute1;
	}
	public Object getAttribute2() {
		return attribute2;
	}
	public void setAttribute2(Object attribute2) {
		this.attribute2 = attribute2;
	}
	public List<TestEntity> getChildren() {
		return children;
	}
	public void setChildren(List<TestEntity> children) {
		this.children = children;
	}
	public TestEntity getParent() {
		return parent;
	}
	public void setParent(TestEntity parent) {
		this.parent = parent;
	}
}
