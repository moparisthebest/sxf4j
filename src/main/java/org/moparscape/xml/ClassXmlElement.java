package org.moparscape.xml;

import org.moparscape.xml.impl.AbstractXmlElement;
import org.moparscape.xml.impl.XmlElement;
import org.moparscape.xml.impl.XmlElementFactory;

import java.lang.annotation.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ClassXmlElement {

	public static final String[] errors = new String[]{
			"ERROR GETTING VALUE"
			, "ERROR: Cannot add " + ClassXmlElement.class.getSimpleName() + " without value as attribute!"
			, "WARNING: INFINITE RECURSION DETECTED"
	};

	public static boolean debug = false;

	private final String uuid;

	{
		uuid = UUID.randomUUID().toString();
	}

	@Target({ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Root {
		String name() default "";
	}

	@Target({ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Attribute {
		String name() default "";
	}

	@Target({ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Child {
		String name() default "";

		String childName() default "";

		String[] children() default {};
	}

	private String getString(Object ret) {
		if (ret == null)
			ret = errors[0];
		return ret.toString();
	}

	private void addToDom(XmlElement addTo, Object key, Object val, boolean child) {
		if (child)
			addTo.getNewChildXmlElement(key.toString()).setValue(val.toString());
		else
			addTo.setAttribute(key.toString(), val.toString());
	}

	private XmlElement getAddTo(XmlElement ret, String name, boolean child) {
		if (!child) // if we are expecting an attribute, never create a subobject
			return ret;
		XmlElement addTo = ret;
		if (!name.isEmpty())
			addTo = ret.getNewChildXmlElement(name);
		return addTo;
	}

	private void addValueToDom(Object value, MethodField field, XmlElement ret, String name, int childDepth, boolean child, final Set<String> recursionDetector, final XmlElementFactory factory) {
		if (value == null)
			value = field.getValue();

		if (name == null)
			name = field.getName();

		// if it's an array, we convert it to a List
		// so our List handling code can be re-used
		if (value instanceof Object[])
			value = Arrays.asList((Object[]) value);
		if (value instanceof List) {
			XmlElement addTo = getAddTo(ret, name, child);
			String childName = field.getName(child, childDepth);
			//System.out.printf("name: '%s', childName: '%s'\n", name, childName);
			List list = (List) value;
			if (child) {
				++childDepth;
				for (Object o : list)
					addValueToDom(o, field, addTo, childName, childDepth, child, recursionDetector, factory);
			} else {
				// if we want an attribute, assume they want the list delimited by spaces
				// don't bother recursing, attributes can only handle values of type String anyhow
				StringBuilder sb = new StringBuilder();
				for (Object o : list)
					sb.append(o.toString()).append(" ");
				sb.delete(sb.length() - 1, sb.length());
				addToDom(addTo, name, sb.toString(), false);
			}
		} else if (value instanceof Map) {
			XmlElement addTo = getAddTo(ret, name, child);
			Map<?, ?> map = (Map<?, ?>) value;
			for (Map.Entry entry : map.entrySet()) {
				// not sure how to handle this, I guess key would be name
				addValueToDom(entry.getValue(), field, addTo, entry.getKey().toString(), childDepth, child, recursionDetector, factory);
				//addToDom(addTo, entry.getKey(), entry.getValue(), child);
			}
			++childDepth;
		} else if (value instanceof ClassXmlElement) {
			ClassXmlElement other = ((ClassXmlElement) value);
			if (child)
				other.toXml(factory, getAddTo(ret, name, child), recursionDetector);
			else {
				XmlElement xmlChild = other.toXml(factory, null, recursionDetector);
				String xmlChildValue = xmlChild.getValue();
				if (xmlChildValue == null)
					xmlChildValue = errors[1];
				ret.setAttribute(xmlChild.getName(), xmlChildValue);
			}
		} else
			addToDom(ret, name, getString(value), child);
	}

	private void processMethodField(MethodField field, XmlElement ret, final Set<String> recursionDetector, final XmlElementFactory factory) {
		if (debug)
			System.out.println(field);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		if (field.isAnnotationPresent(Child.class) || field.isAnnotationPresent(Attribute.class))
			addValueToDom(null, field, ret, null, 0, field.isChild(), recursionDetector, factory);
		field.setAccessible(accessible);
	}

	public final XmlElement toXml() {
		return this.toXml(null);
	}

	public final XmlElement toXml(final XmlElementFactory factory) {
		return this.toXml(factory == null ? AbstractXmlElement.getFactory() : factory, null, new HashSet<String>());
	}

	private XmlElement toXml(final XmlElementFactory factory, final XmlElement parent, final Set<String> recursionDetector) {

		Class thisClass = this.getClass();
		String rootName = thisClass.getSimpleName();
		if (thisClass.isAnnotationPresent(Root.class)) {
			String rootAnnotationName = this.getClass().getAnnotation(Root.class).name();
			if (!rootAnnotationName.isEmpty())
				rootName = rootAnnotationName;
		}
		XmlElement ret = parent != null ? parent.getNewChildXmlElement(rootName) : factory.getNewChildXmlElement(rootName);

		if (recursionDetector.contains(this.uuid)) {
			if (debug)
				System.out.println("Infinite Recursion detected, returning...");
			ret.setValue(errors[2]);
			return ret;
			//return E("WARNING", "INFINITE RECURSION DETECTED");
			//return null;
		}
		recursionDetector.add(this.uuid);

		List<MethodField> mfList = new LinkedList<MethodField>();

		for (Field field : thisClass.getDeclaredFields())
			mfList.add(new MethodField(this).setField(field));
		for (Method method : thisClass.getDeclaredMethods())
			mfList.add(new MethodField(this).setMethod(method));

		// find and add xmlns first thing, some implementations require this (currently Dom4jXmlElement and XomXmlElement)
		Iterator<MethodField> iter = mfList.iterator();
		while (iter.hasNext()) {
			MethodField mf = iter.next();
			if(mf.isChild())
				continue;
			String name = mf.getName();
			if(!name.equals("xmlns") && !name.startsWith("xmlns:"))
				continue;
			// otherwise, process the entry and remove it from the list
			processMethodField(mf, ret, recursionDetector, factory);
			iter.remove();
		}

		for(MethodField mf : mfList)
			processMethodField(mf, ret, recursionDetector, factory);

		recursionDetector.remove(this.uuid);
		return ret;
	}

	private static class MethodField extends java.lang.reflect.AccessibleObject {
		private final Object container;

		private Method m;
		private Field f;

		private AccessibleObject ao;

		public MethodField(Object container) {
			this.container = container;
		}

		public MethodField setField(Field f) {
			return setMethodField(null, f);
		}

		public MethodField setMethod(Method m) {
			return setMethodField(m, null);
		}

		private MethodField setMethodField(Method m, Field f) {
			if (m == null && f == null)
				throw new Error("Method and field cannot be null!");
			this.m = m;
			if (m != null)
				ao = m;
			this.f = f;
			if (f != null)
				ao = f;
			return this;
		}

		public boolean isChild() {
			return ao.isAnnotationPresent(Child.class);
		}

		public String getName() {
			return getName(false, 0);
		}

		public String getName(boolean child, int childDepth) {
			String name = "";
			if (ao.isAnnotationPresent(Attribute.class))
				name = ao.getAnnotation(Attribute.class).name();
			else if (ao.isAnnotationPresent(Child.class)) {
				name = ao.getAnnotation(Child.class).name();
				if (child) {
					String childName = ao.getAnnotation(Child.class).childName();
					String[] childrenNames = ao.getAnnotation(Child.class).children();
					if (childDepth < childrenNames.length)
						childName = childrenNames[childDepth];

					// if childName isn't set, but children is, then take the last valid child
					if (childName.isEmpty() && childrenNames.length > 0)
						childName = childrenNames[childrenNames.length - 1];

					// if the above doesn't exist, then use name but stripS
					if (childName.isEmpty())
						name = stripS(name);
					else
						name = childName;
				}
			}

			if (!name.isEmpty())
				return name;

			if (f != null)
				name = f.getName();
			else {
				name = m.getName();
				// if there is a leading get, strip it off
				if (name.toLowerCase().startsWith("get") && name.length() > 3)
					name = name.substring(3, name.length());
			}
			if (child)
				name = stripS(name);

			// then set it as the field name, replacing _ with :
			name = name.replace('_', ':');
			return name;
		}

		public static String stripS(String name) {
			String nameLower = name.toLowerCase();
			if (name.length() < 2 || !nameLower.endsWith("s"))
				return name;
			if (name.length() > 3 && nameLower.endsWith("ies"))
				name = name.substring(0, name.length() - 3) + "y";
			else
				name = name.substring(0, name.length() - 1);
			return name;

		}

		public Object getValue() {
			Object ret = null;
			try {
				if (f != null)
					ret = f.get(container);
				else
					ret = m.invoke(container);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		}

		@Override
		public void setAccessible(boolean flag) throws SecurityException {
			ao.setAccessible(flag);
		}

		@Override
		public boolean isAccessible() {
			return ao.isAccessible();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return ao.getAnnotation(annotationClass);
		}

		@Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
			return ao.isAnnotationPresent(annotationClass);
		}

		@Override
		public Annotation[] getAnnotations() {
			return ao.getAnnotations();
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return ao.getDeclaredAnnotations();
		}

		@Override
		public int hashCode() {
			return ao.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return ao.equals(obj);
		}

		@Override
		public String toString() {
			return (f == null ? "method" : "field ") + "(" + (isChild() ? "child)    " : "attribute)") + ": " + ao.toString();
		}
	}
}
