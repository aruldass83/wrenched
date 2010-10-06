////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2005-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package com.wrenched.core.messaging.channels
{
	import com.wrenched.core.lazy.LazyAttributeRegistry;
	
	import mx.core.mx_internal;
	import mx.messaging.MessageAgent;
	import mx.messaging.MessageResponder;
	import mx.messaging.channels.NetConnectionChannel;
	import mx.messaging.events.ChannelEvent;
	import mx.messaging.events.ChannelFaultEvent;
	import mx.messaging.messages.AcknowledgeMessage;
	import mx.messaging.messages.AsyncMessage;
	import mx.messaging.messages.ErrorMessage;
	import mx.messaging.messages.IMessage;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	
	use namespace mx_internal;
	
	[ResourceBundle("messaging")]
	
	/**
	 *  @private
	 *  This class provides the responder level interface for dispatching message
	 *  results from a remote destination.
	 *  The NetConnectionChannel creates this handler to manage
	 *  the results of a pending operation started when a message is sent.
	 *  The message handler is always associated with a MessageAgent
	 *  (the object that sent the message) and calls its <code>fault()</code>,
	 *  <code>acknowledge()</code>, or <code>message()</code> method as appopriate.
	 */
	internal class LazyInstrumentingMessageResponder extends MessageResponder
	{
	    //--------------------------------------------------------------------------
	    //
	    // Constructor
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     *  Initializes this instance of the message responder with the specified
	     *  agent.
	     *
	     *  @param agent MessageAgent that this responder should call back when a
	     *            message is received.
	     *
	     *  @param msg The outbound message.
	     *
	     *  @param channel The channel this responder is using.
	     */
	    public function LazyInstrumentingMessageResponder(agent:MessageAgent,
	                                    msg:IMessage, channel:NetConnectionChannel)
	    {
	        super(agent, msg, channel);
	        channel.addEventListener(ChannelEvent.DISCONNECT, channelDisconnectHandler);
	        channel.addEventListener(ChannelFaultEvent.FAULT, channelFaultHandler);
	    }
	
	    //--------------------------------------------------------------------------
	    //
	    // Variables
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     * @private
	     */
	    private var resourceManager:IResourceManager =
	                                    ResourceManager.getInstance();
	
	    //--------------------------------------------------------------------------
	    //
	    // Overridden Methods
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     *  @private
	     *  Called when the result of sending a message is received.
	     *
	     *  @param msg NetConnectionChannel-specific message data.
	     */
	    override protected function resultHandler(msg:IMessage):void
	    {
	        disconnect();
	        if (msg is AsyncMessage)
	        {
	            if (AsyncMessage(msg).correlationId == message.messageId)
	            {
	            	if (LazyAttributeRegistry.initialized) {
		            	//try to create proxies for all instances of known classes in the message body
						msg.body = LazyAttributeRegistry.instance().createProxy(msg.body);
	            	}
	                agent.acknowledge(msg as AcknowledgeMessage, message);
	            }
	            else
	            {
	                errorMsg = new ErrorMessage();
	                errorMsg.faultCode = "Server.Acknowledge.Failed";
	                errorMsg.faultString = resourceManager.getString(
	                    "messaging", "ackFailed");
	                errorMsg.faultDetail = resourceManager.getString(
	                    "messaging", "ackFailed.details",
	                    [ message.messageId, AsyncMessage(msg).correlationId ]);
	                errorMsg.correlationId = message.messageId;
	                agent.fault(errorMsg, message);
	                //@TODO: need to add constants here
	            }
	        }
	        else
	        {
	            var errorMsg:ErrorMessage;
	            errorMsg = new ErrorMessage();
	            errorMsg.faultCode = "Server.Acknowledge.Failed";
	            errorMsg.faultString = resourceManager.getString(
	                "messaging", "noAckMessage");
	            errorMsg.faultDetail = resourceManager.getString(
	                "messaging", "noAckMessage.details",
	                [ msg ? msg.toString() : "null" ]);
	            errorMsg.correlationId = message.messageId;
	            agent.fault(errorMsg, message);
	        }
	    }
	
	    /**
	     *  @private
	     *  Called when the current invocation fails.
	     *  Passes the fault information on to the associated agent that made
	     *  the request.
	     *
	     *  @param msg NetConnectionMessageResponder status information.
	     */
	    override protected function statusHandler(msg:IMessage):void
	    {
	        disconnect();
	
	        // even a fault is still an acknowledgement of a message sent so pass it on...
	        if (msg is AsyncMessage)
	        {
	            if (AsyncMessage(msg).correlationId == message.messageId)
	            {
	                // pass the ack on...
	                var ack:AcknowledgeMessage = new AcknowledgeMessage();
	                ack.correlationId = AsyncMessage(msg).correlationId;
	                ack.headers[AcknowledgeMessage.ERROR_HINT_HEADER] = true; // add a hint this is for an error
	                agent.acknowledge(ack, message);
	                // send the fault on...
	                agent.fault(msg as ErrorMessage, message);
	            }
	            else if (msg is ErrorMessage)
	            {
	                // we can't find a correlation id but do have some sort of error message so just forward
	                agent.fault(msg as ErrorMessage, message);
	            }
	            else
	            {
	                var errorMsg:ErrorMessage;
	                errorMsg = new ErrorMessage();
	                errorMsg.faultCode = "Server.Acknowledge.Failed";
	                errorMsg.faultString = resourceManager.getString(
	                    "messaging", "noErrorForMessage");
	                errorMsg.faultDetail = resourceManager.getString(
	                    "messaging", "noErrorForMessage.details",
	                    [ message.messageId, AsyncMessage(msg).correlationId ]);
	                errorMsg.correlationId = message.messageId;
	                agent.fault(errorMsg, message);
	            }
	        }
	        else
	        {
	            errorMsg = new ErrorMessage();
	            errorMsg.faultCode = "Server.Acknowledge.Failed";
	            errorMsg.faultString = resourceManager.getString(
	                "messaging", "noAckMessage");
	            errorMsg.faultDetail = resourceManager.getString(
	                "messaging", "noAckMessage.details",
	                [ msg ? msg.toString(): "null" ]);
	            errorMsg.correlationId = message.messageId;
	            agent.fault(errorMsg, message);
	        }
	    }
	
	    //--------------------------------------------------------------------------
	    //
	    // Overridden Protected Methods
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     *  @private
	     *  Handle a request timeout by removing ourselves as a listener on the
	     *  NetConnection and faulting the message to the agent.
	     */
	    override protected function requestTimedOut():void
	    {
	        disconnect();
	        statusHandler(createRequestTimeoutErrorMessage());
	    }
	
	    //--------------------------------------------------------------------------
	    //
	    // Protected Methods
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     *  @private
	     *  Handles a disconnect of the underlying Channel before a response is
	     *  returned to the responder.
	     *  The sent message is faulted and flagged with the ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT
	     *  code.
	     *
	     *  @param event The DISCONNECT event.
	     */
	    protected function channelDisconnectHandler(event:ChannelEvent):void
	    {
	        disconnect();
	        var errorMsg:ErrorMessage = new ErrorMessage();
	        errorMsg.correlationId = message.messageId;
	        errorMsg.faultString = resourceManager.getString(
	            "messaging", "deliveryInDoubt");
	        errorMsg.faultDetail = resourceManager.getString
	            ("messaging", "deliveryInDoubt.details");
	        errorMsg.faultCode = ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT;
	        agent.fault(errorMsg, message);
	    }
	
	    /**
	     *  @private
	     *  Handles a fault of the underlying Channel before a response is
	     *  returned to the responder.
	     *  The sent message is faulted and flagged with the ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT
	     *  code.
	     *
	     *  @param event The ChannelFaultEvent.
	     */
	    protected function channelFaultHandler(event:ChannelFaultEvent):void
	    {
	        disconnect();
	        var errorMsg:ErrorMessage = event.createErrorMessage();
	        errorMsg.correlationId = message.messageId;
	        // if the channel is no longer connected then we don't really
	        // know if the message made it to the server.
	        if (!event.channel.connected)
	        {
	            errorMsg.faultCode = ErrorMessage.MESSAGE_DELIVERY_IN_DOUBT;
	        }
	        agent.fault(errorMsg, message);
	    }
	
	    //--------------------------------------------------------------------------
	    //
	    // Private Methods
	    //
	    //--------------------------------------------------------------------------
	
	    /**
	     *  @private
	     *  Disconnects the responder from the underlying Channel.
	     */
	    private function disconnect():void
	    {
	        channel.removeEventListener(ChannelEvent.DISCONNECT, channelDisconnectHandler);
	        channel.removeEventListener(ChannelFaultEvent.FAULT, channelFaultHandler);
	    }
	}
}