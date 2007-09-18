/**
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelTestBase;
import org.junit.Assert;
import org.xml.sax.SAXException;

/**
 * @author Jerric this test suite covers the test on ModelXmlParser. - test
 * validation - test single valid model - test multi-part valid model - test
 * single invalid model - test mutli-part invalid model - test substitution -
 * test property substitution - test complete prop list - test incomplete prop
 * list - test import substitution - test successful import - test missing
 * import file - test model construction - test parse single model - test parse
 * multi-part model
 */
public class ModelXmlParserTest extends WdkModelTestBase {

    /**
     * The RNG validation fails on master model
     */
    public static final String SAMPLE_DB_BAD_SYNTAX = "SampleDB_bad_syntax";

    /**
     * the sub model included in the <import> is missing
     */
    public static final String SAMPLE_DB_BAD_IMPORT = "SampleDB_bad_import";

    /**
     * the RNG validation of sub-model is failed
     */
    public static final String SAMPLE_DB_BAD_SUB = "SampleDB_bad_sub";

    /**
     * the sub-model has an invalid reference to other object
     */
    public static final String SAMPLE_DB_BAD_REF = "SampleDB_bad_ref";

    // private static final Logger logger =
    // Logger.getLogger(ModelXmlParserTest.class);

    /**
     * a test to parse a model file, and construct the model object
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.Test
    public void testParseModel()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        WdkModel wdkModel = parser.parseModel(projectId);
        Assert.assertNotNull(wdkModel);
    }

    /**
     * test parsing an invalid model file. The RNG syntax should fail on the
     * master model.
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadSyntax()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        String modelName = SAMPLE_DB_BAD_SYNTAX;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        parser.parseModel(modelName);
    }

    /**
     * test parsing a invalid model file. The <import> is pointing to a
     * non-existing file.
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.Test(expected = IOException.class)
    public void testParseModelBadImport()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        String modelName = SAMPLE_DB_BAD_IMPORT;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        parser.parseModel(modelName);
    }

    /**
     * test parsing an invalid model file. The sub model failed on RNG
     * validation
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadSub()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        String modelName = SAMPLE_DB_BAD_SUB;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        parser.parseModel(modelName);
    }

    /**
     * test parsing an invalid model file. The sub model has an invalid
     * reference to another object.
     * 
     * @throws WdkModelException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws ParserConfigurationException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadRef()
            throws WdkModelException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException {
        String modelName = SAMPLE_DB_BAD_REF;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        parser.parseModel(modelName);
    }
}
