it would have been perfect. however, nobody is perfect and even a product as wrenched has some limitations:

  * as mentioned already, Wrenched only works with AMF.

  * while it is supported to have attributes of type Map, it is not currently possible to exchange Maps per se. that is due to the fact that AMF messages are in fact maps as well and while deserializing AMF is peanuts, writing two maps in two different ways to the same AMF stream is yet unclear how to.

  * due to the way instrumentation is implemented, it is not currently possible to use `ObjectUtil.copy` on proxies. i'm looking forward to implementing my own proxying engine that overcomes this issue for next release.