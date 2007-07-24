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
    public static final String SAMPLE_PROJECT_ID = "SampleDB";

    protected String gusHome;
    protected String projectId;
    protected WdkModel wdkModel;

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
    @org.junit.Before
    public void initializeModel()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        // get input from the system environment
        projectId = System.getProperty(Utilities.ARGUMENT_PROJECT_ID);
        gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        // GUS_HOME is required
        if (gusHome == null || gusHome.length() == 0)
            throw new WdkModelException("Required "
                    + Utilities.SYSTEM_PROPERTY_GUS_HOME + " property is missing.");

        // project id is optional
        if (projectId == null || projectId.length() == 0)
            projectId = SAMPLE_PROJECT_ID;

        // initialize the model
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        wdkModel = parser.parseModel(projectId);
    }
}
