package org.gusdb.wdk.model.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.gusdb.wdk.model.WdkModelException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 */

/**
 * @author Jerric
 * @created Oct 20, 2005
 */
public class XmlConverter {

    public static void convert(InputStream inStream, InputStream xslStream,
            OutputStream outStream, String msg) throws WdkModelException {
        TransformerFactory Factory = TransformerFactory.newInstance();

        if (!Factory.getFeature(SAXSource.FEATURE)
                || !Factory.getFeature(SAXResult.FEATURE))
            throw new WdkModelException(
                    "Unsupported XML feature. Conversion cancelled.");

        SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) Factory);
        // Create an XMLFilter for each stylesheet.
        XMLFilter xmlFilter;
        try {
            xmlFilter = saxTFactory.newXMLFilter(new StreamSource(xslStream));
            XMLReader reader = XMLReaderFactory.createXMLReader();
            xmlFilter.setParent(reader);

            Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
            xmlProps.setProperty("indent", "yes");
            xmlProps.setProperty("standalone", "no");
            Serializer serializer = SerializerFactory.getSerializer(xmlProps);
            serializer.setOutputStream(outStream);
            xmlFilter.setContentHandler(serializer.asContentHandler());

            xmlFilter.parse(new InputSource(inStream));
        } catch (TransformerConfigurationException ex) {
            throw new WdkModelException(msg, ex);
        } catch (SAXException ex) {
            throw new WdkModelException(msg, ex);
        } catch (IOException ex) {
            throw new WdkModelException(msg, ex);
        }
    }

    public static void main(String[] args) throws WdkModelException {
        // get the file parameters
        if (args.length != 3) {
            System.err.println("Usage: xmlConvert <input_xml> <xsl> <output_xml>");
            System.exit(-1);
        }

        File xmlDataDir = new File(System.getProperty("xmlDataDir"));
        File inXmlFile = new File(xmlDataDir, args[0]);
        File inXslFile = new File(xmlDataDir, args[1]);
        File outXmlFile = new File(xmlDataDir, args[2]);

        if (!inXmlFile.exists() || !inXslFile.exists()) {
            System.err.println("The input XML or XSL does not exist!");
            System.exit(-1);
        }

        try {
            InputStream inXmlStream = new FileInputStream(inXmlFile);
            InputStream inXslStream = new FileInputStream(inXslFile);
            OutputStream outXmlStream = new FileOutputStream(outXmlFile);

            // convert the xml
            XmlConverter.convert(inXmlStream, inXslStream, outXmlStream,
                    inXslFile.getName());

            // save the result
            outXmlStream.flush();
            outXmlStream.close();
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        }
        System.out.println("Xml file is converted successfully.");
    }
}
