## dependency management ##

Wrenched is naturally managed by Maven and comes in several projects.

`wrenched-core` and `wrenched-core-swc` are core modules that provide all main features. depend you java back-end on the former and your flex frontend on the latter.

unfortunately transitive SWCs are not resolved correctly, so there're three additional dependencies that must be put in your flex project: `granite-swc`, `floxy` and `flemit`.

apart from these come _adapters_: `hibernate-adapter`, `simple-adapter`, etc. these are certain exclusive SPIs that your java back-end should also be depending on. choose one according to a technology or framework used (Hibernate, JPA, maybe pure CGLIB). adapters are discovered at runtime and therefore don't require any configuration whatsoever. however make sure either there's one on the classpath or you don't need any.

## frameworks ##

as mentioned in [Introduction](Introduction.md) Wrenched is based on **Spring Framework** _2.5.6_ (not mandatory, though strongly recommended) and is built towards **Flex SDK** _3.2.0_ and **Flash Player** _9_. thus backward compatibility is not an issue.

## building and running examples ##

there's a comprehensive example on configuring and using Wrenched at `trunk/example` on SVN. it's a twin swf/war application with a sub-project containing some test domain classes.

example covers externalization and both types of lazy-loading. in-memory database (h2) is used to demonstrate persistence, an xml-based tree is used to demonstrate pure java lazy-loading.

example project is also built using Maven. all its aspects are configured within the application itself and even though it is by default configured to be deployed on Oracle Weblogic 10g, nothing prevents it from being deployed to any container of choice.