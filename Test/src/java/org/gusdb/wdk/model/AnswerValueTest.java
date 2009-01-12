/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
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
        AnswerValue answer = step.getAnswer().getAnswerValue();

        Map<String, AttributeField> displayFields = answer.getDisplayableAttributeMap();
        Map<String, AttributeField> summaryFields = answer.getSummaryAttributeFieldMap();

        // no display fields should appear in summary fields
        for (AttributeField field : displayFields.values()) {
            Assert.assertFalse(summaryFields.containsKey(field.getName()));
        }

        // no summary fields should appear in display fields
        for (AttributeField field : summaryFields.values()) {
            Assert.assertFalse(displayFields.containsKey(field.getName()));
        }
    }

    @Test
    public void testAddSummaryAttibute() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answer = step.getAnswer().getAnswerValue();

        Map<String, AttributeField> displayFields = answer.getDisplayableAttributeMap();
        Map<String, AttributeField> summaryFields = answer.getSummaryAttributeFieldMap();
        
        AttributeField field = displayFields.values().iterator().next();
        
        // prepare the summary list
        List<String> list = new ArrayList<String>();
        for (AttributeField f : summaryFields.values()) {
            list.add(f.getName());
        }
        list.add(field.getName());
        String[] summaryList = new String[list.size()];
        list.toArray(summaryList);
        
        answer.setSumaryAttributes(summaryList);
        
        displayFields = answer.getDisplayableAttributeMap();
        summaryFields = answer.getSummaryAttributeFieldMap();
        
        Assert.assertFalse(displayFields.containsKey(field.getName()));
        for(String name : summaryList) {
            Assert.assertTrue(summaryFields.containsKey(name));
        }
    }
}
