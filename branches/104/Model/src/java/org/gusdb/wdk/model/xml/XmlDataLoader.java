/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.gusdb.wdk.model.WdkModelException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlDataLoader {

    private URL schemaURL;

    /**
     * 
     */
    public XmlDataLoader(URL schemaURL) {
        this.schemaURL = schemaURL;
    }

    public XmlAnswerValue parseDataFile(URL xmlDataURL) throws WdkModelException {
        // validate the xml data file
        if (!validDataFile(xmlDataURL, schemaURL))
            throw new WdkModelException("Validation to data xml file failed: "
                    + xmlDataURL.toExternalForm());

        // Create the InputStream of xmlData
        InputStream xmlDataStream = null;
        try {
            xmlDataStream = xmlDataURL.openStream();
            return parseDataStream(xmlDataStream);
        } catch (IOException ex) {
            throw new WdkModelException("Could not parse data file at "
                    + xmlDataURL, ex);
        } finally {
            try {
                if (xmlDataStream != null) xmlDataStream.close();
            } catch (IOException ex) {
                throw new WdkModelException(ex);
            }
        }
    }

    public XmlAnswerValue parseDataStream(InputStream xmlDataStream)
            throws WdkModelException {
        // I have to bypass the validation part for stream source, since the
        // validator need to use a String or File to validate it

        // configure the digester
        Digester digester = configureDigester();

        try {
            // load and parse the data source
            XmlAnswerValue answer = (XmlAnswerValue) digester.parse(xmlDataStream);

            return answer;
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        }
    }

    private boolean validDataFile(URL dataXmlURL, URL schemaURL)
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

            // validate the data xml file
            InputSource is = ValidationDriver.uriOrFileInputSource(dataXmlURL.toExternalForm());
            return vd.validate(is);
        } catch (SAXException e) {
            throw new WdkModelException("parsing: " + dataXmlURL, e);
        } catch (IOException e) {
            throw new WdkModelException("parsing: " + dataXmlURL, e);
        }
    }

    private Digester configureDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        // set the root
        digester.addObjectCreate("xmlAnswer", XmlAnswerValue.class);
        digester.addSetProperties("xmlAnswer");

        // xmlRecord
        digester.addObjectCreate("xmlAnswer/record", XmlRecordInstance.class);
        digester.addSetProperties("xmlAnswer/record");

        // xmlAttribute
        digester.addObjectCreate("xmlAnswer/record/attribute",
                XmlAttributeValue.class);
        digester.addSetProperties("xmlAnswer/record/attribute");
        digester.addCallMethod("xmlAnswer/record/attribute", "setValue", 1);
        digester.addCallParam("xmlAnswer/record/attribute", 0);

        digester.addSetNext("xmlAnswer/record/attribute", "addAttribute");

        // xmlTable
        digester.addObjectCreate("xmlAnswer/record/table", XmlTableValue.class);
        digester.addSetProperties("xmlAnswer/record/table");

        // xmlRow
        digester.addObjectCreate("xmlAnswer/record/table/row",
                XmlRowValue.class);
        digester.addSetProperties("xmlAnswer/record/table/row");

        // xmlAttribute - columns
        digester.addObjectCreate("xmlAnswer/record/table/row/attribute",
                XmlAttributeValue.class);
        digester.addSetProperties("xmlAnswer/record/table/row/attribute");
        digester.addCallMethod("xmlAnswer/record/table/row/attribute",
                "setValue", 1);
        digester.addCallParam("xmlAnswer/record/table/row/attribute", 0);
        digester.addSetNext("xmlAnswer/record/table/row/attribute", "addColumn");

        digester.addSetNext("xmlAnswer/record/table/row", "addRow");

        digester.addSetNext("xmlAnswer/record/table", "addTable");

        digester.addSetNext("xmlAnswer/record", "addRecordInstance");

        return digester;
    }
}
