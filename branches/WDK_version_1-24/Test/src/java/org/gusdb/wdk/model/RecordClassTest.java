package org.gusdb.wdk.model;

import org.junit.Assert;
import org.junit.Test;

public class RecordClassTest {

    @Test
    public void testGetTransforms() throws Exception {
        WdkModel wdkModel = UnitTestHelper.getModel();
        for (RecordClassSet recordClassSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass recordClass : recordClassSet.getRecordClasses()) {
                Question[] questions = recordClass.getTransformQuestions(false);
                for (Question question : questions) {
                    Assert.assertTrue(question.getQuery().isTransform());
                }
            }
        }
    }
}
