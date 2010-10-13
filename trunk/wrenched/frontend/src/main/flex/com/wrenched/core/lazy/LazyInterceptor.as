package com.wrenched.core.lazy {
	import com.wrenched.core.remoting.DelegatingResponder;
	import com.wrenched.core.domain.LazyAttribute;
	
	import flash.events.IEventDispatcher;
	
	import mx.collections.ICollectionView;
	import mx.events.PropertyChangeEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.ObjectUtil;
	
	import org.floxy.IInterceptor;
	import org.floxy.IInvocation;
	
	/**
	 * invocation interceptor that will try to load
	 * lazy attributes when they are called using getters 
	 * @author konkere
	 */
	internal dynamic class LazyInterceptor implements IInterceptor {
		private static const LOAD:String = "__load";
		private static const LOADING:String = "__loading";

		private var _class:String;
		private var _id:Object;
		private var _started:Boolean = false;
		
		public function LazyInterceptor(arg0:String, arg1:Object, arg2:Array) {
			_class = arg0;
			_id = arg1;
			
			for each (var attributeName:String in arg2) {
				this.createLoader(attributeName);
			}
		}
		
		/**
		 * convenience method to prevent the interceptor from
		 * doing his job during proxy initialization
		 */
		function start():void {
	    	trace("START", _class, _id);
			_started = true;
		}
		
		function stop():void {
	    	trace("STOP", _class, _id);
			_started = false;
		}

		/**
		 * checks if an attribute must be lazy loaded (or in other terms
		 * has a loader function)
		 */
		private function isAttributeRegistered(attributeName:String):Boolean {
			return this.hasOwnProperty(getLoaderName(attributeName));
		}
		
		/**
		 * checks if at attribute is being loaded already (safety as
		 * flex tends to call attribute getters hundreds of times).
		 */
		private function isLoading(attributeName:String):Boolean {
			return this.hasOwnProperty(getLoadingFlagName(attributeName)) &&
				this[getLoadingFlagName(attributeName)];
		}
		
		private static function getLoaderName(attributeName:String):String {
			return LOAD + "_" + attributeName;
		}
		
		private static function getLoadingFlagName(attributeName:String):String {
			return LOADING + "_" + attributeName;
		}
		
		/**
		 * creates a dynamic loader function for an attribute
		 */
		private function createLoader(attributeName:String):void {
    	    this[getLoaderName(attributeName)] =
                function(target:Object):void {
                    LazyAttributeRegistry.instance().load(_class, _id, attributeName,
                        new DelegatingResponder(this,
                            function(event:ResultEvent):void {
								process(target, event.result as LazyAttribute);
							}));
				};
			this[getLoadingFlagName(attributeName)] = false;
		}
		
		/**
		 * deletes loader function
		 */
		private function deleteLoader(attributeName:String):void {
			delete this[getLoaderName(attributeName)];
			delete this[getLoadingFlagName(attributeName)];
		}
		
		/**
		 * processes lazy attribute that arrives from LAL
		 * and makes sure it is never fetched again for this
		 * proxy
		 */
        private function process(target:Object, la:LazyAttribute):void { 
            trace("PROCESS", _class, la.attributeName, la.attributeValue); 
            target[la.attributeName] = la.attributeValue;

            if (target is IEventDispatcher) { 
                IEventDispatcher(target).dispatchEvent(createLoadedEvent(target, la)); 
            }

            this.deleteLoader(la.attributeName); 
        } 
        
        private static function createLoadedEvent(source:Object, la:LazyAttribute):PropertyChangeEvent { 
            var event:PropertyChangeEvent = new PropertyChangeEvent(LazyAttributeRegistry.PROPERTY_LOAD); 
            event.kind = LazyAttributeRegistry.PROPERTY_LOAD; 
            event.source = source; 
            event.property = la.attributeName; 
            event.oldValue = null; 
            event.newValue = la.attributeValue; 
            return event; 
        }

		private static function isGetter(invocation:IInvocation):Boolean {
			return invocation.property &&
				(invocation.property.getMethod.fullName == invocation.method.fullName);
		}
		
		private static function isEmpty(value:Object):Boolean {
			return !value || (value is ICollectionView && ICollectionView(value).length == 0);
		}

        public function intercept(invocation:IInvocation):void {
//          trace("INTERCEPT", invocation.method.fullName);
            if (_started && isGetter(invocation)) {
                var attributeName:String = invocation.property.name;

                if (this.isAttributeRegistered(attributeName) && !isLoading(attributeName)) {
                    this[getLoadingFlagName(attributeName)] = true;
                                   
                    //if the attribute has data, perhaps it's better to keep it
                    if (isEmpty(invocation.invocationTarget[attributeName])) {
                        this[getLoaderName(attributeName)](invocation.invocationTarget);
                    }
                    else {
                        this.deleteLoader(attributeName);
                    }
                }
            }
            
            invocation.proceed();                  
        }
	}
}