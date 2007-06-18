/**
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.junit.Assert;

/**
 * @author Jerric
 * this test suite covers the test on ModelXmlParser.
 * - test validation
 * 	- test single valid model
 *      - test multi-part valid model
 *      - test single invalid model
 *      - test mutli-part invalid model
 * - test substitution
 * 	- test property substitution
 * 		- test complete prop list
 * 		- test incomplete prop list
 *      - test import substitution
 *      	- test successful import
 *      	- test missing import file
 * - test model construction
 *      - test parse single model
 *      - test parse multi-part model
 */
public class ModelXmlParserTest {

    private URL modelXmlUrl;
    private URL modelPropUrl;
    private URL modelConfigUrl;
    private URL schemaUrl;
    private URL xmlSchemaUrl;

    @org.junit.Before
    public void getInput() throws MalformedURLException {
        // get input from the system environment
        String modelName = System.getProperty(ModelXmlParser.PROP_MODEL_NAME);
        String configDirName = System.getProperty(ModelXmlParser.PROP_CONFIG_DIR);
        String schemaName = System.getProperty(ModelXmlParser.PROP_SCHEMA_FILE);
        String xmlSchemaName = System.getProperty(ModelXmlParser.PROP_XML_SCHEMA_FILE);

        // construct corresponding files. The validation of those files should be covered by environment test
        File configDir = new File(configDirName);

        File modelXmlFile = new File(configDir, modelName + ".xml");
        File modelPropFile = new File(configDir, modelName + ".prop");
        File modelConfigFile = new File(configDir, modelName + "-config.xml");

        File schemaFile = new File(schemaName);
        File xmlSchemaFile = new File(xmlSchemaName);

        // get the urls
        modelXmlUrl = modelXmlFile.toURI().toURL();
        modelPropUrl = modelPropFile.toURI().toURL();
        modelConfigUrl = modelConfigFile.toURI().toURL();
        schemaUrl = schemaFile.toURI().toURL();
        xmlSchemaUrl = xmlSchemaFile.toURI().toURL();
    }

    /**
     * a test to parse a model file, and construct the model object
     * @throws WdkModelException 
     * @throws MalformedURLException 
     */
    @org.junit.Test
    public void testParseModel()
            throws WdkModelException, MalformedURLException {

        WdkModel wdkModel = ModelXmlParser.parseXmlFile(modelXmlUrl,
                modelPropUrl, schemaUrl, xmlSchemaUrl, modelConfigUrl);
        Assert.assertNotNull(wdkModel);
    }
}
