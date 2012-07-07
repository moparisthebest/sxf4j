package org.moparscape.xml.impl;

import java.io.*;
import java.lang.reflect.Constructor;

public abstract class AbstractXmlElement implements XmlElement, XmlElementFactory {

	public static final String implProperty = "xmlElementImpl";

	// provided implementations
	public static final String XPP3 = "Xpp3";
	public static final String XPP = "Xpp";
	public static final String DOM4J = "Dom4j";
	public static final String XOM = "Xom";
	public static final String W3C = "W3C";

	public static XmlElementFactory getFactory() {
		XmlElementFactory ret = null;

		//ret = new org.moparscape.xml.impl.W3CXmlElement();
		//ret = new org.moparscape.xml.impl.XppXmlElement();
		//ret = new org.moparscape.xml.impl.Xpp3XmlElement();
		//ret = new org.moparscape.xml.impl.Dom4jXmlElement();
		//ret = new org.moparscape.xml.impl.XomXmlElement();
		final String xmlDocType = System.getProperty(implProperty);
		//System.out.println("xmlDocType: "+xmlDocType);
		String implPkgClass = AbstractXmlElement.class.getPackage().getName() + "." + xmlDocType;
		if (ret == null && xmlDocType != null)
			ret = objectForName(implPkgClass + "XmlElement", null);
		if (ret == null && xmlDocType != null)
			ret = objectForName(implPkgClass, null);
		if (ret == null && xmlDocType != null)
			ret = objectForName(xmlDocType, null);
		// try them in a defined default order
		if (ret == null) {
			Class[] xmlDocs = new Class[]{XppXmlElement.class, Xpp3XmlElement.class, Dom4jXmlElement.class, XomXmlElement.class};
			for (Class xmlDoc : xmlDocs) {
				ret = objectForName(null, xmlDoc);
				if (ret != null)
					break;
			}
		}
		// as a last resort, W3CXmlElement should ALWAYS be available
		if (ret == null)
			ret = new W3CXmlElement();
		return ret;
	}

	@SuppressWarnings({"unchecked"})
	private static <E> E objectForName(String name, Class clazz) {
		try {
			if (clazz == null)
				clazz = Class.forName(name);
			Constructor constructor = null;
			try {
				constructor = clazz.getDeclaredConstructor();
			} catch (Exception e) {
				// we would only reach here if there is no default no-arg constructor
				// we must use sun classes to get around this, unfortunately
				sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
				constructor = rf.newConstructorForSerialization(clazz, Object.class.getDeclaredConstructor(new Class[0]));
			}
			if (!constructor.isAccessible())
				constructor.setAccessible(true);
			return (E) constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static String emptyForNull(String ret) {
		return ret == null ? "" : ret;
	}

	protected void writeHeader(OutputStreamWriter osw, boolean newLine) throws IOException {
		// only write the header if parent is null (top level)
		if (getParent() != null)
			return;
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		if (newLine)
			header += "\n";
		osw.write(header, 0, header.length());
	}

	@Override
	public final XmlElement[] getChildren() {
		return getChildren(null);
	}

	@Override
	public void writeToFile(File file) throws Exception {
		writeToStream(new FileOutputStream(file));
	}

	@Override
	public void writeToFile(String fileName) throws Exception {
		writeToFile(new File(fileName));
	}

	@Override
	public String toString() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			writeToStream(bos);
			return new String(bos.toByteArray(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String(bos.toByteArray());
	}

	@Override
	public String toStringCompact() {
		// this removes all whitespace between tags
		String prettyXml = toString().replaceAll(">\\s+<", "><");
		// now we have to remove all whitespace between attributes
		StringBuilder compactXml = new StringBuilder(prettyXml.length());
		boolean inTag = false;
		boolean inAttribute = false;
		boolean appendedWhitespace = false;
		for (int x = 0; x < prettyXml.length(); ++x) {
			char c = prettyXml.charAt(x);
			if (c == '<')
				inTag = true;
			else if (c == '>')
				inTag = false;
			else if (c == '"') {
				inAttribute = !inAttribute;
				appendedWhitespace = false;
			}

			if (inTag && !inAttribute && Character.isWhitespace(c)) {
				if (appendedWhitespace)
					continue;
				appendedWhitespace = true;
				c = ' ';
			}

			compactXml.append(c);
		}
		return compactXml.toString();
	}

	private int byteLength(String s) {
		try {
			return s.getBytes("UTF-8").length;
		} catch (Exception e) {
			return s.getBytes().length;
		}
	}

	@Override
	public int byteLength() {
		return byteLength(toString());
	}

	@Override
	public int byteLengthCompact() {
		return byteLength(toStringCompact());
	}

	@Override
	public XmlElement readFromFile(File file) throws Exception {
		return readFromStream(new FileInputStream(file));
	}

	@Override
	public XmlElement readFromFile(String file) throws Exception {
		return readFromFile(new File(file));
	}

	@Override
	public XmlElement readFromString(String string) throws Exception {
		return readFromStream(new ByteArrayInputStream(string.getBytes()));
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return iface != null && iface.isInstance(getInternal());
	}

	@Override
	public <T> T unwrap(Class<T> iface) {
		if (!isWrapperFor(iface))
			return null;
		return iface.cast(getInternal());
	}

	/**
	 * For isWrapperFor and unwrap implementation
	 *
	 * @return
	 */
	protected abstract Object getInternal();

	protected XmlElement[] wrapArray(Object[] os) {
		XmlElement[] ret = new XmlElement[os.length];
		for (int x = 0; x < ret.length; ++x)
			ret[x] = wrapObject(os[x]);
		return ret;
	}

	protected XmlElement wrapParent(Object parent) {
		return parent == null ? null : wrapObject(parent);
	}

	protected abstract XmlElement wrapObject(Object o);

	public XmlElement copyTo(XmlElementFactory factory) throws Exception {
		if (factory == null)
			return null;

		return factory.readFromString(this.toString());
	}

	/** old way, read/write above probably better
	 public XmlElement copyTo(XmlElementFactory factory) {
	 if (factory == null)
	 return null;

	 XmlElement ret = factory.getNewChildXmlElement(this.getName());
	 copyXmlElement(this, ret);
	 return ret;
	 }

	 private void copyXmlElement(XmlElement src, XmlElement dst) {
	 // set the value
	 if (src.getValue() != null)
	 dst.setValue(src.getValue());
	 // set all the attributes
	 for (String attName : src.getAttributeNames())
	 dst.setAttribute(attName, src.getAttribute(attName));
	 // recursively set all the children
	 for (XmlElement child : src.getChildren())
	 copyXmlElement(child, dst.getNewChildXmlElement(child.getName()));
	 }
	 */
}
