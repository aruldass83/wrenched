package com.wrenched.core.messaging.services.remoting.adapters;

import com.wrenched.core.instrumentation.ProxyInstrumentor;

import flex.messaging.messages.Message;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * convenient custom JavaAdapter for blazeDS-exposed services
 * that instruments return values by unwrapping potential proxies.
 * @author konkere
 *
 */
public class InstrumentingJavaAdapter extends JavaAdapter {
	private final ProxyInstrumentor instrumentor;

	public InstrumentingJavaAdapter() {
		//find an appropriate instrumentor using jar discovery
		instrumentor = (ProxyInstrumentor)FactoryFinder.find(ProxyInstrumentor.class.getCanonicalName());				
	}
	
	/*
	 * (non-Javadoc)
	 * @see flex.messaging.services.remoting.adapters.JavaAdapter#invoke(flex.messaging.messages.Message)
	 */
	@Override
	public Object invoke(Message message) {
		Object result = super.invoke(message);

		if (instrumentor != null) {
			try {
				return instrumentor.unwrap(result);
			}
			catch (NoSuchFieldException nsfe) {
				return result;
			}
		}
		else {
			return result;
		}
	}
}
