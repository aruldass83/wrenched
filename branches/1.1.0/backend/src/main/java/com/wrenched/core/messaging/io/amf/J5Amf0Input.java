package com.wrenched.core.messaging.io.amf;

import java.io.IOException;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf0Input;
import flex.messaging.io.amf.AmfTypes;

public class J5Amf0Input extends Amf0Input {
	public J5Amf0Input(SerializationContext context) {
		super(context);
	}

	@Override protected Object readObjectValue(final int type)
    	throws ClassNotFoundException, IOException {
		
		if (AmfTypes.kAvmPlusObjectType  == type && avmPlusInput == null) {
			avmPlusInput = new J5Amf3Input(context);
			avmPlusInput.setDebugTrace(trace);
			avmPlusInput.setInputStream(in);
		}
		
		return super.readObjectValue(type);
	}

}
