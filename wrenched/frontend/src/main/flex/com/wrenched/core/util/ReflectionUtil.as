package com.wrenched.core.util {
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	import flash.utils.describeType;
	
	import org.flemit.reflection.PropertyInfo;
	import org.flemit.reflection.Type;
	
	/**
	 *
	 * @author konkere
	 */
	public class ReflectionUtil {

		/**
		 * copies properties from source to destination using class information.
		 */
	    public static function copyProperties(source:Object, destination:Object, propertyCallback:Function=null):void {
			var sourceType:Type = Type.getType(source);
			var destinationType:Type = Type.getType(destination);
			
	    	if (!sourceType.isAssignableFromInstance(destination)) {
	    		throw new ArgumentError("incompatible objects: " + getQualifiedClassName(source) + ", " + getQualifiedClassName(destination));
	    	}
	    	
			while(sourceType != Type.getType(Object)) {
				for each(var property:PropertyInfo in sourceType.getProperties(false)) {
					var p:PropertyInfo = destinationType.getProperty(property.name, true);
					if (p && p.canWrite && property.canRead) { 
						if (propertyCallback) {
		    				destination[property.name] = propertyCallback(property.name, source[property.name]);
		    			}
		    			else {
		    				destination[property.name] = source[property.name];
		    			}
		   			}
				}
				
				sourceType = sourceType.baseType;
			}
		}
	    
		public static function hasProperty(clazz:Class, attributeName:String):Boolean {
			var type:Type = Type.getType(clazz);
			
			while ((type != Type.getType(Object))) {
				var f:Boolean = type.getProperties(false).some(
					function(item:PropertyInfo, index:int, array:Array):Boolean {
						return item.name == attributeName;
					}, null);
				
				if (f) {
					return true;
				}
				type = type.baseType;
			}
			
			return false;
		}

	
		/**
		 * returns class for an object
		 */
		public static function getClass(obj:Object):Class {
			return getDefinitionByName(getQualifiedClassName(obj)) as Class;
		}

		public static function getClassAlias(o:Object):String {
			return describeType(o).@alias;
		}
	}
}