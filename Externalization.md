here and after i will be using Spring to demonstrate configuration snippets.

so, how to configure externalization.

## transport ##

first a custom channel has to be added in BlazeDS' `services-config.xml`:

```
<channel-definition id="my-custom-amf" class="flex.messaging.channels.AMFChannel">
	<endpoint url="http://{server.name}:{server.port}/{context.root}/messagebroker/amf"
		class="com.wrenched.core.messaging.endpoints.J5AMFEndpoint" />
</channel-definition>
```

Wrenched provides a custom AMF endpoint, that is enriched with "knowledge" on how to process enums, maps and decorators (that are mentioned below).

this channel may be added to default channels, or you can use it explicitly in your Flex application.

## POJOs ##

well, the only thing that needs to be done here is subclassing `com.wrenched.core.domain.AbstractExternalizableEntity`, which is `java.io.Externalizable` and delegates all the externalization work to `com.wrenched.core.Externalizer` - custom (de)serializer that manages externalizable objects.

## decorators ##

quite often a situation arise when one must exchange objects of classes that come from somewhere else. a jar file from the development team across the corridor, etc. there's little you can do with these classes in this case. and here comes Wrenched again, with externalizable decorator support.

the only thing you need to do here is to register corresponding classes:

```
<bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
	<property name="staticMethod" value="com.wrenched.core.externalization.Externalizer.registerDecoratorsFor" />
	<property name="arguments">
		<list>
			<value>foo.bar.TestEntity</value>
			<value>bar.foo.TestEntity2</value>
		</list>
	</property>
</bean>
```

so that when Wrenched does i/o it knows that objects of these classes are not externalizable and decorates them.

## auto-decoration ##

in addition to manually registering classes to be decorated, one can annotate his entity classes with `com.wrenched.core.annotations.Externalizable`. when instances of these classes are being exchanged, Wrenched checks class metadata and automatically creates runtime decorators if needed.

## starting ##

final thing that you have to do (yes, for the very purpose of not wanting to do that) is to instantiate Externalizer:

```
<bean class="com.wrenched.core.externalization.Externalizer" factory-method="getInstance"/>
```

`Externalizer` is configured by specifying an instance of `com.wrenched.core.externalization.Externalizer$Configuration` or a subclass of `com.wrenched.core.externalization.AbstractConfiguration` (this way will be familiar to jBPM users).

you may still not want externalization support, which is turned off by simply not instantiating Externalizer.