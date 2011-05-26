package com.wrenched.core.exchange;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.wrenched.core.annotations.Externalizable;
import com.wrenched.core.domain.AbstractExternalizableEntity;


/**
 * 
 */
@Externalizable
public class TestPlainEntity2 {
	private int a1;
	private boolean a2;
	private final TestEnum a3 = TestEnum.TRK;
	protected Serializable a4;
	public String[] a5;
	public List<Integer> a6;
	public Map<Integer, Object> a7;
	
	public int getA1() {
		return a1;
	}
	public void setA1(int a1) {
		this.a1 = a1;
	}
	public boolean isA2() {
		return a2;
	}
	public void setA2(boolean a2) {
		this.a2 = a2;
	}
	public TestEnum getA3() {
		return a3;
	}
	public Serializable getA4() {
		return a4;
	}
	public void setA4(Serializable a4) {
		this.a4 = a4;
	}
	public String[] getA5() {
		return a5;
	}
	public void setA5(String[] a5) {
		this.a5 = a5;
	}
	public List<Integer> getA6() {
		return a6;
	}
	public void setA6(List<Integer> a6) {
		this.a6 = a6;
	}
	public Map<Integer, Object> getA7() {
		return a7;
	}
	public void setA7(Map<Integer, Object> a7) {
		this.a7 = a7;
	}
}