package com.wrenched.core.domain {
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	import flash.utils.Dictionary;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.utils.Proxy;
	import flash.utils.flash_proxy;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.events.PropertyChangeEvent;
	import mx.utils.ObjectUtil;
	
	import org.granite.collections.IMap;
	import org.granite.util.Enum;
	
	use namespace flash_proxy;
	
	/**
	 * convenient hashmap implementation for actionscript. notice that for better performance
	 * stored keys and values should implement "hashCode():int" and "equals(*):Boolean".
	 * @author konkere
	 */  
	[Bindable]
	[RemoteClass(alias="com.wrenched.core.domain.ExternalizableMap")]
	public dynamic class HashMap extends Proxy implements IMap, IExternalizable, IEventDispatcher {
		private var dispatcher:EventDispatcher = new EventDispatcher();
		
		private var _keys:ArrayList = new ArrayList();
		private var _values:ArrayList = new ArrayList();
		private var _entrySet:Dictionary = new Dictionary();
		
		public function HashMap(pairs:Array = null) {
			if (pairs != null) {
				for each (var pair:Array in pairs) {
					this.innerPut(pair);
				}
			}
		}
		
		override flash_proxy function callProperty(name:*, ... rest):* {
			name = unpack(name);
			return Function(get(name)).apply(_entrySet, rest);
		}
		
		override flash_proxy function deleteProperty(name:*):Boolean {
			name = unpack(name);
			return remove(name) !== null;
		}
		
		override flash_proxy function getProperty(name:*):* {
			name = unpack(name);
			return get(name);
		}
		
		override flash_proxy function hasProperty(name:*):Boolean {
			name = unpack(name);
			return containsKey(name);
		}
		
		override flash_proxy function setProperty(name:*, value:*):void {
			name = unpack(name);
			put(name, value);
		}
		
		override flash_proxy function nextName(index:int):String {
			return _keys.getItemAt(index - 1).toString();
		}
		
		override flash_proxy function nextNameIndex(index:int):int {
			return (index < _keys.length ? (index + 1) : 0);
		}
		
		override flash_proxy function nextValue(index:int):* {
			return _entrySet[hash(_keys.getItemAt(index - 1))];
		}
		
		[Bindable(event="collectionChange")]		
		public function get keySet():ArrayCollection {
			return new ArrayCollection(_keys.toArray());
		}
		
		[Bindable(event="collectionChange")]		
		public function get values():ArrayCollection {
			return new ArrayCollection(_values.toArray());
		}
		
		[Bindable(event="collectionChange")]		
		public function get entries():ArrayCollection {
			var entries:ArrayCollection = new ArrayCollection();
			for each (var key:Object in this.keySet) {
				entries.addItem([key, this.get(key)]);
			}
			
			return entries;
		}		
		
		[Bindable(event="collectionChange")]		
		public function get(key:*):* {
			var entry:Array = this.innerGet(key);
			return (entry ? entry[1] : null);
		}
		
		public function containsKey(o:*):Boolean {
			return this.innerContains(o, this._keys.source);
		}
		
		public function containsValue(o:*):Boolean {
			return this.innerContains(o, this._values.source);
		}
		
		public function remove(key:*):Object {
			var oldValue:Object = null;
			if (containsKey(key)) {
				var entry:Array = this.innerGet(key);
				oldValue = entry[1];
				this.innerRemove(entry);
				
				this.commit(CollectionEventKind.REMOVE,	[key, oldValue]);
			}
			return oldValue;
		}
		
		public function put(key:*, value:*):* {
			var oldValue:Object = null;
			if (!containsKey(key)) {
				this.innerPut([key, value]);
				
				this.commit(CollectionEventKind.ADD, [key, value]);
			}
			else {
				var entry:Array = this.innerGet(key);
				oldValue = entry[1];
				_values.removeItem(oldValue);
				_values.addItem(value);
				entry[1] = value;
				
				this.commit(CollectionEventKind.REPLACE,
					[PropertyChangeEvent.createUpdateEvent(this, key, oldValue, value)]);
			}
			return oldValue;
		}
		
		public function clear():void {
			_keys.removeAll();
			_values.removeAll();
			_entrySet = new Dictionary();
			
			this.commit(CollectionEventKind.RESET, null);
		}
		
		[Bindable(event="collectionChange")]		
		public function get length():int {
			return _keys.length;
		}
			
		public function readExternal(input:IDataInput):void {
			for each (var pair:Array in input.readObject() as Array) {
				this.innerPut(pair);
			}
		}
		
		public function writeExternal(output:IDataOutput):void {
			var elements:Array = new Array(_keys.length);
			var i:int = 0;
			for each (var value:Array in _entrySet) {
				elements[i++] = value;
			}
			output.writeObject(elements);
		}
		
		protected function innerGet(key:*):Array {
			return this._entrySet[hash(key)] as Array;
		}
		
		protected function innerPut(entry:Array):void {
			this._keys.addItem(enumerate(entry[0]));
			this._values.addItem(enumerate(entry[1]));
			this._entrySet[hash(entry[0])] = entry;
		}
		
		protected function innerRemove(entry:Array):void {
			this._values.removeItem(entry[1]);
			this._keys.removeItem(entry[0]);
			delete this._entrySet[hash(entry[0])];
		}

		protected function innerContains(item:Object, source:Array):Boolean {
			for each (var i:Object in source) {
				if (innerCompare(item, i)) {
					return true;
				}
			}
			
			return false;
		} 
		
		private function innerCompare(item0:Object, item1:Object):Boolean {
			if (!item0 || !item1 || (getQualifiedClassName(item0) != getQualifiedClassName(item1))) {
				return false;
			}
			else if (item0.hasOwnProperty("equals")) {
				return item0.equals(item1);
			}
			else if (item0.hasOwnProperty("hashCode") && item1.hasOwnProperty("hashCode")) {
				return (Number(item0.hashCode()) == Number(item1.hashCode()));
			}
			else {
				return ObjectUtil.compare(item0, item1) == 0;
			}
		}
		
		private function hash(key:Object):* {
			if (key.hasOwnProperty("hashCode")) {
				return key.hashCode();
			}
			else if (key is Enum) {
				return Enum(key).name;
			}
			else {
				return key;
			}
		}
		
		private function enumerate(item:Object):Object {
			return item is Enum ? Enum.normalize(item as Enum) : item;
		}
		
		private function unpack(name:*):* {
			return (name is QName ? (name as QName).localName : name);
		}
		
		private function commit(kind:String, items:Array):void {
			this.dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE,
				false, false, kind, -1, -1, items));
			this.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "keySet", null, this._keys));
			this.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "values", null, this._values));
			this.dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "length", null, this._keys.length));
		}
		
		public function dispatchEvent(event:Event):Boolean {
			return dispatcher.dispatchEvent(event);
		}
		
		public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
			dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
		}
		
		public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
			dispatcher.removeEventListener(type, listener, useCapture);
		}
		
		public function hasEventListener(type:String):Boolean {
			return dispatcher.hasEventListener(type);
		}
		
		public function willTrigger(type:String):Boolean {
			return dispatcher.willTrigger(type);
		}
		
		public function toString():String {
			var s:String = "";
			
			for each (var key:Object in this.keySet) {
				s += key.toString() + "=" + this.get(key).toString() + ",";
			}
			return "{" + s.substr(0, s.length - 1) + "}";
		}
	}
}