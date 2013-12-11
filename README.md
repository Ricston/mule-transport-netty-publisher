WELCOME
=======
This is a Mule transport implementation using Netty (http://netty.io) that supports Publish/Subscribe on raw TCP (from a high level perspective, its like JMS topics, but with limited functionality, for example, there is no transaction support, and there it no external broker). 

Developed by Ricston Ltd (www.ricston.com).

To compile and package from source, just execute: mvn package. This will also generate the Mule Studio plugin.

All tests are use-cases which you can copy and paste from.

Minimum Requirements
====================

Mule 3.3.x or higher
Maven 3.0.3 or higher

Documentation
=============
Please read the following blog post: http://ricston.com/blog/mule-netty-publisher/. It explains how to use the module. You can also look at the functional tests cases.

License
========

Apache License 2.0 (Please see LICENSE.md)