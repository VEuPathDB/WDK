/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class BasketTest {

    private static final int POOL_SIZE = 5;
    private static final int OPEARTION_SIZE = 5;

    private WdkModel wdkModel;
    private BasketFactory basketFactory;
    private RecordClass recordClass;

    public BasketTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();
        basketFactory = wdkModel.getBasketFactory();
        recordClass = UnitTestHelper.getNormalQuestion().getRecordClass();
    }

    @Test
    public void testAddToBasket() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        addSomeRecords(user);
    }

    @Test
    public void testRemoveFromBasket() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        List<String[]> added = addSomeRecords(user);
        basketFactory.removeFromBasket(user, recordClass, added);
    }

    @Test
    public void testClearBasket() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        basketFactory.clearBasket(user, recordClass);
    }

    @Test
    public void testGetRealtimeBasketQuestion() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        addSomeRecords(user);
        Question question = recordClass.getRealtimeBasketQuestion();
        Map<String, String> params = new HashMap<String, String>();
        params.put(BasketFactory.PARAM_USER_SIGNATURE, user.getSignature());
        AnswerValue answerValue = question.makeAnswerValue(user, params, true, 0);
        Assert.assertTrue("answer size >= 5", answerValue.getResultSize() >= 5);
        // check each records
        for (RecordInstance instance : answerValue.getRecordInstances()) {
            String pkValue = (String) instance.getPrimaryKey().getValue();
            AttributeValue attribute = instance.getAttributeValue(BasketFactory.BASKET_ATTRIBUTE);
            int value = Integer.parseInt(attribute.getValue().toString());
            Assert.assertEquals("record basket: " + pkValue, 1, value);
        }
    }

    @Test
    public void testGetCounts() throws Exception {
        UserBean user = new UserBean(UnitTestHelper.getRegisteredUser());
        Map<RecordClassBean, Integer> beanCounts = user.getBasketCounts();
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (RecordClassBean rcBean : beanCounts.keySet()) {
        	String rcName = rcBean.getFullName();
        	counts.put(rcName, beanCounts.get(rcBean));
        }
        for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass rc : rcSet.getRecordClasses()) {
                if (!rc.isUseBasket()) continue;
                String rcName = rc.getFullName();
                Assert.assertTrue(counts.containsKey(rcName));
                Assert.assertTrue(counts.get(rcName) >= 0);
            }
        }
    }

    private List<String[]> addSomeRecords(User user) throws Exception {
        // get a list of record ids
        List<String[]> ids = getIds(POOL_SIZE);
        // randomly pick up 5 ids from the list, and add them into basket
        List<String[]> selected = new ArrayList<String[]>();
        Random random = UnitTestHelper.getRandom();
        int i = Math.max(0, random.nextInt(ids.size() - OPEARTION_SIZE + 1));
        for (; i < OPEARTION_SIZE; i++) {
            selected.add(ids.get(i));
        }
        basketFactory.addToBasket(user, recordClass, selected);
        return selected;
    }

    private List<String[]> getIds(int limit) throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        Step step = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step.getAnswerValue();
        RecordClass recordClass = step.getQuestion().getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        List<String[]> ids = new ArrayList<String[]>();

        int count = 0;
        for (RecordInstance instance : answerValue.getRecordInstances()) {
            Map<String, String> valueMap = instance.getPrimaryKey().getValues();
            String[] values = new String[pkColumns.length];
            for (int i = 0; i < pkColumns.length; i++) {
                String value = valueMap.get(pkColumns[i]);
                values[i] = value;
            }
            ids.add(values);
            count++;
            if (count >= limit) break;
        }
        return ids;
    }
}
