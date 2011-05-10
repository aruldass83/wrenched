package com.wrenched.core.lazy {
	import com.wrenched.core.domain.HashMap;
	import com.wrenched.core.domain.LazyAttributeRegistryDescriptor;
	import com.wrenched.core.remoting.DelegatingResponder;
	import com.wrenched.core.util.ReflectionUtil;

	import flash.events.Event;
	import flash.net.getClassByAlias;
	import flash.system.ApplicationDomain;
	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;

	import mx.collections.ArrayCollection;
	import mx.collections.ListCollectionView;
	import mx.collections.ICollectionView;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	import org.floxy.IProxyRepository;
	import org.floxy.ProxyRepository;
	import org.granite.collections.IMap;
	import org.granite.reflect.AmbiguousClassNameError;
	import org.granite.reflect.ClassNotFoundError;
	import org.granite.reflect.Type;
	import org.granite.util.Enum;

	/**
	* LAL registry that manages entity proxification and class and attribute registration
	* @author konkere
	*/
	public class LazyAttributeRegistry {
		public static const PROPERTY_LOAD:String = "propertyLoad";

		private static const ID_NAME:String = "__idName";
		private static const ATTRIBUTES:String = "__lazyAttributes";

		private var lazyAttributeLoader:RemoteObject;
		private static var classes:Object = new Object();
		private static var proxyRepository:IProxyRepository = new ProxyRepository();
		private static const proxyDomain = new ApplicationDomain(ApplicationDomain.currentDomain); 
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

			var token:AsyncToken = lazyAttributeLoader.getManagedClasses(); 
			token.addResponder(new DelegatingResponder(this, 
				function(event:ResultEvent):void { 
					for each (var line:LazyAttributeRegistryDescriptor in event.result as ICollectionView) {
						var local:Class = flash.net.getClassByAlias(line.className);
	
						for each (var attributeName:String in line.attributes) { 
							registerClass(local, line.idName, attributeName, false);
						}	 
					} 
				})); 
		}

		/**
		* class registry access 
		*/
		
		/**
		* registeres a class and an attribute to be subject for LAL by marking the class
		* to be available for later proxying.
		*/
		public static function registerClass(clazz:Class, entityIdName:Object, attributeName:String, force:Boolean=true):void {
			if (!Type.isDomainRegistered(proxyDomain)) {
				Type.registerDomain(proxyDomain);                      
			} 

			var className:String = getQualifiedClassName(clazz);

			if (!ReflectionUtil.hasProperty(clazz, attributeName)) {
				throw new ArgumentError(className + " doesn't have attribute " + attributeName); 
			}

			proxyRepository.prepare([ReflectionUtil.getClass(clazz)], proxyDomain).addEventListener(Event.COMPLETE,
				function(evt:Event):void {
					registerClassByName(className, entityIdName, attributeName);
				});
		}

		/**
		* registeres a class and an attribute in this registry
		*/
		static function registerClassByName(className:String, entityIdName:Object, attributeName:String, force:Boolean=true):void {
			if (!classes.hasOwnProperty(className)) {
				classes[className] = {};
				classes[className][ID_NAME] = entityIdName;
				classes[className][ATTRIBUTES] = [];
			}
			else if (force) {
				classes[className][ID_NAME] = entityIdName;
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
			var type:Type = Type.forInstance(obj, ApplicationDomain.currentDomain);

			while (!(className && classes.hasOwnProperty(className)) && type) {
				className = type.name;
				type = type.superclass;
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
			var token:AsyncToken =
				lazyAttributeLoader.loadAttribute(Type.forName(className, proxyDomain).alias, id, attributeName);
			token.addResponder(responder);
		}

		/**
		* will recursively go through passed object and try to create proxies
		* for instances of registered classes
		*/
		public static function createProxy(obj:Object, context:Dictionary=null):Object {
			if (!context) {
				context = new Dictionary();
			}

			if (!obj || obj is Enum || obj is String || obj is Number || obj is int) {
			}
			else if (context[obj]) {
				return context[obj];
			}
			else if (obj is ListCollectionView) {
				var list:ListCollectionView = ListCollectionView(obj);

				for (var i:int = 0; i < list.length; i++) {
					list.setItemAt(createProxy(list.getItemAt(i), context), i);
				}
			}
			else if (obj is IMap) {
				var map:IMap = IMap(obj);
				var keys:ArrayCollection = map.keySet;

				for (var i:int = 0; i < keys.length; i++) {
					map.put(keys.getItemAt(i), createProxy(map.get(keys.getItemAt(i)), context));
				}
			}
			else {
				var local:String = getRegisteredClassName(obj);

				context[obj] = obj;

				if (local) {
					var id:Object = resolveId(local, obj);

					try {
						var interceptor:LazyInterceptor = new LazyInterceptor(local, id, classes[local][ATTRIBUTES]);
						var proxy:Object = proxyRepository.create(ReflectionUtil.getClass(obj), [], interceptor);

						context[proxy] = proxy;
						context[obj] = proxy;

						ReflectionUtil.copyProperties(obj, proxy,
							function(propertyName:String, propertyValue:Object):Object {
								return createProxy(propertyValue, context);
							});

						interceptor.start();
					}
					catch(err:ArgumentError) {
						trace(err.message);
						//can't create proxy, skip
					}
				}
				else {
					ReflectionUtil.copyProperties(obj, obj,
						function(propertyName:String, propertyValue:Object):Object {
							return createProxy(propertyValue, context);
						});
				}

				return context[obj];
			}

			return obj;
		} 

		private static function resolveId(className:String, obj:Object):Object {
			if (classes[className][ID_NAME] is String) {			
				if (classes[className][ID_NAME] == "self") {
					return obj;
				}
				else {
					return obj[classes[className][ID_NAME]];				
				}
			}	
			else if (classes[className][ID_NAME] is LazyAttributeRegistryDescriptor) {
				var idDescriptor:LazyAttributeRegistryDescriptor =
					classes[className][ID_NAME] as LazyAttributeRegistryDescriptor;
				//composite id
				var idClass:Class = flash.net.getClassByAlias(idDescriptor.className) as Class;
				var id:Object = new idClass();

				for each (var idName:String in idDescriptor.attributes) {
					id[idName] = obj[idName];
				}

				return id;
			}
			else {
				//don't accept anything else
				return null;
			}
		}
	}
}		