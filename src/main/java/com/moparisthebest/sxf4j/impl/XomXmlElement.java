package com.moparisthebest.sxf4j.impl;

import nu.xom.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XomXmlElement extends AbstractXmlElement {

	private final Element internal;

	public XomXmlElement() {
		internal = null;
	}

	public XomXmlElement(Element internal) {
		this.internal = internal;
	}

	private XomXmlElement(String name, Element parent) {
		internal = new Element(name);
		if (parent == null)
			return;
		String nameSpaceUri = parent.getNamespaceURI();
		if (nameSpaceUri != null)
			internal.setNamespaceURI(nameSpaceUri);
		parent.appendChild(internal);
	}

	@Override
	public String getName() {
		return internal.getQualifiedName();
	}

	@Override
	public String getValue() {
		return internal.getValue();
	}

	@Override
	public String getAttribute(String name) {
		// Xom doesn't allow xmlns(:.*)? to be attributes, but we want them to be, so implement that here
		if (name.equals("xmlns"))
			return emptyForNull(internal.getNamespaceURI());
		String nameSpaceStart = "xmlns:";
		if (name.startsWith(nameSpaceStart) && name.length() > nameSpaceStart.length()) {
			return emptyForNull(internal.getNamespaceURI(name.split(":")[1]));
		}
		// otherwise it's a regular attribute
		Attribute attr = internal.getAttribute(name);
		return attr == null ? null : attr.getValue();
	}

	@Override
	public String[] getAttributeNames() {
		String[] ret = new String[internal.getAttributeCount()+internal.getNamespaceDeclarationCount()];
		int x = 0;
		for(; x < internal.getAttributeCount(); ++x)
			ret[x] = internal.getAttribute(x).getQualifiedName();
		// special handling for xmlns, again...
		for(int y = 0; y < internal.getNamespaceDeclarationCount(); ++y){
			String name = internal.getNamespacePrefix(y);
			name = name.isEmpty() ? "xmlns" : ("xmlns:"+name);
			ret[x++] = name;
		}
		return ret;
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
		Elements nl = internal.getChildElements();
		List<XmlElement> ret = new ArrayList<XmlElement>(nl.size());
		for(int x = 0; x < nl.size(); ++x){
			Element n = nl.get(x);
			if(name == null || name.equals(n.getQualifiedName()))
				ret.add(new XomXmlElement(n));
		}
		return ret.toArray(new XmlElement[ret.size()]);
	}

	@Override
	public XmlElement getChild(String name) {
		if(name == null)
			return null;

		//Element child = internal.getFirstChildElement(name, internal.getNamespaceURI());
		//return child == null ? null : new XomXmlElement(child);

		// in order to avoid namespace issues, we need to call getChildElements and find them ourselves...
		Elements elements = internal.getChildElements();
		for(int x = 0; x < elements.size(); ++x){
			Element element = elements.get(x);
			if(name.equals(element.getQualifiedName()))
				return new XomXmlElement(element);
		}
		return null;
	}

	@Override
	public XmlElement setAttribute(String name, String value) {
		if (name.startsWith("xmlns"))
			try {
				String prefix = name.split(":")[1];
				internal.addNamespaceDeclaration(prefix, value);
			} catch (Exception e) {
				if (name.equals("xmlns"))
					internal.setNamespaceURI(value);
				else
					internal.addAttribute(new Attribute(name, value));
			}
		else
			internal.addAttribute(new Attribute(name, value));
        return this;
	}

	@Override
	public XmlElement setValue(String value) {
		internal.appendChild(value);
        return this;
	}

	@Override
	public XmlElement getNewChildXmlElement(String name) {
		return new XomXmlElement(name, internal);
	}

	@Override
	public XmlElement addChild(XmlElement other) {
		if (other instanceof XomXmlElement) {
			XomXmlElement o = (XomXmlElement) other;
			internal.appendChild(o.internal);
		}
        return this;
	}

	@Override
	public XmlElement readFromStream(InputStream is) throws Exception {
		Builder parser = new Builder();
		Document doc = parser.build(is);
		if(doc == null)
			return null;
		return new XomXmlElement(doc.getRootElement());
	}

	@Override
	public void writeToStream(OutputStream os) throws Exception {
		final Serializer serializer;
		Element toWrite = internal;
		if(internal.getParent() != null){
			// then we already have a parent, so can't create a new document, so print a copy of this object
			toWrite = new Element(toWrite);
			serializer = new Serializer(os, "UTF-8"){
				@Override
				protected void writeXMLDeclaration() throws IOException {
					// we don't want this for a child
				}
			};
		}else
			serializer = new Serializer(os, "UTF-8");

		serializer.setIndent(2);
		//serializer.setMaxLength(64); // line length
		serializer.write(new Document(toWrite));

		os.close();
	}

	@Override
	protected Object getInternal() {
		return this.internal;
	}

	@Override
	protected XmlElement wrapObject(Object internal) {
		if(internal instanceof Element)
			return new XomXmlElement((Element)internal);
		return null;
	}
}
