package com.wrenched.core.domain;

import com.wrenched.core.domain.AbstractExternalizableEntity;

/**
 * POJO that is used by LAL mechanism for exchanging fetched
 * attribute values 
 * @author konkere
 *
 */
public class LazyAttribute extends AbstractExternalizableEntity {
	private String entityName;
	private Object entityId;
	private String attributeName;
	private Object attributeValue;
	
	public LazyAttribute(String en, Object ei, String an, Object av) {
		this.entityName = en;
		this.entityId = ei;
		this.attributeName = an;
		this.attributeValue = av;
	}
	
	/**
	 * qualified class name of the entity this attribute belongs to
	 * @return
	 */
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	/**
	 * value of the id field of a certaing entity instance. used for both search
	 * and validation
	 * @return
	 */
	public Object getEntityId() {
		return entityId;
	}
	public void setEntityId(Object entityId) {
		this.entityId = entityId;
	}
	
	/**
	 * an entity attribute name that has FetchType.LAZY
	 * @return
	 */
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	/**
	 * fetched value of this lazy attribute
	 * @return
	 */
	public Object getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(Object attributeValue) {
		this.attributeValue = attributeValue;
	}
}
