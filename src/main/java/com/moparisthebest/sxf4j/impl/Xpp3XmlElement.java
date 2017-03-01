package com.moparisthebest.sxf4j.impl;

import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;

import java.io.*;

public class Xpp3XmlElement extends AbstractXmlElement {

	private final Xpp3Dom internal;

	public Xpp3XmlElement() {
		internal = null;
	}

	public Xpp3XmlElement(String name, Xpp3Dom parent) {
		internal = new Xpp3Dom(name);
		if(parent != null)
			parent.addChild(internal);
	}

	private Xpp3XmlElement(Xpp3Dom internal) {
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
		return new Xpp3XmlElement(internal.getChild(name));
	}

	@Override
	public XmlElement getNewChildXmlElement(String name){
		return new Xpp3XmlElement(name, internal);
	}

	@Override
	public XmlElement addChild(XmlElement other) {
		if (other instanceof Xpp3XmlElement)
			internal.addChild(((Xpp3XmlElement) other).internal);
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
		return new Xpp3XmlElement(Xpp3DomBuilder.build(new XmlStreamReader(is)));
	}

	@Override
	public void writeToStream(OutputStream os) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		writeHeader(osw, true);

		Xpp3DomWriter.write(osw, internal);
		osw.close();
		os.close();
	}

	@Override
	protected Object getInternal() {
		return this.internal;
	}

	@Override
	protected XmlElement wrapObject(Object internal) {
		if(internal instanceof Xpp3Dom)
			return new Xpp3XmlElement((Xpp3Dom)internal);
		return null;
	}

}
