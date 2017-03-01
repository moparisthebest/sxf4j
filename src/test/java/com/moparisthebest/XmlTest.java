package com.moparisthebest;

import com.moparisthebest.sxf4j.ClassXmlElement;

import com.moparisthebest.sxf4j.ClassXmlElement.Root;
import com.moparisthebest.sxf4j.impl.*;

import java.util.*;

@Root(name = "xmltest1")
public class XmlTest extends ClassXmlElement {

    private enum Bob {
        TOM,
        CHARLES,
        EDWARD
    }

    @Child
    private Bob bobenum = Bob.TOM;

    @Child
    private String bob = "tom";

    @Attribute
    private String xmlns_xsl = "http://www.w3.org/1999/XSL/Transform";

    @Attribute
    public String xmlns = "http://www.w3.org/1999/xhtml";

    @Attribute
    public static String xmlnsstatic = "http://www.w3.org/1999/xhtml";

    @Attribute
    private static String xmlns_xslstatic = "http://www.w3.org/1999/XSL/Transform";

    @Child(children = {"dep", "subdep", "subsubdep"})
    private static List<Object> deps = Arrays.asList((Object) "dep1", "dep2", "dep3"
            , new Object[]{"subdep1", "subdep2"
            , new Object[]{"subsubdep1", "subsubdep2"}
    }
            //		, new XmlTest()
    );

    @Attribute
    private String[] modules = new String[]{"mod1", "mod2"};

    @Attribute()
    private static Map<String, Object> map = new HashMap<String, Object>() {{
        put("k1", "v1");
        put("k2", "v2");
//		put("k3", new XmlTest());
//		put("k4", new Object[]{"mapsubdep1", "mapsubdep2"
//				,new Object[]{"mapsubsubdep1", "mapsubsubdep2"}
//		});
    }};

    @Child
    private static XmlTest childXmlTest = new XmlTest();
//	@Attribute private static XmlTest childXmlTest = new XmlTest();

    @Attribute
    private String getCharley() {
        return "charley content";
    }

    @Child()
    private String[] getdependencies() {
        return new String[]{"dependency1", "dependency2", "dependency3"};
    }

    public static void main(String[] args) throws Exception {
        System.setErr(System.out);
        //testXmlElement();System.exit(0);
        XmlElementFactory[] factories = new XmlElementFactory[]{
                new XppXmlElement(),
                new Xpp3XmlElement(),
                new Dom4jXmlElement(),
                new W3CXmlElement(),
                new XomXmlElement(),
        };
        for(XmlElementFactory factory : factories)
            testXmlElement(factory);
    }

    public static void testDynamicImpls() throws Exception {

        String[] impls = new String[]{
                AbstractXmlElement.XPP
                , AbstractXmlElement.XPP3
                , AbstractXmlElement.DOM4J
                , AbstractXmlElement.XOM
                , AbstractXmlElement.W3C
        };
        for (String impl : impls) {
            System.setProperty(AbstractXmlElement.implProperty, impl);
            testXmlElement();
        }
    }

    public static void testXmlElement() throws Exception {
        testXmlElement(null);
    }

    public static void testXmlElement(XmlElementFactory copyTo) throws Exception {
        XmlElement xml = new XmlTest().toXml();
        System.out.println(xml.getClass());
        //nu.xom.Element e = xml.unwrap(nu.xom.Element.class);
        //System.out.println("internal class: " + xml.unwrap(nu.xom.Element.class));
        if (copyTo != null) {
            xml = xml.copyTo(copyTo);
            System.out.println("copied: " + xml.getClass());
        }

        xml.writeToFile("./out.xml");
        System.out.printf("child name: '%s' value: '%s'\nattribute xmlns: '%s'\n",
                xml.getChild("bob").getName(),
                xml.getChild("bob").getValue(),
//			    ""
                xml.getAttribute("xmlns")
        );
        System.out.println("parent node (should be null): " + (xml.getParent() == null ? "null" : xml.getParent().getName()));
        System.out.println("parent node of child (NOT null): " + (xml.getChildren()[0].getParent() == null ? "null" : xml.getChildren()[0].getParent().getName()));

        System.out.println("attribute names:" + Arrays.toString(xml.getAttributeNames()));

        System.out.println("children count:" + xml.getChildCount());
        //System.out.println("children: "+Arrays.toString(xml.getChildren()));
        for (XmlElement children : xml.getChildren()) {
            System.out.print(children.getName() + ", ");
        }
        System.out.println("\n-----");
        //System.exit(0);
    }

}
