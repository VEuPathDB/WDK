/**
 * 
 */
package org.gusdb.wdk.model.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Jerric
 * 
 */
public class SanityTest extends WdkModelTestBase {

    private SanityModel sanityModel;

    @Before
    public void loadSanityModel()
            throws SAXException, IOException, WdkModelException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException {
        // create sanity model parser
        SanityTestXmlParser parser = new SanityTestXmlParser(gusHome);
        sanityModel = parser.parseModel(modelName, wdkModel);
    }
    
    @Test
    public void testGetSanityModel() {
        SanityQuestion[] questions = sanityModel.getAllSanityQuestions();
        Assert.assertTrue(questions.length>0);
        for (SanityQuestion question : questions) {
            Assert.assertTrue(false);
        }
    }
    
    @Test
    public void testGenerateQuery() {
        // get a list of sanity questions
        
    }
}
