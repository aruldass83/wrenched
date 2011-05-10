package com.wrenched.example.domain;

import javax.persistence.Basic;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.wrenched.core.annotations.Externalizable;

@Entity
@Externalizable
public class TestEntity2 {
	private TestPK id;
	private byte[] attribute1;
	private String attribute2;
	private TestEntity parent;

	@EmbeddedId
	public TestPK getId() {
		return id;
	}
	public void setId(TestPK id) {
		this.id = id;
	}
	
	@Basic
	@Lob
	public byte[] getAttribute1() {
		return attribute1;
	}
	public void setAttribute1(byte[] attribute1) {
		this.attribute1 = attribute1;
	}
	
	@Basic
	public String getAttribute2() {
		return attribute2;
	}
	public void setAttribute2(String attribute2) {
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
