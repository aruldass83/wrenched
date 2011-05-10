package com.wrenched.example.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

import com.wrenched.core.annotations.Externalizable;

@Embeddable
@Externalizable
public class TestPK implements Serializable {
	private String id1;
	private String id2;
	
	public TestPK() {}
	
	public TestPK(String arg0, String arg1) {
		id1 = arg0;
		id2 = arg1;
	}
	
	public String getId1() {
		return id1;
	}
	public void setId1(String id1) {
		this.id1 = id1;
	}
	public String getId2() {
		return id2;
	}
	public void setId2(String id2) {
		this.id2 = id2;
	}
	
	public boolean equals(Object other) {
		return (other instanceof TestPK) &&
			(((id1 == null && ((TestPK) other).getId1() == null) &&
					(id2 == null && ((TestPK) other).getId2() == null)) ||
			(id1.equals(((TestPK) other).getId1()) && id2.equals(((TestPK) other).getId2())));
	}
	
	public int hashCode() {
		return (id1 + id2).hashCode();
	}
}
