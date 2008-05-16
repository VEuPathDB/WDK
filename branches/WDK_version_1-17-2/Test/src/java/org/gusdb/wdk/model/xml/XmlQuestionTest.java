/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class XmlQuestionTest extends WdkModelTestBase {

    public static final String SAMPLE_XML_QUESTION_SET = "XmlQuestions";
    public static final String SAMPLE_XML_QUESTION = "News";

    /**
     * test reading questions from the model
     */
    @Test
    public void testGetAllXmlQuestions() {
        // validate the references to questions
        XmlQuestionSet[] questionSets = wdkModel.getXmlQuestionSets();
        Assert.assertTrue("there must be at least one xml question set",
                questionSets.length > 0);
        for (XmlQuestionSet questionSet : questionSets) {
            XmlQuestion[] questions = questionSet.getQuestions();
            Assert.assertTrue("There must be at leasr one question in each "
                    + "question set", questions.length > 0);
            for (XmlQuestion question : questions) {
                // the question must have reference to record class
                Assert.assertNotNull("The question must have reference to an "
                        + "record class", question.getRecordClass());
            }
        }
    }

    /**
     * The question set belongs to toy db, not sample db; although it is defined
     * in the same model
     * 
     * @throws WdkModelException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidXmlQuestionSet() throws WdkModelException {
        String qsetName = "ToyXmlQuestions";
        wdkModel.getXmlQuestionSet(qsetName);
    }

    /**
     * get a known question, and verify its description
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetXmlQuestionSet() throws WdkModelException {
        XmlQuestionSet questionSet = wdkModel.getXmlQuestionSet(SAMPLE_XML_QUESTION_SET);
        Assert.assertNotNull("questionSet " + SAMPLE_XML_QUESTION_SET
                + " should exist", questionSet);
        String description = questionSet.getDescription().trim();
        Assert.assertEquals("Data contents from XML data sources", description);
    }

    /**
     * the question is excluded from the Sample DB
     * 
     * @throws WdkModelException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidXmlQuestion() throws WdkModelException {
        String qName = "ToyXmlQuestion";
        XmlQuestionSet questionSet = wdkModel.getXmlQuestionSet(SAMPLE_XML_QUESTION_SET);
        questionSet.getQuestion(qName);
    }

    /**
     * Test getting question and its properties
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetXmlQuestion() throws WdkModelException {
        // get question by question set
        XmlQuestionSet questionSet = wdkModel.getXmlQuestionSet(SAMPLE_XML_QUESTION_SET);
        XmlQuestion question1 = questionSet.getQuestion(SAMPLE_XML_QUESTION);

        // get question by question full name
        String fullName = SAMPLE_XML_QUESTION_SET + "." + SAMPLE_XML_QUESTION;
        XmlQuestion question2 = (XmlQuestion) wdkModel.resolveReference(fullName);
        Assert.assertSame(question1, question2);

        // check the description, summary, and help
        Assert.assertEquals("Retrieve news from XML data source",
                question2.getDescription());
        Assert.assertEquals("Sample Xml Question Help", question2.getHelp());
    }
}
