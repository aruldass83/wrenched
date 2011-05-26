package com.wrenched.core.messaging.channels {
	import mx.messaging.MessageAgent;
	import mx.messaging.MessageResponder;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.messages.IMessage;

	/**
	 * simple AMF channel that instruments messages when they arrive.
	 * must be configured in BlazeDS's services-config.xml.
	 * @author konkere
	 */
	public class LazyAMFChannel extends AMFChannel {
		public function LazyAMFChannel(id:String=null, uri:String=null)	{
			super(id, uri);
		}
		
		//TODO: remove proxies in send?
	    
	    override protected function getDefaultMessageResponder(agent:MessageAgent, msg:IMessage):MessageResponder {
	    	return new LazyInstrumentingMessageResponder(agent, msg, this);
	    }
	}
}