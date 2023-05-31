/*
 * Copyright (c) 2023 University of Tartu
 */
package org.qsardb.editor.container.cargo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.dmg.pmml.ObjectFactory;
import org.dmg.pmml.PMML;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class PmmlFilter extends XMLFilterImpl {

	private static JAXBContext jaxbContext = null;

	private PmmlFilter(XMLReader reader) {
		super(reader);
	}

	@Override
	public void startElement(String ns, String lname, String qname, Attributes atts) throws SAXException{
		atts = fixAttributes(lname, atts);
		ns = fixNamespace(ns);
		super.startElement(ns, lname, qname, atts);
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		namespaceURI = fixNamespace(namespaceURI);
		super.endElement(namespaceURI, localName, qualifiedName);
	}

	private String fixNamespace(String ns) {
		if (ns.startsWith("http://www.dmg.org/PMML-")) {
			return "http://www.dmg.org/PMML-4_1";
		}
		return ns;
	}

	private Attributes fixAttributes(String localName, Attributes attributes) {
		if (localName.equals("PMML")) {
			int idx = attributes.getIndex("", "version");
			if (idx > 0) {
				AttributesImpl atts = new AttributesImpl(attributes);
				atts.setValue(idx, "4.1");
				return atts;
			}
		}

		if (localName.equals("MiningField")) {
			int idx = attributes.getIndex("", "usageType");
			if (idx > 0 && attributes.getValue(idx).equals("target")) {
				AttributesImpl atts = new AttributesImpl(attributes);
				atts.setValue(idx, "predicted");
				return atts;
			}
		}

		return attributes;
	}

	static public PMML unmarshal(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			if(jaxbContext == null){
				jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			}

			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);

            XMLReader reader = parserFactory.newSAXParser().getXMLReader();
			SAXSource source = new SAXSource(new PmmlFilter(reader), new InputSource(fis));
			return (PMML)jaxbContext.createUnmarshaller().unmarshal(source);
		} catch (ParserConfigurationException | JAXBException | SAXException ex) {
			throw new IOException(ex);
		}
	}

}
