/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class AnswerValueTest {

    private User user;

    public AnswerValueTest() throws Exception {
        this.user = UnitTestHelper.getRegisteredUser();
    }

    @Test
    public void testGetSummaryAttributes() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answer = step.getAnswerValue();

        Map<String, AttributeField> displayFields = answer
                .getDisplayableAttributeMap();
        Map<String, AttributeField> summaryFields = answer
                .getSummaryAttributeFieldMap();

        // no display fields should appear in summary fields
        Assert.assertTrue(
                "display attributes is fewer than summary attributes",
                displayFields.size() > summaryFields.size());

        // summary fields should be included in displayable fields
        for (String name : summaryFields.keySet()) {
            Assert.assertTrue("Summary atribute [" + name
                    + "] doesn't exist in displayable attributes.",
                    displayFields.containsKey(name));
        }
    }

    @Test
    public void testAddSummaryAttibute() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step.getAnswerValue();

        Map<String, AttributeField> displayFields = answerValue
                .getDisplayableAttributeMap();
        Map<String, AttributeField> summaryFields = answerValue
                .getSummaryAttributeFieldMap();

        for (AttributeField field : displayFields.values()) {
            // skip the fields that are already in the summary
            if (summaryFields.containsKey(field.getName()))
                continue;

            // prepare the summary list
            List<String> list = new ArrayList<String>();
            for (AttributeField f : summaryFields.values()) {
                list.add(f.getName());
            }
            list.add(field.getName());
            String[] summaryList = new String[list.size()];
            list.toArray(summaryList);

            summaryFields = answerValue.getSummaryAttributeFieldMap();

            for (String name : summaryList) {
                Assert.assertTrue(summaryFields.containsKey(name));
            }
        }
    }

    @Test
    public void testDeleteSummaryAttibute() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step.getAnswerValue();

        Map<String, AttributeField> displayFields = answerValue
                .getDisplayableAttributeMap();
        Map<String, AttributeField> summaryFields = answerValue
                .getSummaryAttributeFieldMap();

        // skip the primary key field
        Iterator<AttributeField> it = summaryFields.values().iterator();
        it.next();
        AttributeField field = it.next();

        // prepare the summary list
        List<String> list = new ArrayList<String>();
        for (AttributeField f : summaryFields.values()) {
            if (f.equals(field))
                continue;
            list.add(f.getName());
        }
        String[] summaryList = new String[list.size()];
        list.toArray(summaryList);

        displayFields = answerValue.getDisplayableAttributeMap();
        summaryFields = answerValue.getSummaryAttributeFieldMap();

        Assert.assertTrue(displayFields.containsKey(field.getName()));
        Assert.assertFalse(summaryFields.containsKey(field.getName()));
        for (String name : summaryList) {
            Assert.assertTrue(summaryFields.containsKey(name));
        }
    }

    @Test
    public void testGetFilterSizes() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step.getAnswerValue();
        AnswerFilterInstance currentFilter = answerValue.getFilter();
        int size = answerValue.getResultSize();

        AnswerFilterInstance[] filters = answerValue.getQuestion()
                .getRecordClass().getFilters();
        for (AnswerFilterInstance filter : filters) {
            int filterSize = answerValue.getFilterSize(filter.getName());
            if (filter.equals(currentFilter)) {
                Assert.assertEquals(size, filterSize);
            } else {
                Assert.assertTrue(filterSize >= 0);
            }
        }
    }
}
