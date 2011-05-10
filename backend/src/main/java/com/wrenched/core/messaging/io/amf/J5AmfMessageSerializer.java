package com.wrenched.core.messaging.io.amf;

import java.io.OutputStream;

import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;

public class J5AmfMessageSerializer extends AmfMessageSerializer {
    @Override
	public void initialize(SerializationContext context, OutputStream out,
			AmfTrace trace) {
		amfOut = new J5Amf0Output(context);
        amfOut.setAvmPlus(version >= MessageIOConstants.AMF3);
        amfOut.setOutputStream(out);

        debugTrace = trace;
        isDebug = trace != null;
        amfOut.setDebugTrace(debugTrace);
	}
    
    public void setSerializationContext(final SerializationContext context) {
    	amfOut = new J5Amf0Output(context);
    }
}
