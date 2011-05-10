package com.wrenched.core.exchange;

import java.util.List;

import com.wrenched.core.domain.AbstractExternalizableEntity;


/**
 * 
 */

public class TestPlainEntity {
	private int a1;
	private boolean a2;
	private final TestEnum a3 = TestEnum.TRK;
	protected Object a4;
	public String[] a5;
	public List<Integer> a6;
	
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
	public Object getA4() {
		return a4;
	}
	public void setA4(Object a4) {
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
}