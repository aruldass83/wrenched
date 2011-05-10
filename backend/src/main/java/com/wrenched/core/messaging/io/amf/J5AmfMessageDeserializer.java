package com.wrenched.core.messaging.io.amf;

import java.io.InputStream;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfTrace;

public class J5AmfMessageDeserializer extends AmfMessageDeserializer {
	@Override
	public void initialize(SerializationContext context, InputStream in,
			AmfTrace trace) {
		super.initialize(context, in, trace);
		
        amfIn = new J5Amf0Input(context);
        amfIn.setInputStream(in);
	}
}
