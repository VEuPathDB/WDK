/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class PropertyListTest {

    private static WdkModel wdkModel;

    public PropertyListTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();
    }

    @Test
    public void testGetPropertyList() throws WdkModelException {
        Map<String, String[]> defaultPropertyList = wdkModel.getDefaultPropertyLists();

        for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
            for (Question question : questionSet.getQuestions()) {
                Map<String, String[]> propertyMap = question.getPropertyLists();
                for (String propName : propertyMap.keySet()) {
                    String[] properties = propertyMap.get(propName);
                    Assert.assertTrue("property list is empty: " + propName,
                            properties.length > 0);
                }

                // default properties should appear in the property list too
                for (String propName : defaultPropertyList.keySet()) {
                    Assert.assertTrue(
                            "default prop doesn't exist: " + propName,
                            propertyMap.containsKey(propName));
                }
            }
        }
    }
}
