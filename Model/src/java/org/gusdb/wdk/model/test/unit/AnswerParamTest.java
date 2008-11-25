/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class AnswerParamTest {

    @Test
    public void testPrepareValue() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Step step = UnitTestHelper.createNormalStep(user);
        String externalValue = user.getSignature() + ":" + step.getDisplayId();
        String answerChecksum = step.getAnswer().getAnswerChecksum();
        AnswerFilterInstance filter = step.getAnswer().getAnswerValue().getFilter();
        if (filter != null) answerChecksum += ":" + filter.getName();

        Question question = UnitTestHelper.getAnswerParamQuestion();
        for (Param param : question.getParams()) {
            AnswerParam answerParam = (AnswerParam) param;
            String internalValue = answerParam.prepareValue(externalValue);
            Assert.assertEquals("user-independent value", answerChecksum,
                    internalValue);
        }
    }

    @Test
    public void testGetAnswerValue() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Step step = UnitTestHelper.createNormalStep(user);
        String paramValue = step.getAnswer().getAnswerChecksum();

        Question question = UnitTestHelper.getAnswerParamQuestion();
        for (Param param : question.getParams()) {
            if (param instanceof AnswerParam) {
                AnswerParam answerParam = (AnswerParam) param;
                AnswerValue answerValue = answerParam.getAnswerValue(paramValue);

                Assert.assertTrue("input size",
                        answerValue.getResultSize() >= 0);
            }
        }
    }

    @Test
    public void testUseAnswerParam() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Question question = UnitTestHelper.getAnswerParamQuestion();

        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        for (Param param : question.getParams()) {
            String paramValue;
            if (param instanceof AnswerParam) {
                Step step = UnitTestHelper.createNormalStep(user);
                paramValue = step.getAnswer().getAnswerChecksum();
            } else paramValue = param.getDefault();
            paramValues.put(param.getName(), paramValue);
        }
        AnswerValue answerValue = question.makeAnswerValue(paramValues);

        Assert.assertTrue("result size", answerValue.getResultSize() >= 0);
    }
}
