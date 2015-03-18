configuring lazy loading in Wrenched is a bit more complex and will require some coding, but not much.

## transport ##

as usual, a channel has to be configured in BlazeDS' `services-config.xml`, but in this case - on Flex side:
```
<channel-definition id="my-custom-amf"
	class="com.wrenched.core.messaging.channels.LazyAMFChannel">
	<endpoint url="http://{server.name}:{server.port}/{context.root}/messagebroker/amf"
		class="flex.messaging.endpoints.AMFEndpoint" />
</channel-definition>
```

unfortunately Flash hides all the serialization business, so, unlike Java side, it is impossible to customize it on levels of i/o streams (which would've been "symmetrical" and therefore - most appropriate). Wrenched only provides a custom AMF channel, that instruments already deserialized data as it passes through.

in order to handle proxies properly a custom `JavaAdapter` must be configured as well:

```
<adapter-definition id="java-object"
	class="com.wrenched.core.messaging.services.remoting.adapters.InstrumentingJavaAdapter" default="true"/>
```

`InstrumentingJavaAdapter` instruments values and handles proxies as provided by corresponding adapter (see [Setup](Setup.md)).

## providers ##

thanks to provider pattern implementation, lazy loading in Wrenched is oblivious to the way entities are managed. you can even serialize entities to files and still lazy loading mechanism will work as long as you tell Wrenched how to use you own way.

first, lazy attribute loader (or LAL :D) has to be configured and fed with providers.  Lazy attribute providers are wrappers over user DAO classes. each provider manages a certain "domain" which is basically a package (or fully qualified class name). all the classes that are resolved to a domain will be managed by a single provider. this is convenient to have multiple providers for the same LAL. domain can be configured by setting the corresponding property on a provider.

currently two predefined providers exist:

  * persistence-based provider, which is built around the fact that ORM frameworks have a dedicated "loader" method that takes two parameters: entity class and entity id. in Hibernate it is `Session.load(Class, Object)`, in JPA it is `EntityManager.find(Class, Object)`. will scan provided domain for classes with `@javax.persistence.Entity` that have attributes with `fetch=FetchType.LAZY` and register them later. this type of provider usually doesn't require any additional code, only configuration.

LAL configuration for JPA could look like this for example:

```
<bean id="lazyAttributeLoaderService" class="com.wrenched.core.services.LazyAttributeLoader">
	<property name="providers">
		<list>
			<bean class="com.wrenched.core.services.support.PersistenceBasedAttributeProvider" init-method="init">
				<property name="delegate">
					<bean factory-bean="entityManagerFactory" factory-method="createEntityManager"/>
				</property>
				<property name="domain" value="foo.bar.domain"/>
				<property name="loaderMethodName" value="find"/>
			</bean>
		</list>
	</property>
</bean>
```

  * method-based provider, that will simply call a certain method on your DAO, assuming that this method accepts a single parameter - an id. the provider must be configured by associating a pair of entity and attribute names and method name. this information will be also used to register classes for lazy loading: through `methods` setter or by introspecting the loader object for annotations.

```
<bean id="lazyAttributeLoaderService" class="com.wrenched.core.services.LazyAttributeLoader">
	<property name="providers">
		<list>
			<bean class="com.wrenched.core.services.support.MethodBasedAttributeProvider" init-method="init">
				<property name="delegate">
					<bean class="foo.bar.dao.EntityLoader"/>
				</property>
				<property name="methods">
					<map>
						<entry key="TestEntity#id#children" value="getTestChildren"/>
					</map>
				</property>
				<property name="domain" value="foo.bar.other.domain"/>
			</bean>
		</list>
	</property>
</bean>
```

notice mentioning of `id` in method declaration above. it is identity attribute name of specified class and it must be also provided (as there's no general way to uniquely address instances outside ORM apart from object references, which don't match across different virtual machines). if for some reason this is not applicable, it can be omitted and will result in the whole instance being passed as fetcher method argument (this is not recommended resulting in substantial network traffic).

```
public class EntityLoader {
//...
	//or set methods like that
	public TestEntity getTestParent(Integer id) {
		//...
	}

	public List<TestEntity2> getTestChildren(Integer id) {
		//...
	}
//...
}
```
```
public class TestEntity {
//...
	private Integer id;
	private List<TestEntity2> children;
//...
}

public class TestEntity2 {
//...
	private Integer id;
	private TestEntity parent;
//...
}
```

## annotation support ##

to be in line with the annotation trend :P Wrenched provides full annotation support for configuring lazy-loading (hence hiding all internal business of setting up all the properties and populating internal dependencies). have a look at the example below:

```
<beans>
	<bean id="loader1" class="EntityLoader"/>
	<bean id="loader2" class="EntityLoader2"/>

	<bean id="lazyAttributeLoader" class="com.wrenched.core.services.LazyAttributeLoader"/>

	<!-- that is mandatory, similar to spring-jpa's PersistenceAnnotationBeanPostProcessor -->
	<bean class="com.wrenched.core.annotations.LazyAttributeProviderAnnotationPostProcessor"/>
</beans>
```
```
@LazyAttributeProvider(LazyAttributeProviderType.METHOD)
@LazyAttributeDomain("foo.bar.other.domain")
public class EntityLoader {
//...
	@LazyAttributeFetcher(targetClass=TestEntity2.class, idName="id", attributeName="parent")
	public Object getTestParent(Integer id) {
		//...
	}

	@LazyAttributeFetcher(targetClass=TestEntity.class, idName="id", attributeName="children")
	public List<TestEntity2> getTestChildren(Integer id) {
		//...
	}
//...
}

@LazyAttributeProvider(LazyAttributeProviderType.PERSISTENCE)
@LazyAttributeDomain("foo.bar.domain")
public class EntityLoader2 extends HibernateDaoSupport {
//...
	@LazyAttributeFetcher
	public Object loadMyStuff(Class<?> clazz, Object id) {
		//why not like this?
		return getHibernateTemplate().load(clazz, (Serializable)id);
	}
//...
```

here two DAO classes are annotated with `@com.wrenched.core.annotations.LazyAttributeProvider` specifying type of provider: method- or persistence-based. method configuration is achieved by annotating corresponding ones with `@com.wrenched.core.annotations.LazyAttributeFetcher` specifying class and attribute name this method fetches.

notice that in case of persistence-based provider it is assumed that only one suitable method exists and annotation attributes are ignored. the domain is configured with `@com.wrenched.core.annotations.LazyAttributeDomain`, specifying the domain as value.

keep in mind that instantiating `com.wrenched.core.annotations.LazyAttributeProviderAnnotationPostProcessor` in spring bean context is mandatory when using `@com.wrenched.core.annotations.LazyAttributeProvider`, otherwise there's no limitation on using annotation support whatsoever. both types of configuration (explicit and annotation) can be even mixed together. setting properties explicitly through corresponding setters on providers will however have precedence over any annotation-based configuration.

## usage ##

now that the server is configured to provide lazy loading, the client has to make use of it. all the LAL logic on the Flex side is concentrated in `com.wrenched.core.lazy.LazyAttributeRegistry` class that needs to be instantiated with a remote object pointing to LAL service.

```
<mx:Application creationComplete="onCreated();">
	<mx:Script>
		<![CDATA[
			import foo.bar.TestEntity;
			import com.wrenched.core.lazy.LazyAttributeRegistry;
			
			function onCreated():void {
				LazyAttributeRegistry.instance(this.lazyAttributeLoader);
				//optional since RC2
				LazyAttributeRegistry.registerClass(TestEntity, "id", "children");
			}
		]]>
	</mx:Script>

	<mx:RemoteObject id="lazyAttributeLoader" destination="lazyAttributeLoaderService" showBusyCursor="true">
		<mx:operation name="loadAttribute"/>
	</mx:RemoteObject>
</mx:Application>
```

for the implementation to be as generic as possible, each class and lazy attribute have to be registered specifying so-called "id-name" of the class additionally. in case of persistence-based providers that has to be the name of `@Id` attribute, but for method-based providers on the other hand this can be any convenient attribute name (or `self` to use the instance itself), as long as it suffices the underlying DAO method.

as of RC2 it is not mandatory to register classes on the client, as it will happen automatically according to configuration of lazy attribute providers on Java side. you can still however do it, if for some reason automatic configuration is not enough for you. keep in mind that explicit configuration will always take precedence over the automatic one. also notice that automatic configuration relies on the fact that ActionScript classes you are going to use in client-server exchange are aliased to their Java counterparts with `[RemoteClass]`.

after being registered, a lazy attribute will be fetched from the server when it's first asked for, otherwise it looks like a normal property, that can take part in bindings, etc. when it obtains value, in addition to normal Flex'  property change event, the proxy will fire a `mx.events.PropertyChangeEvent` of type `propertyLoad` and kind `propertyLoad`. this is pure convenience, normally it is enough to listen to default property events.