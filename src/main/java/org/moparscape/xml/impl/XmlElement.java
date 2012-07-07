package org.moparscape.xml.impl;

import java.io.File;
import java.io.OutputStream;

public interface XmlElement {
	public String getName();

	public String getValue();

	public String getAttribute(String name);

	public String[] getAttributeNames();

	public XmlElement getParent();

	public int getChildCount();

	public XmlElement[] getChildren();

	public XmlElement[] getChildren(String name);

	public XmlElement getChild(String name);

	public XmlElement setAttribute(String name, String value);

	public XmlElement setValue(String value);

	public XmlElement getNewChildXmlElement(String name);

	public XmlElement addChild(XmlElement other);

	public void writeToStream(OutputStream os) throws Exception;

	public void writeToFile(File file) throws Exception;

	public void writeToFile(String fileName) throws Exception;

	public String toString();

	public String toStringCompact();

	public int byteLength();

	public int byteLengthCompact();

	public boolean isWrapperFor(Class<?> iface);
	public <T> T unwrap(Class<T> iface);

	public XmlElement copyTo(XmlElementFactory factory) throws Exception;
}
