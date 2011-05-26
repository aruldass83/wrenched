package com.wrenched.core.services;

import com.wrenched.core.annotations.LazyAttributeDomain;
import com.wrenched.core.services.support.MethodBasedAttributeProvider;

@LazyAttributeDomain("com.wrenched.core.domain1")
public class TestProvider extends MethodBasedAttributeProvider {
}
