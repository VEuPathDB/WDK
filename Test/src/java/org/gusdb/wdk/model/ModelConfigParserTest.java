/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.IOException;

import javax.xml.bind.ValidationException;

import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.junit.Assert;
import org.xml.sax.SAXException;

/**
 * @author Jerric
 *
 */
public class ModelConfigParserTest {

    private String modelName;
    private String gusHome;

    /**
     * get and validate the input
     * @throws WdkModelException
     */
    @org.junit.Before
    public void getInput() throws WdkModelException {
        // get input from the system environment
        modelName = System.getProperty(ModelXmlParser.MODEL_NAME);
        gusHome = System.getProperty(ModelXmlParser.GUS_HOME);

        // GUS_HOME is required
        if (gusHome == null || gusHome.length() == 0)
            throw new WdkModelException("Required " + ModelXmlParser.GUS_HOME
                    + " property is missing.");

        // model name is optional
        if (modelName == null || modelName.length() == 0)
            modelName = WdkModelTestBase.SAMPLE_MODEL;
    }

    /**
     * test parsing a valid config file
     * @throws IOException 
     * @throws SAXException 
     * @throws ValidationException 
     * @throws WdkModelException 
     */
    @org.junit.Test
    public void testParseConfig()
            throws ValidationException, SAXException, IOException {
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        ModelConfig config = parser.parseConfig(modelName);
        Assert.assertNotNull(config);
    }

    /**
     * parse an invalid configuration file
     * @throws IOException 
     * @throws SAXException 
     * @throws ValidationException 
     * @throws WdkModelException 
     */
    @org.junit.Test(expected = ValidationException.class)
    public void testParseInvalidConfig()
            throws ValidationException, SAXException, IOException {
        String modelName = "sampleModel_bad_config_syntax";
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        parser.parseConfig(modelName);
    }
}
