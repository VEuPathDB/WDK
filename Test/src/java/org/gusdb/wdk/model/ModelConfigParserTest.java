/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.IOException;

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
     * 
     * @throws WdkModelException
     */
    @org.junit.Before
    public void getInput() throws WdkModelException {
        // get input from the system environment
        modelName = System.getProperty(Utilities.ARGUMENT_MODEL);
        gusHome = System.getProperty(Utilities.SYS_PROP_GUS_HOME);

        // GUS_HOME is required
        if (gusHome == null || gusHome.length() == 0)
            throw new WdkModelException("Required "
                    + Utilities.SYS_PROP_GUS_HOME + " property is missing.");

        // model name is optional
        if (modelName == null || modelName.length() == 0)
            modelName = WdkModelTestBase.SAMPLE_MODEL;
    }

    /**
     * test parsing a valid config file
     * 
     * @throws IOException
     * @throws SAXException
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testParseConfig()
            throws SAXException, IOException, WdkModelException {
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        ModelConfig config = parser.parseConfig(modelName);
        Assert.assertNotNull(config);
    }

    /**
     * parse an invalid configuration file
     * 
     * @throws IOException
     * @throws SAXException
     * @throws WdkModelException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseInvalidConfig()
            throws SAXException, IOException, WdkModelException {
        String modelName = "sampleModel_bad_config_syntax";
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        parser.parseConfig(modelName);
    }
}
