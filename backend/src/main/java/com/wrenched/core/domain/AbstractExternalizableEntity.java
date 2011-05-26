package com.wrenched.core.domain;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.wrenched.core.externalization.Externalizer;


/**
 * convenient superclass that is Externalizable and delegates
 * all externalization work to Externalizer.
 * @author konkere
 *
 */
public abstract class AbstractExternalizableEntity implements Externalizable {
	public void readExternal(ObjectInput in) throws IOException {
		Externalizer.getInstance().readExternal(this, in);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		Externalizer.getInstance().writeExternal(this, out);
	}
}
