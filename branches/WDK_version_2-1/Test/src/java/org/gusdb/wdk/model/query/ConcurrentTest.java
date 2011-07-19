/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
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

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            System.out.println("start test thread: #" + id);
            try {
                QueryInstance instance = query.makeInstance(user, values, true,
                        0, new LinkedHashMap<String, String>());
                ResultList resultList = instance.getResults();
                resultList.next();
                resultList.close();
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
        ParamValuesSet paramValues = query.getParamValuesSets().get(0);
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
