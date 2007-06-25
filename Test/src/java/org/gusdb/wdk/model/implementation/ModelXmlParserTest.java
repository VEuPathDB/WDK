/**
 * 
 */
package org.gusdb.wdk.model.implementation;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
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

    /**
     * the correct, multi-part model
     */
    public static final String SAMPLE_MODEL = "sampleModel";

    /**
     * The RNG validation fails on master model
     */
    public static final String SAMPLE_MODEL_BAD_SYNTAX = "sampleModel_bad_syntax";

    /**
     * the sub model included in the <import> is missing
     */
    public static final String SAMPLE_MODEL_BAD_IMPORT = "sampleModel_bad_import";

    /**
     * the RNG validation of sub-model is failed
     */
    public static final String SAMPLE_MODEL_BAD_SUB = "sampleModel_bad_sub";

    /**
     * the sub-model has an invalid reference to other object
     */
    public static final String SAMPLE_MODEL_BAD_REF = "sampleModel_bad_ref";

    private static final Logger logger = Logger.getLogger(ModelXmlParserTest.class);

    private String modelName;
    private String gusHome;

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
            modelName = SAMPLE_MODEL;
    }

    /**
     * a test to parse a model file, and construct the model object
     * @throws WdkModelException 
     * @throws MalformedURLException 
     */
    @org.junit.Test
    public void testParseModel()
            throws WdkModelException, MalformedURLException {
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        WdkModel wdkModel = parser.parseModel(modelName);
        Assert.assertNotNull(wdkModel);

        // validate the references to questions
        QuestionSet[] questionSets = wdkModel.getAllQuestionSets();
        Assert.assertTrue("there must be at least one question set",
                questionSets.length > 0);
        for (QuestionSet questionSet : questionSets) {
            Question[] questions = questionSet.getQuestions();
            Assert.assertTrue("There must be at leasr one question in each "
                    + "question set", questions.length > 0);
            for (Question question : questions) {
                // the question must have reference to record class
                Assert.assertNotNull("The question must have reference to an "
                        + "record class", question.getRecordClass());
            }
        }

        // validate the references to the queries
        QuerySet[] querySets = wdkModel.getAllQuerySets();
        Assert.assertTrue("There must be at least one query set",
                querySets.length > 0);
        for (QuerySet querySet : querySets) {
            Query[] queries = querySet.getQueries();
            Assert.assertTrue("There must be at least one query in each query "
                    + "set", queries.length > 0);
            for (Query query : queries) {
                // the query must have at least one column
                Assert.assertTrue("The query must define at least one column",
                        query.getColumns().length > 0);
            }
        }

        // validate the references to the record classes
        RecordClassSet[] rcSets = wdkModel.getAllRecordClassSets();
        Assert.assertTrue("There must be at least one record class set",
                rcSets.length > 0);
        for (RecordClassSet rcSet : rcSets) {
            RecordClass[] recordClasses = rcSet.getRecordClasses();
            Assert.assertTrue("There must be at least one Record class in "
                    + "each query set", recordClasses.length > 0);
            for (RecordClass recordClass : recordClasses) {
                // the record class must have at least one attribute
                Assert.assertTrue(
                        "The record class must define at least one field",
                        recordClass.getFields().length > 0);
            }
        }
    }

    /**
     * test parsing an invalid model file. The RNG syntax should fail on the 
     * master model.
     * @throws WdkModelException 
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadSyntax() throws WdkModelException {
        String modelName = SAMPLE_MODEL_BAD_SYNTAX;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        try {
            parser.parseModel(modelName);
        } catch (WdkModelException ex) {
            logger.info("Expected: master model parsing failed - "
                    + ex.getMessage());
            throw ex;
        }
    }

    /**
     * test parsing a invalid model file. The <import> is pointing to a 
     * non-existing file.
     * @throws WdkModelException 
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadImport() throws WdkModelException {
        String modelName = SAMPLE_MODEL_BAD_IMPORT;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        try {
            parser.parseModel(modelName);
        } catch (WdkModelException ex) {
            logger.info("Expected: sub-model file missing - " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * test parsing an invalid model file. The sub model failed on RNG validation
     * @throws WdkModelException 
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadSub() throws WdkModelException {
        String modelName = SAMPLE_MODEL_BAD_SUB;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        try {
            parser.parseModel(modelName);
        } catch (WdkModelException ex) {
            logger.info("Expected: sub-model parsing failed - "
                    + ex.getMessage());
            throw ex;
        }
    }

    /**
     * test parsing an invalid model file. The sub model has an invalid reference
     * to another object.
     * @throws WdkModelException 
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testParseModelBadRef() throws WdkModelException {
        String modelName = SAMPLE_MODEL_BAD_REF;
        ModelXmlParser parser = new ModelXmlParser(gusHome);
        try {
            parser.parseModel(modelName);
        } catch (WdkModelException ex) {
            logger.info("Expected: sub-model has invalid reference - "
                    + ex.getMessage());
            throw ex;
        }
    }
}
