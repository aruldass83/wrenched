package com.wrenched.core.remoting {
	import mx.controls.Alert;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;

	/**
	 * simple responder that delegates result handling to
	 * an external method
	 * @author konkere
	 */
	public class DelegatingResponder implements IResponder {
		private var target:Object;
		private var handler:Function
		
		public function DelegatingResponder(comp:Object, func:Function) {
			this.target = comp;
			this.handler = func;
		}

		public function result(data:Object):void {
			this.handler.call(this.target, data as ResultEvent);
		}
		
		public function fault(info:Object):void	{
			var event:FaultEvent = info as FaultEvent;
			Alert.show("Msg: " + event.message, "Fault: " + event.fault);
		}
		
	}
}