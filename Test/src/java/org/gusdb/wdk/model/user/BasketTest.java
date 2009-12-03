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
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
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

        for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass rc : rcSet.getRecordClasses()) {
                if (rc.hasBasket()) {
                    this.recordClass = rc;
                    break;
                }
            }
            if (this.recordClass != null) break;
        }
    }

    @Test
    public void testAddToBasket() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        addSomeRecords(user);
    }

    @Test
    public void testRemoveFromBasket() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        List<Map<String, String>> added = addSomeRecords(user);
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
        AnswerValue answerValue = question.makeAnswerValue(user, params);
        Assert.assertTrue("answer size >= 5", answerValue.getResultSize() >= 5);
        // check each records
        for (RecordInstance instance : answerValue.getRecordInstances()) {
            String pkValue = (String) instance.getPrimaryKey().getValue();
            AttributeValue attribute = instance.getAttributeValue(BasketFactory.BASKET_ATTRIBUTE);
            int value = Integer.parseInt(attribute.getValue().toString());
            Assert.assertEquals("record basket: " + pkValue, 1, value);
        }
    }

    private List<Map<String, String>> addSomeRecords(User user)
            throws Exception {
        // get a list of record ids
        List<Map<String, String>> ids = getIds(recordClass, POOL_SIZE);
        // randomly pick up 5 ids from the list, and add them into basket
        List<Map<String, String>> selected = new ArrayList<Map<String, String>>();
        Random random = UnitTestHelper.getRandom();
        int i = Math.max(0, random.nextInt(ids.size() - OPEARTION_SIZE + 1));
        for (; i < OPEARTION_SIZE; i++) {
            selected.add(ids.get(i));
        }
        basketFactory.addToBasket(user, recordClass, selected);
        return selected;
    }

    private List<Map<String, String>> getIds(RecordClass recordClass, int limit)
            throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        List<Map<String, String>> ids = new ArrayList<Map<String, String>>();
        Query query = recordClass.getAllRecordsQuery();
        Map<String, String> params = new HashMap<String, String>();
        QueryInstance instance = query.makeInstance(user, params, true);
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        ResultList results = instance.getResults();
        int count = 0;
        while (results.next()) {
            Map<String, String> values = new HashMap<String, String>();
            for (String column : pkColumns) {
                String value = results.get(column).toString();
                values.put(column, value);
            }
            ids.add(values);
            count++;
            if (count >= limit) break;
        }
        results.close();
        return ids;
    }
}
