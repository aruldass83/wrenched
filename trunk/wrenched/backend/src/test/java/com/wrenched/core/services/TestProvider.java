package com.wrenched.core.services;

import com.wrenched.core.services.support.PersistenceBasedAttributeProvider;

public class TestProvider extends PersistenceBasedAttributeProvider {
	public TestProvider() {
		super.setDomain("eu");
	}
}
