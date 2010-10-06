package com.wrenched.core.lazy {
	import com.wrenched.core.domain.HashMap;
	import com.wrenched.core.util.ReflectionUtil;
	
	import flash.events.Event;
	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ICollectionView;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.RemoteObject;
	
	import org.flemit.reflection.Type;
	import org.floxy.IProxyRepository;
	import org.floxy.ProxyRepository;
	import org.granite.collections.IMap;

	/**
	* LAL registry that manages entity proxification and class and attribute registration
	* @author konkere
	*/
	public class LazyAttributeRegistry {
		private static const ID_NAME:String = "__idName";
		private static const ATTRIBUTES:String = "__lazyAttributes";

		private var lazyAttributeLoader:RemoteObject;
		private static var classes:Object = new Object();
		private static var proxyRepository:IProxyRepository = new ProxyRepository();
		private static var _instance:LazyAttributeRegistry;

		public static function instance(loader:RemoteObject=null):LazyAttributeRegistry {
			if (loader && !_instance) {
				_instance = new LazyAttributeRegistry(loader);
			}
			return _instance;
		}

		public static function get initialized():Boolean {
			return _instance;
		}

		function LazyAttributeRegistry(loader:RemoteObject) {
			lazyAttributeLoader = loader;
		}

		/**
		* class registry access 
		*/
		
		/**
		* registeres a class and an attribute to be subject for LAL by marking the class
		* to be available for later proxying.
		*/
		public static function registerClass(clazz:Class, entityIdName:String, attributeName:String):void {
			var className:String = ReflectionUtil.getCanonicalClassName(clazz);
			
			if (!ReflectionUtil.hasProperty(className, attributeName)) {
				throw new ArgumentError(className + " doesn't have attribute " + attributeName); 
			}

			proxyRepository.prepare([ReflectionUtil.getClass(clazz)]).addEventListener(Event.COMPLETE,
				function(evt:Event):void {
					registerClassByName(className, entityIdName, attributeName);
    			});
		}

		/**
		* registeres a class and an attribute in this registry
		*/
		static function registerClassByName(className:String, entityIdName:String, attributeName:String):void {
			if (!classes.hasOwnProperty(className)) {
				classes[className] = {};
				classes[className][ID_NAME] = entityIdName;
				classes[className][ATTRIBUTES] = [];
			}

			(classes[className][ATTRIBUTES] as Array).push(attributeName);
			trace("REGISTER", className, classes[className][ID_NAME], attributeName);
		}

		/**
		* traverses object class hierarchy in serach for a class that
		* is registered for LAL
		*/
		static function getRegisteredClassName(obj:Object):String {
			var className:String = null;
			var type:Type = Type.getType(obj);

			while (!(className && classes.hasOwnProperty(className)) && type) {
				className = type.fullName.replace(":",".");
				type = type.baseType;
			}

			return classes.hasOwnProperty(className) ? className : null;
		}

		/**
		* proxy management
		*/ 
		
		/**
		* LAL invocation
		*/
		function load(className:String, id:Object, attributeName:String, responder:IResponder):void {
			trace("LOAD", className, id, attributeName);
			var token:AsyncToken = lazyAttributeLoader.loadAttribute(className, id, attributeName);
			token.addResponder(responder);
		}

		/**
		* will recursively go through passed object and try to create proxies
		* for instances of registered classes
		*/
		public function createProxy(obj:Object, context:IMap=null, introspectionWatch:Dictionary=null):Object { 
			if (!context) { 
				context = new HashMap(); 
			} 
			if (!introspectionWatch) { 
				introspectionWatch = new Dictionary(); 
			} 

			if (!obj || introspectionWatch[obj]) { 
				return obj; 
			} 
			else if (obj is ICollectionView) { 
				var t:ArrayCollection = new ArrayCollection(); 
				for each (var o:Object in obj as ICollectionView) { 
					t.addItem(createProxy(o, context, introspectionWatch)); 
				} 
				return t; 
			} 
			else { 
				var local:String = getRegisteredClassName(obj); 
				var proxy:Object = obj; 
				var interceptor:LazyInterceptor = null; 

				if (local) { 
					var id:Object = obj[classes[local][ID_NAME]]; 
					if (context.containsKey(id)) { 
						//the proxy has already been created 
						//for this very object, reuse 
						return context.get(id); 
					} 

					try { 
						interceptor = new LazyInterceptor(local, id, classes[local][ATTRIBUTES]); 
						proxy = proxyRepository.create(ReflectionUtil.getClass(obj), [], interceptor);

						context.put(id, proxy); 
					} 
					catch(err:ArgumentError) { 
						trace(err.message); 
						//can't create proxy, skip                             
					} 
				} 

				introspectionWatch[proxy] = true;

				ReflectionUtil.copyProperties(obj, proxy, 
					function(propertyName:String, propertyValue:Object):Object { 
						return createProxy(propertyValue, context, introspectionWatch) 
					}); 

				//to avoid stack overflows 
				if (interceptor) { 
					interceptor.start(); 
				} 

				return proxy; 
			} 
		}
	}
}		