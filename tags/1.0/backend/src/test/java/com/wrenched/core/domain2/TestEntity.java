package com.wrenched.core.domain2;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;

@Entity
@IdClass(TestPK.class)
public class TestEntity {
	private String id1;
	private String id2;
	private Object attribute;
	private List<TestEntity2> children;
	
	@Id
	public String getId1() {
		return id1;
	}
	public void setId1(String id1) {
		this.id1 = id1;
	}
	@Id
	public String getId2() {
		return id2;
	}
	public void setId2(String id2) {
		this.id2 = id2;
	}
	@Basic(fetch=FetchType.LAZY)
	public Object getAttribute() {
		return attribute;
	}
	public void setAttribute(Object attribute) {
		this.attribute = attribute;
	}
	
	@OneToMany(fetch=FetchType.LAZY)
	public List<TestEntity2> getChildren() {
		return children;
	}
	public void setChildren(List<TestEntity2> children) {
		this.children = children;
	}
}
