package com.moparisthebest.sxf4j.impl;

import org.dom4j.*;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class Dom4jXmlElement extends AbstractXmlElement {

	private final Element internal;

	public Dom4jXmlElement() {
		internal = null;
	}

	public Dom4jXmlElement(String name, Element parent) {
		if (parent == null) {
			Document document = DocumentHelper.createDocument();
			internal = document.addElement(name);
		} else
			internal = parent.addElement(name);
	}

	private Dom4jXmlElement(Element internal) {
		this.internal = internal;
	}

	@Override
	public String getName() {
		return internal.getName();
	}

	@Override
	public String getValue() {
		return internal.getStringValue();
	}

	@Override
	public String getAttribute(String name) {
		Attribute attr = internal.attribute(name);
		return attr == null ? "" : attr.getValue();
	}

	@Override
	public String[] getAttributeNames() {
		String[] ret = new String[internal.attributeCount()];
		for(int x = 0; x < ret.length; ++x)
			ret[x] = internal.attribute(x).getQualifiedName();
		return ret;
	}

	@Override
	public XmlElement getParent() {
		return wrapParent(internal.getParent());
	}

	@Override
	public int getChildCount() {
		return internal.elements().size();
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public XmlElement[] getChildren(String name){
		List elements = name == null ? internal.elements() : internal.elements(name);
		return wrapArray(elements.toArray(new Object[elements.size()]));
	}

	@Override
	public XmlElement getChild(String name) {
		return new Dom4jXmlElement(internal.element(name));
	}

	@Override
	public XmlElement setAttribute(String name, String value) {
		if (name.startsWith("xmlns"))
			try {
				String prefix = name.split(":")[1];
				internal.add(new Namespace(prefix, value));
			} catch (Exception e) {
				if(name.equals("xmlns"))
					internal.setQName(QName.get(internal.getName(), value));
			}
		internal.addAttribute(name, value);
		return this;
	}

	@Override
	public XmlElement setValue(String value) {
		internal.setText(value);
		return this;
	}

	@Override
	public XmlElement getNewChildXmlElement(String name) {
		return new Dom4jXmlElement(name, internal);
	}

	@Override
	public XmlElement addChild(XmlElement other) {
		if (other instanceof Dom4jXmlElement) {
			Dom4jXmlElement o = (Dom4jXmlElement) other;
			internal.add(o.internal);
		}
		return this;
	}

	@Override
	public XmlElement readFromStream(InputStream is) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(is);
		Element root = doc.getRootElement();
		if(root == null)
			return null;
		return new Dom4jXmlElement(root);
	}

	@Override
	public void writeToStream(OutputStream os) throws Exception {
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		writeHeader(osw, false);

		OutputFormat format = OutputFormat.createPrettyPrint();
		//format = OutputFormat.createCompactFormat();
		XMLWriter writer = new XMLWriter(osw, format);
		writer.write(internal);

		osw.close();
		os.close();
	}

	@Override
	protected Object getInternal() {
		return this.internal;
	}

	@Override
	protected XmlElement wrapObject(Object internal) {
		if(internal instanceof Element)
			return new Dom4jXmlElement((Element)internal);
		return null;
	}
}
