package com.wrenched.core.messaging.io.amf;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf0Output;

public class J5Amf0Output extends Amf0Output {
	
	public J5Amf0Output(final SerializationContext context) {
		super(context);
	}

	@Override
	protected void createAMF3Output() {
		avmPlusOutput = new J5Amf3Output(context);
		avmPlusOutput.setOutputStream(out);
		avmPlusOutput.setDebugTrace(trace);
	}
}
