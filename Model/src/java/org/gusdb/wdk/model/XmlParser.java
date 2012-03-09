/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * @author Jerric
 * 
 */
public abstract class XmlParser {

    private static final Logger logger = Logger.getLogger(XmlParser.class);

    protected String gusHome;
    protected ValidationDriver validator;
    protected Digester digester;

    public XmlParser(String gusHome, String schemaPath) throws SAXException,
            IOException {
        this.gusHome = gusHome;

        // get model schema file and xml schema file
        URL schemaURL = makeURL(gusHome, schemaPath);

        // config validator and digester
        validator = configureValidator(schemaURL);
        digester = configureDigester();
    }

    private ValidationDriver configureValidator(URL schemaURL)
            throws SAXException, IOException {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
        PropertyMap schemaProperties = new SinglePropertyMap(
                ValidateProperty.ERROR_HANDLER, errorHandler);
        ValidationDriver validator = new ValidationDriver(schemaProperties,
                PropertyMap.EMPTY);
        validator.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));
        return validator;
    }

    protected URL makeURL(String parent, String relativePath)
            throws MalformedURLException {
        String url = parent + "/" + relativePath;
        String lower = url.toLowerCase();
        if (lower.startsWith("file:/") || lower.startsWith("http://")
                || lower.startsWith("https://") || lower.startsWith("ftp://")
                || lower.startsWith("ftps://")) {
            return new URL(url);
        } else {
            File file = new File(url);
            return file.toURI().toURL();
        }
    }

    protected void validate(URL modelXmlURL) throws SAXException, IOException,
            WdkModelException {
        logger.debug("Validating model file: " + modelXmlURL);
        String err = "Validation failed: " + modelXmlURL.toExternalForm();
        try {
            InputSource is = ValidationDriver.uriOrFileInputSource(modelXmlURL.toExternalForm());
            boolean result = validator.validate(is);
            if (!result) {
                logger.error(err);
                throw new WdkModelException(err);
            }
        } catch (SAXException ex) {
            logger.error(err + "\n" + ex);
            throw ex;
        } catch (IOException ex) {
            logger.error(err + "\n" + ex);
            throw ex;
        }
    }

    /**
     * Load XML Document from string content with out validation & substitution.
     * 
     * @param content
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected Document loadDocument(String content) throws SAXException,
            IOException, ParserConfigurationException {
        // load the content into DOM model
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // turn off validation here, since we don't use DTD; validation is done
        // before this point
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
        // builder.setErrorHandler(errorHandler);
        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {

            // ignore fatal errors (an exception is guaranteed)
            public void fatalError(SAXParseException exception)
                    throws SAXException {
                exception.printStackTrace(System.err);
            }

            // treat validation errors as fatal
            public void error(SAXParseException e) throws SAXParseException {
                e.printStackTrace(System.err);
                throw e;
            }

            // dump warnings too
            public void warning(SAXParseException err) throws SAXParseException {
                System.err.println("** Warning" + ", line "
                        + err.getLineNumber() + ", uri " + err.getSystemId());
                System.err.println("   " + err.getMessage());
            }
        });

        InputStream stream = new ByteArrayInputStream(content.getBytes());
        Document doc = builder.parse(stream);
        stream.close();
        return doc;
    }

    protected void configureNode(Digester digester, String path,
            Class<?> nodeClass, String method) {
        digester.addObjectCreate(path, nodeClass);
        digester.addSetProperties(path);
        digester.addSetNext(path, method);
    }

    protected abstract Digester configureDigester();
}
