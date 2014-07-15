/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class XmlQuestionTest {

    private WdkModel wdkModel;

    public XmlQuestionTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();
    }

    /**
     * test reading questions from the model
     */
    @Test
    public void testGetAllXmlQuestions() {
        // validate the references to questions
        for (XmlQuestionSet questionSet : wdkModel.getXmlQuestionSets()) {
            String setName = questionSet.getName();
            Assert.assertTrue("set name", setName.trim().length() > 0);

            XmlQuestion[] questions = questionSet.getQuestions();
            Assert.assertTrue("question count > 0", questions.length > 0);
            for (XmlQuestion question : questions) {
                String qName = question.getName();
                Assert.assertTrue("name", qName.trim().length() > 0);

                // the question must have reference to record class
                Assert.assertNotNull("record class", question.getRecordClass());
                Assert.assertNotNull("display name", question.getDisplayName());
                Assert.assertEquals("question set", setName,
                        question.getQuestionSet().getName());
                String fullName = question.getFullName();
                Assert.assertTrue("fullName starts with",
                        fullName.startsWith(setName));
                Assert.assertTrue("fullName ends with",
                        fullName.endsWith(qName));
                Assert.assertNotNull("data url", question.getXmlDataURL());
            }
        }
    }

    /**
     * The question set belongs to toy db, not sample db; although it is defined
     * in the same model
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidXmlQuestionSet() throws WdkModelException {
        String qsetName = "NonexistXmlQuestions";
        wdkModel.getXmlQuestionSet(qsetName);
    }

    /**
     * get a known question, and verify its description
     */
    @org.junit.Test
    public void testGetXmlQuestionSet() throws WdkModelException {
        for (XmlQuestionSet questionSet : wdkModel.getXmlQuestionSets()) {
            String qsetName = questionSet.getName();
            XmlQuestionSet qset = wdkModel.getXmlQuestionSet(qsetName);
            Assert.assertEquals(qsetName, qset.getName());
        }
    }

    /**
     * Test getting question and its properties
     */
    @org.junit.Test
    public void testGetXmlQuestion() throws WdkModelException {
        for (XmlQuestionSet questionSet : wdkModel.getXmlQuestionSets()) {
            for (XmlQuestion question : questionSet.getQuestions()) {
                String name = question.getName();
                XmlQuestion q = questionSet.getQuestion(name);
                Assert.assertEquals("by name", name, q.getName());

                String fullName = question.getFullName();
                q = (XmlQuestion) wdkModel.resolveReference(fullName);
                Assert.assertEquals("by fullName", fullName, q.getFullName());
            }
        }
    }

    /**
     * the question is excluded from the Sample DB
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidXmlQuestion() throws WdkModelException {
        String qName = "NonexistXmlQuestion";
        for (XmlQuestionSet questionSet : wdkModel.getXmlQuestionSets()) {
            questionSet.getQuestion(qName);
        }
    }

    /**
     * the question is excluded from the Sample DB
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidXmlQuestionByFull() throws WdkModelException {
        String fullName = "NonexistXmlQuestionSet.NonexistXmlQuestion";
        wdkModel.resolveReference(fullName);
    }

}
