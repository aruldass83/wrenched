package com.wrenched.core.services;

import com.wrenched.core.services.support.MethodBasedAttributeProvider;

public class TestProvider2 extends MethodBasedAttributeProvider {
	@Override
	public String getDomain() {
		return "com.wrenched.core.domain2";
	}

}
