package com.wrenched.core.domain;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

/**
 * convenient decorator for externalizing enums. externalizable representation
 * is string name of enums.
 * @author konkere
 *
 */
public class EnumHolder implements Externalizable {
	private Class<? extends Enum> enumClass;
	private String name;
	
	public EnumHolder() {
		
	}
	
	public EnumHolder(final Enum<?> value) {
    	this.enumClass = value.getClass();
    	this.name = value.name();
	}
	
	public EnumHolder(final Class<? extends Enum> enumClass) {
		this.enumClass = enumClass;
	}
	
	public Class<? extends Enum> getEnumClass() {
		return this.enumClass;
	}
	
	public Enum<?> enumValue() {
		return Enum.valueOf(this.enumClass, this.name);
	}
	
    public void writeExternal(final ObjectOutput out) throws IOException {
    	out.writeObject(name);
    }
    
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    	name = (String)in.readObject();
    }
}