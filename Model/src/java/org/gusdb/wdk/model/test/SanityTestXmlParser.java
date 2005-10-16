package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
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

public class SanityTestXmlParser {

    private static final String DEFAULT_SCHEMA_NAME = "sanityModel.rng";

    public static SanityModel parseXmlFile(URL modelXmlURL, URL modelPropURL,
            URL schemaURL) throws WdkModelException {

        if (schemaURL == null) {
            schemaURL = SanityModel.INSTANCE.getClass().getResource(
                    DEFAULT_SCHEMA_NAME);
        }

        // NOTE: we are validating before we substitute in the properties
        // so that the validator will operate on a file instead of a stream.
        // this way the validator spits out line numbers for errors

        if (!validModelFile(modelXmlURL, schemaURL)) {
            throw new WdkModelException("Model validation failed");
        }

        Digester digester = configureDigester();
        SanityModel model = null;

        try {
            InputStream modelXmlStream = makeModelXmlStream(modelXmlURL,
                    modelPropURL);
            model = (SanityModel) digester.parse(modelXmlStream);
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }

        setModelDocument(model, modelXmlURL, modelPropURL);

        return model;
    }

    private static InputStream makeModelXmlStream(URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {
        InputStream modelXmlStream;

        if (modelPropURL != null) {
            modelXmlStream = configureModelFile(modelXmlURL, modelPropURL);
        } else {
            try {
                modelXmlStream = modelXmlURL.openStream();
            } catch (FileNotFoundException e) {
                throw new WdkModelException(e);
            } catch (IOException e) {
                throw new WdkModelException(e);
            }
        }
        return modelXmlStream;
    }

    private static void setModelDocument(SanityModel model, URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {
        try {
            InputStream modelXmlStream = makeModelXmlStream(modelXmlURL,
                    modelPropURL);
            model.setDocument(buildDocument(modelXmlStream));
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        } catch (ParserConfigurationException e) {
            throw new WdkModelException(e);
        }
    }

    public static Document buildDocument(InputStream modelXMLStream)
            throws ParserConfigurationException, SAXException, IOException {

        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Turn on validation, and turn off namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
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

        doc = builder.parse(modelXMLStream);
        return doc;
    }

    private static boolean validModelFile(URL modelXmlURL, URL schemaURL)
            throws WdkModelException {

        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        try {

            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(
                    ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties,
                    PropertyMap.EMPTY, null);

            vd.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));

            // System.err.println("modelXMLURL is "+modelXmlURL);

            InputSource is = ValidationDriver.uriOrFileInputSource(modelXmlURL.toExternalForm());
            // return vd.validate(new InputSource(modelXMLStream));
            return vd.validate(is);

        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }

    private static Digester configureDigester() {

        Digester digester = new Digester();
        digester.setValidating(false);

        // Root -- Sanity Model

        digester.addObjectCreate("sanityModel", SanityModel.class);
        digester.addSetProperties("sanityModel");

        // SanityRecord

        /**/digester.addObjectCreate("sanityModel/sanityRecord",
                SanityRecord.class);

        /**/digester.addSetProperties("sanityModel/sanityRecord");

        /**/digester.addSetNext("sanityModel/sanityRecord", "addSanityRecord");

        /**/digester.addObjectCreate("sanityModel/sanityQuery",
                SanityQuery.class);

        /**/digester.addSetProperties("sanityModel/sanityQuery");

        /*   */digester.addObjectCreate("sanityModel/sanityQuery/sanityParam",
                SanityParam.class);

        /*   */digester.addSetProperties("sanityModel/sanityQuery/sanityParam");

        /*   */digester.addSetNext("sanityModel/sanityQuery/sanityParam",
                "addParam");

        /**/digester.addSetNext("sanityModel/sanityQuery", "addSanityQuery");

        /**/digester.addObjectCreate("sanityModel/sanityQuestion",
                SanityQuestion.class);

        /**/digester.addSetProperties("sanityModel/sanityQuestion");

        /*   */digester.addObjectCreate(
                "sanityModel/sanityQuestion/sanityParam", SanityParam.class);

        /*   */digester.addSetProperties("sanityModel/sanityQuestion/sanityParam");

        /*   */digester.addSetNext("sanityModel/sanityQuestion/sanityParam",
                "addParam");

        /**/digester.addSetNext("sanityModel/sanityQuestion",
                "addSanityQuestion");

        // Add XmlQuestion Tests
        /**/digester.addObjectCreate("sanityModel/sanityXmlQuestion",
                SanityXmlQuestion.class);

        /**/digester.addSetProperties("sanityModel/sanityXmlQuestion");

        /**/digester.addSetNext("sanityModel/sanityXmlQuestion",
                "addSanityXmlQuestion");

        return digester;
    }

    /**
     * Substitute property values into model xml
     */
    public static InputStream configureModelFile(URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {

        try {
            StringBuffer substituted = new StringBuffer();
            Properties properties = new Properties();
            properties.load(modelPropURL.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    modelXmlURL.openStream()));
            while (reader.ready()) {
                String line = reader.readLine();
                line = substituteProps(line, properties);
                substituted.append(line);
            }

            return new ByteArrayInputStream(substituted.toString().getBytes());
        } catch (FileNotFoundException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }

    static String substituteProps(String string, Properties properties) {
        Enumeration propNames = properties.propertyNames();
        String newString = string;
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String value = properties.getProperty(propName);
            newString = newString.replaceAll("\\@" + propName + "\\@", value);
        }
        return newString;
    }

    public static void main(String[] args) {
        try {
            File modelXmlFile = new File(args[0]);
            File modelPropFile = null;
            if (args.length > 1) {
                modelPropFile = new File(args[1]);
            }
            File schemaFile = new File(System.getProperty("schemaFile"));
            SanityModel sanityModel = parseXmlFile(modelXmlFile.toURL(),
                    modelPropFile.toURL(), schemaFile.toURL());

            System.out.println(sanityModel.toString());

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
