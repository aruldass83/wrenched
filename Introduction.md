# Introduction #

none of the two-and-a-half major integration frameworks that exist to provide interoperability between Flex and Java, namely BlazeDS (and its commercial big brother LCDS) and GraniteDS, cover the full set of features developers need in their projects. examples are good enough, but when it comes to full scale RIAs, some of the key features are really lacking.

Wrenched is based around BlazeDS and addresses client-server data exchange issues that typically arise in projects based on Flex and Java. currently Wrenched provides several key features.

# Externalization #

no more need to manually implement `java.io.Externalizable` (or in fact `java.io.Serializable`) with all the concequences. enjoy using GAS3 (which not only is the most powerful generator currently, but is also included in flexmojos-maven-plugin) and forget manually reimplementing your POJOs in actionscript. have 3rd party POJOs that you can't control or alter, but really need to exchange? (de)serialization on the server side is properly done behind the scene.

Wrenched provides two ways of using externalization: when developers have control over their POJOs (and therefore they can "enhance" their classes) and when they don't (and they have to somehow work that fact around). either cases can be configured.

externalization in Wrenched includes full support for Maps and Enums (probably similar to GraniteDS, as it still uses `org.granite.util.Enum` and `org.granite.collections.IMap` for convenience) and provides an AS3 implementation of a HashMap.

Wrenched works (and ever will be) only via AMF protocol and that _is_ a feature. no xml, sorry ;P.

# Lazy loading #

that's exactly it. full lazy-loading support that (unlike [dpHibernate](http://code.google.com/p/dphibernate) for example) doesn't depend on any persistence provider (and actually works even _without_ a database if so configured) and still allows strong typing.

# Configuration ease #

all that is done with minimal configuration (which was one of the main points actually) and practically without dependencies. currently Wrenched requires only BlazeDS (being sort of an extension of it and hence using quite few of its classes, especially transport) some `javax` APIs (which just have to be there due to the nature of stuff going on) and certain core classes of Spring, leaving other frameworks to be of developers' choice.