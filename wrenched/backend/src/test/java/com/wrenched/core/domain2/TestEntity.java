package com.wrenched.core.domain2;

import java.util.List;

public class TestEntity {
	public Integer id;
	private Object attribute1;
	private Object attribute2;
	private List<TestEntity2> children;

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
	public List<TestEntity2> getChildren() {
		return children;
	}
	public void setChildren(List<TestEntity2> children) {
		this.children = children;
	}
}
