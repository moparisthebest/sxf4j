package org.moparscape.xml.impl;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class W3CXmlElement extends AbstractXmlElement {

	private final Element internal;

	public W3CXmlElement(){
		internal = null;
	}

	public W3CXmlElement(String name, Element parent) {
		if(parent == null){
			DocumentBuilder db = null;
			try{
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(db == null){
				internal = null;
				return;
			}
			Document doc = db.newDocument();
			internal = doc.createElement(name);
			doc.appendChild(internal);
		}else{
			internal = parent.getOwnerDocument().createElement(name);
			parent.appendChild(internal);
		}
	}

	private W3CXmlElement(Element internal) {
		this.internal = internal;
	}

	@Override
	public String getName() {
		return internal.getNodeName();
	}

	@Override
	public String getValue() {
		return internal.getTextContent();
	}

	@Override
	public String getAttribute(String name) {
		return internal.getAttributes().getNamedItem(name).getNodeValue();
	}

	@Override
	public String[] getAttributeNames() {
		NamedNodeMap nnm = internal.getAttributes();
		if(nnm == null)
			return new String[0];
		String[] ret = new String[nnm.getLength()];
		for(int x = 0; x < ret.length; ++x)
			ret[x] = nnm.item(x).getNodeName();
		return ret;
	}

	@Override
	public XmlElement getParent() {
		return wrapParent(internal.getParentNode());
	}

	@Override
	public int getChildCount() {
		return internal.getChildNodes().getLength();
	}

	@Override
	public XmlElement[] getChildren(String name){
		NodeList nl = internal.getChildNodes();
		List<XmlElement> ret = new ArrayList<XmlElement>(nl.getLength());
		for(int x = 0; x < nl.getLength(); ++x){
			Node n = nl.item(x);
			if( n instanceof Element && (name == null || name.equals(n.getNodeName())) )
				ret.add(new W3CXmlElement((Element)n));
		}
		return ret.toArray(new XmlElement[ret.size()]);
	}

	@Override
	public XmlElement getChild(String name) {
		return new W3CXmlElement((Element)internal.getElementsByTagName(name).item(0));
	}

	@Override
	public XmlElement getNewChildXmlElement(String name) {
		return new W3CXmlElement(name, this.internal);
	}

	@Override
	public XmlElement addChild(XmlElement other) {
		if (other instanceof W3CXmlElement){
			W3CXmlElement o = (W3CXmlElement) other;
			internal.appendChild(o.internal);
		}
		return this;
	}

	@Override
	public XmlElement setAttribute(String name, String value) {
		internal.setAttribute(name, value);
        return this;
	}

	@Override
	public XmlElement setValue(String value) {
		internal.setTextContent(value);
        return this;
	}

	@Override
	public XmlElement readFromStream(InputStream is) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		if(db == null)
			return null;
		Document doc = db.parse(is);
		if(doc == null)
			return null;
		return new W3CXmlElement(doc.getDocumentElement());
	}

	@Override
	public void writeToStream(OutputStream os) throws Exception {
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

		// Use a Transformer for output
		Transformer transformer = TransformerFactory.newInstance().newTransformer();

		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, getParent() == null ? "no" : "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(new DOMSource(internal), new StreamResult(osw));

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
			return new W3CXmlElement((Element)internal);
		return null;
	}

}
