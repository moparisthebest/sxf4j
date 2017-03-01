package com.moparisthebest.sxf4j.impl;

import java.io.File;
import java.io.InputStream;

public interface XmlElementFactory {

	public XmlElement getNewChildXmlElement(String name);

	public XmlElement readFromStream(InputStream is) throws Exception;

	public XmlElement readFromFile(File file) throws Exception;

	public XmlElement readFromFile(String file) throws Exception;

	public XmlElement readFromString(String string) throws Exception;

}
