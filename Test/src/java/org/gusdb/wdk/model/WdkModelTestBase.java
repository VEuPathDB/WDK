/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.xml.sax.SAXException;

/**
 * @author Jerric
 * 
 */
public abstract class WdkModelTestBase {

    /**
     * the correct, multi-part model
     */
    public static final String SAMPLE_MODEL = "sampleModel";

    protected static WdkModel wdkModel;

    /**
     * load the model
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.BeforeClass
    public static void initializeModel()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        // get input from the system environment
        String modelName = System.getProperty(ModelXmlParser.MODEL_NAME);
        String gusHome = System.getProperty(ModelXmlParser.GUS_HOME);

        // GUS_HOME is required
        if (gusHome == null || gusHome.length() == 0)
            throw new WdkModelException("Required " + ModelXmlParser.GUS_HOME
                    + " property is missing.");

        // model name is optional
        if (modelName == null || modelName.length() == 0)
            modelName = SAMPLE_MODEL;

        // initialize the model
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        wdkModel = parser.parseModel(modelName);
    }

}
