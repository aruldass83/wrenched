package com.wrenched.example.services;

import java.io.Serializable;
import java.util.Arrays;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.wrenched.core.annotations.LazyAttributeDomain;
import com.wrenched.core.annotations.LazyAttributeFetcher;
import com.wrenched.core.annotations.LazyAttributeProvider;
import com.wrenched.core.annotations.LazyAttributeProviderType;
import com.wrenched.example.domain.TestEntity;
import com.wrenched.example.domain.TestEntity2;
import com.wrenched.example.domain.TestPK;

@LazyAttributeProvider(LazyAttributeProviderType.PERSISTENCE)
@LazyAttributeDomain("com.wrenched.example.domain")
public class HibernateLoader extends HibernateDaoSupport {
    @LazyAttributeFetcher
    public Object loadEntity(Class<?> clazz, Object id) {
        return getHibernateTemplate().load(clazz, (Serializable)id);
    }
    
    public Object getTestEntity(TestPK id) throws NoSuchFieldException {
    	return loadEntity(TestEntity.class, id);
    }
    
    @Override
    protected void initDao() {
		TestEntity te3 = new TestEntity();
		te3.setId1("0");
		te3.setId2("1");
		te3.setAttribute(new Double(666));

		TestEntity2 te11 = new TestEntity2();
		te11.setId(new TestPK("1", "1"));
		te11.setAttribute1(new byte[] {});
		te11.setAttribute2("test1");
		te11.setParent(te3);
		TestEntity2 te12 = new TestEntity2();
		te12.setId(new TestPK("1", "2"));
		te12.setAttribute1(new byte[] {});
		te12.setAttribute2("test2");
		te12.setParent(te3);
		TestEntity2 te13 = new TestEntity2();
		te13.setId(new TestPK("1", "3"));
		te13.setAttribute1(new byte[] {});
		te13.setAttribute2("test3");
		te13.setParent(te3);
		
		this.getHibernateTemplate().saveOrUpdate(te3);
		this.getHibernateTemplate().saveOrUpdate(te11);
		this.getHibernateTemplate().saveOrUpdate(te12);
		this.getHibernateTemplate().saveOrUpdate(te13);
    }
}
