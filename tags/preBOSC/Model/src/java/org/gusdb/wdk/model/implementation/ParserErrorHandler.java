package org.gusdb.gus.wdk.model.implementation;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

public class ParserErrorHandler extends DefaultHandler {

    public ParserErrorHandler() {
	super();
    }

    public void error(SAXParseException e) throws SAXException {
	throw e;
    }

    public void fatalrror(SAXParseException e) throws SAXException {
	throw e;
    }

    public void warning(SAXParseException e) throws SAXException {
	throw e;
    }
}
