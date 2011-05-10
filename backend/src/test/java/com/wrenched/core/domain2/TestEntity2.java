package com.wrenched.core.domain2;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class TestEntity2 {
	private TestPK id;
	private Object attribute1;
	private Object attribute2;
	private TestEntity parent;

	@EmbeddedId
	public TestPK getId() {
		return id;
	}
	public void setId(TestPK id) {
		this.id = id;
	}
	public Object getAttribute1() {
		return attribute1;
	}
	public void setAttribute1(Object attribute1) {
		this.attribute1 = attribute1;
	}
	public Object getAttribute2() {
		return attribute2;
	}
	public void setAttribute2(Object attribute2) {
		this.attribute2 = attribute2;
	}
	@ManyToOne(fetch=FetchType.LAZY)
	public TestEntity getParent() {
		return parent;
	}
	public void setParent(TestEntity parent) {
		this.parent = parent;
	}

}
