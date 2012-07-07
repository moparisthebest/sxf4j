SXF4J

This library provides a common interface for multiple different XML implementations, so they can be plugged in at runtime,
or programmatically selected by the programmer, allowing them to access the backing implementation for extra features,
and seamlessly convert between different implementations via a copy method.

Currently, and as a side project that may be separated from this in the near future, the class ClassXmlElement may be
extended by any class, which can be annotated with @Attribute or @Child annotations so it can be seamlessly dumped to an
XML file by calling the toXml() method of ClassXmlElement, this makes it easy to represent an XML file in java code without
any logic per class to dump it to XML.  Functionality is planned to load the class from the XML file as well.  After writing
this class, I discovered that a program called XStream offers similar (if more complex) functionality, but I still believe
this is simpler and easier to work with, and they still have different purposes. (XStream offers serialization, this class
simply aims to simplify the XML to Java Class and back process.)

Hopefully someone besides me will find this useful, I would be more than happy to accept feature requests, patches, and
pull requests.

SXF4J is currently licensed under the GNU/LGPLv3.  If you need this under another license, let me know and I'll see what I can do.

Enjoy!