package org.gusdb.wdk.model.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.test.ParamValuesFactory;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class ConcurrentTest {

    private class TestThread extends Thread {

        private int id;
        private User user;
        private Query query;
        private Map<String, String> values;

        TestThread(int id, Query query, Map<String, String> values)
                throws Exception {
            this.id = id;
            this.query = query;
            this.values = values;
            this.user = UnitTestHelper.getRegisteredUser();
        }

        @Override
        public void run() {
            System.out.println("start test thread: #" + id);
            try {
                QueryInstance<?> instance = Query.makeQueryInstance(
                    QueryInstanceSpec.builder().putAll(values)
                    .buildRunnable(user, query, StepContainer.emptyContainer()));
                try (ResultList resultList = instance.getResults()) {
                  resultList.next();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public ConcurrentTest() throws Exception {
        // try getting the model, it will reset the cache.
        UnitTestHelper.getModel();
    }

    @Test
    public void testConcurrentQuery() throws Exception {
        Query query = UnitTestHelper.getNormalQuestion().getQuery();
        ParamValuesSet paramValues = ParamValuesFactory.getParamValuesSets(
            UnitTestHelper.getModel().getSystemUser(), query).get(0);
        Map<String, String> values = paramValues.getParamValues();
        List<TestThread> threads = new ArrayList<TestThread>();
        for (int i = 1; i <= 3; i++) {
            threads.add(new TestThread(i, query, values));
        }
        for (TestThread thread : threads) {
            thread.start();
        }
    }

}
