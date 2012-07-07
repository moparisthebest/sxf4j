package org.moparscape.xml.impl;

import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDomReader;
import com.thoughtworks.xstream.io.xml.xppdom.XppDom;
import com.thoughtworks.xstream.io.xml.xppdom.XppFactory;

import java.io.*;

public class XppXmlElement extends AbstractXmlElement {

	private final XppDom internal;

	public XppXmlElement() {
		internal = null;
	}

	public XppXmlElement(String name, XppDom parent) {
		internal = new XppDom(name);
		if(parent != null)
			parent.addChild(internal);
	}

	private XppXmlElement(XppDom internal) {
		this.internal = internal;
	}

	@Override
	public String getName() {
		return internal.getName();
	}

	@Override
	public String getValue() {
		return internal.getValue();
	}

	@Override
	public String getAttribute(String name) {
		return internal.getAttribute(name);
	}

	@Override
	public String[] getAttributeNames() {
		return internal.getAttributeNames();
	}

	@Override
	public XmlElement getParent() {
		return wrapParent(internal.getParent());
	}

	@Override
	public int getChildCount() {
		return internal.getChildCount();
	}

	@Override
	public XmlElement[] getChildren(String name){
		return wrapArray(name == null ? internal.getChildren() : internal.getChildren(name));
	}

	@Override
	public XmlElement getChild(String name) {
		return new XppXmlElement(internal.getChild(name));
	}

	@Override
	public XmlElement getNewChildXmlElement(String name){
		return new XppXmlElement(name, this.internal);
	}

	@Override
	public XmlElement addChild(XmlElement other) {
		if (other instanceof XppXmlElement)
			internal.addChild(((XppXmlElement) other).internal);
        return this;
	}

	@Override
	public XmlElement setAttribute(String name, String value) {
		internal.setAttribute(name, value);
        return this;
	}

	@Override
	public XmlElement setValue(String value) {
		internal.setValue(value);
        return this;
	}

	@Override
	public XmlElement readFromStream(InputStream is) throws Exception {
		return new XppXmlElement(XppFactory.buildDom(is, "UTF-8")); // guess at encoding...
	}

	@Override
	public void writeToStream(OutputStream os) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		writeHeader(osw, true);

		new HierarchicalStreamCopier().copy(new XppDomReader(internal), new PrettyPrintWriter(osw));
		osw.close();
		os.close();
	}

	@Override
	protected Object getInternal() {
		return this.internal;
	}

	@Override
	protected XmlElement wrapObject(Object internal) {
		if(internal instanceof XppDom)
			return new XppXmlElement((XppDom)internal);
		return null;
	}
}
