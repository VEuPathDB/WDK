/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.wdk.model.query.Query;
import org.junit.Test;
import org.junit.Assert;

/**
 * @author Jerric
 * 
 */
public class QuestionTest extends WdkModelTestBase {

    public static final String SAMPLE_QUESTION_SET = "SampleQuestions";
    public static final String SAMPLE_QUESTION = "SampleQuestion";

    /**
     * test reading questions from the model
     */
    @org.junit.Test
    public void testGetAllQuestions() {
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
    }

    /**
     * The question set belongs to toy db, not sample db; although it is defined
     * in the same model
     * 
     * @throws WdkModelException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidQuestionSet() throws WdkModelException {
        String qsetName = "ToyQuestions";
        wdkModel.getQuestionSet(qsetName);
    }

    /**
     * get a known question, and verify its description
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetQuestionSet() throws WdkModelException {
        QuestionSet questionSet = wdkModel.getQuestionSet(SAMPLE_QUESTION_SET);
        Assert.assertNotNull("questionSet " + SAMPLE_QUESTION_SET
                + " should exist", questionSet);
        String description = questionSet.getDescription().trim();
        String expected = "Sample DB questions";
        Assert.assertEquals("The desc is supposed to be '" + expected
                + "', but it is '" + description + "'.", expected, description);
    }

    /**
     * the question is excluded from the Sample DB
     * 
     * @throws WdkModelException
     */
    @org.junit.Test(expected = WdkModelException.class)
    public void testGetInvalidQuestion() throws WdkModelException {
        String qName = "ToyQuestion";
        QuestionSet questionSet = wdkModel.getQuestionSet(SAMPLE_QUESTION_SET);
        questionSet.getQuestion(qName);
    }

    /**
     * Test getting question and its properties
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetQuestion() throws WdkModelException {
        // get question by question set
        QuestionSet questionSet = wdkModel.getQuestionSet(SAMPLE_QUESTION_SET);
        Question question1 = questionSet.getQuestion(SAMPLE_QUESTION);
        Assert.assertNotNull("question " + SAMPLE_QUESTION + " should exist",
                question1);

        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question2 = (Question) wdkModel.resolveReference(fullName);
        Assert.assertSame(question1, question2);

        // check the description, summary, and help
        Assert.assertNotNull("the description should exist",
                question2.getDescription());
        Assert.assertNotNull("the summary should exist", question2.getHelp());
        Assert.assertNotNull("the help should exist", question2.getSummary());
    }

    /**
     * test getting the summary and sorting attribute list
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetAttributeList() throws WdkModelException {
        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question = (Question) wdkModel.resolveReference(fullName);

        Map<String, AttributeField> summaryMap = question.getSummaryAttributeFieldMap();
        Assert.assertTrue("summary list is missing", summaryMap != null
                && summaryMap.size() > 0);

        Map<String, Boolean> sortList = question.getSortingAttributeMap();
        Assert.assertTrue("sorting list is missing", sortList != null
                && sortList.size() > 0);
    }

    /**
     * get the dynamic attribute set
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetDynamicAttributeSet() throws WdkModelException {
        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question = (Question) wdkModel.resolveReference(fullName);
        Assert.assertTrue("question should be dynamic", question.isDynamic());
        Map<String, AttributeField> attributes = question.getDynamicAttributeFields();
        Assert.assertTrue("the dynamic attribute Set should have attributes",
                attributes.size() > 0);
        for (AttributeField attribute : attributes.values()) {
            Assert.assertNotNull("attribute should not be null", attribute);
            // the attribute is supposed to be excluded
            String toyField = "end_time";
            Assert.assertFalse("The field " + toyField + " should be excluded",
                    toyField.equals(attribute.getName()));
        }

        // test text field
        TextAttributeField textField = (TextAttributeField) attributes.get("run_time");
        String text = textField.getText();
        Assert.assertTrue(text != null && text.length() > 0);

        // test link field
        LinkAttributeField linkField = (LinkAttributeField) attributes.get("url");
        String url = linkField.getUrl();
        Assert.assertTrue(url != null && url.length() > 0);
        Assert.assertFalse(url.indexOf("@") >= 0);
    }

    /**
     * test getting property lists from question
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetPropertyList() throws WdkModelException {
        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question = (Question) wdkModel.resolveReference(fullName);

        Map<String, String[]> propLists = question.getPropertyLists();
        Assert.assertTrue("question should have some property lists",
                propLists != null && propLists.size() > 0);
        for (String plName : propLists.keySet()) {
            Assert.assertNotNull("property list name should not be null",
                    plName);
            String[] values = propLists.get(plName);
            Assert.assertTrue("property list should have some values",
                    values.length > 0);
        }
    }

    /**
     * test getting default property lists from question
     * 
     * @throws WdkModelException
     */
    @org.junit.Test
    public void testGetDefaultPropertyList() throws WdkModelException {
        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question = (Question) wdkModel.resolveReference(fullName);

        String[] values = question.getPropertyList("species");
        Assert.assertTrue(
                "question should get the default property list from model",
                values != null && values.length > 0);
    }

    /**
     * test getting the recordClass and id query associated with the question
     * 
     * @throws WdkModelException
     */
    @Test
    public void testGetReferences() throws WdkModelException {
        // get question by question full name
        String fullName = SAMPLE_QUESTION_SET + "." + SAMPLE_QUESTION;
        Question question = (Question) wdkModel.resolveReference(fullName);

        // get record class
        RecordClass recordClass = question.getRecordClass();
        Assert.assertNotNull(recordClass);

        // get id query
        Query query = question.getQuery();
        Assert.assertNotNull(query);
    }
}
