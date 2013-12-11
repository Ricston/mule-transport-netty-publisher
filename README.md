WELCOME
=======
This is a Mule transport implementation using Netty (http://netty.io) that supports Publish/Subscribe on raw TCP (from a high level perspective, its like JMS topics, but with limited functionality, for example, there is no transaction support, and there it no external broker). 

Developed by Ricston Ltd (www.ricston.com).

To compile and package from source, just execute: 

```Shell
mvn package 
```

Make sure you have the minimum requirements mentioned here below. The above command will also generate the Mule Studio plugin.

All tests are use-cases which you can copy and paste from.

Minimum Requirements
====================

Mule 3.3.x or higher,
Maven 3.0.3 or higher

Documentation
=============
Please read the following blog post: http://ricston.com/blog/mule-netty-publisher/. It explains how to use the module. You can also look at the functional tests cases.

License
========

Apache License 2.0 (Please see LICENSE.md)

Connector Configuration Example
===============================

```XML
<nettypublisher:config name="nettyPublisher" nettyPublisherName="nettyPublisher">
	<!-- configure publishers here -->
	<nettypublisher:publishers>
		<!-- we have 2 different publishers, listening on ports 8091 and 8092 -->
		<nettypublisher:publisher key="publisher1">8091</nettypublisher:publisher>
		<nettypublisher:publisher key="publisher2">8092</nettypublisher:publisher>
	</nettypublisher:publishers>
</nettypublisher:config>
```

Publishing information
======================

```XML
<!-- simple flow that receives messages on a VM endpoint and publishes to all 
clients connected to publisher1 (port 8091 from the connector configuration) -->
<flow name="nettyPublisherFlow">
    <vm:inbound-endpoint path="toPublisher" />
    <logger message="#[payload]" level="INFO" />
    <nettypublisher:publish publisher="publisher1" data="#[payload]"/>
</flow>
```

Starting a Netty Server
=======================

```XML
<!-- this will start a Netty server on port 8090, each message received will trigger
 this flow. You can configure any Mule processor as in a normal Mule flow -->
<flow name="nettyServerFlow">
    <nettypublisher:server port="8090"/>
    <logger message="#[payload]" level="INFO" />
    
    <!-- your message procesors here -->
</flow>
```