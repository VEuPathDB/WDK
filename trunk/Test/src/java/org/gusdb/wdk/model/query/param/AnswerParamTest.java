/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class AnswerParamTest {

    private User user;

    /**
     * @throws Exception
     * 
     */
    public AnswerParamTest() throws Exception {
        user = UnitTestHelper.getRegisteredUser();
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
                AnswerValue answerValue = answerParam.getAnswerValue(user, paramValue);

                Assert.assertTrue("input size",
                        answerValue.getResultSize() >= 0);
            }
        }
    }

    @Test
    public void testUseAnswerParam() throws Exception {
        Question question = UnitTestHelper.getAnswerParamQuestion();

        Map<String, String> paramValues = new LinkedHashMap<String, String>();
        for (Param param : question.getParams()) {
            String paramValue;
            if (param instanceof AnswerParam) {
                Step step = UnitTestHelper.createNormalStep(user);
                paramValue = Integer.toString(step.getDisplayId());
            } else paramValue = param.getDefault();
            paramValues.put(param.getName(), paramValue);
        }
        AnswerValue answerValue = question.makeAnswerValue(user, paramValues);

        Assert.assertTrue("result size", answerValue.getResultSize() >= 0);
    }
}
