/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.UnitTestHelper;
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
public class FavoriteTest {

    private static final int POOL_SIZE = 10;
    private static final int OPEARTION_SIZE = 5;

    private WdkModel wdkModel;
    private List<RecordClass> recordClasses;

    public FavoriteTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();

        recordClasses = new ArrayList<RecordClass>();
        for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass rc : rcSet.getRecordClasses()) {
                recordClasses.add(rc);
            }
        }
    }

    @Test
    public void testAddToFavorite() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);

        List<String[]> added = addSomeRecords(user, recordClass);

        Assert.assertTrue(added.size() <= user.getFavoriteCount());
        Favorite favorite = user.getFavorites().get(recordClass.getFullName());
        Assert.assertTrue(added.size() <= favorite.getRecordInstances().length);
    }

    @Test
    public void testRemoveFromFavorite() throws Exception {
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);
        User user = UnitTestHelper.getRegisteredUser();

        List<String[]> added = addSomeRecords(user, recordClass);
        user.removeFromFavorite(recordClass, added);

        Map<String, Favorite> favorites = user.getFavorites();
        if (favorites.containsKey(recordClass.getFullName())) {
            Favorite favorite = favorites.get(recordClass.getFullName());
            for (RecordInstance instance : favorite.getRecordInstances()) {
                Map<String, String> values = instance.getPrimaryKey().getValues();
                String[] pkValues = new String[values.size()];
                values.values().toArray(pkValues);

                for (String[] add : added) {
                    boolean match = true;
                    for (int i = 0; i < pkValues.length; i++) {
                        if (!add[i].equals(pkValues[i])) {
                            match = false;
                            break;
                        }
                    }
                    Assert.assertFalse(match);
                }
            }
        }
    }

    @Test
    public void testClearFavorite() throws Exception {
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);
        User user = UnitTestHelper.getRegisteredUser();

        addSomeRecords(user, recordClass);

        user.clearFavorite();

        Assert.assertEquals(0, user.getFavoriteCount());
    }

    @Test
    public void testGetCounts() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        user.clearFavorite();

        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);
        List<String[]> added1 = addSomeRecords(user, recordClass);

        // now add some records of other record types
        while (true) {
            int idx = UnitTestHelper.getRandom().nextInt(recordClasses.size());
            if (idx != index) {
                index = idx;
                break;
            }
        }
        recordClass = recordClasses.get(index);
        List<String[]> added2 = addSomeRecords(user, recordClass);

        int expected = added1.size() + added2.size();
        Assert.assertEquals(expected, user.getFavoriteCount());
    }

    private List<String[]> addSomeRecords(User user, RecordClass recordClass)
            throws Exception {
        // get a list of record ids
        List<String[]> ids = getIds(recordClass, POOL_SIZE);
        // randomly pick up 5 ids from the list, and add them into basket
        List<String[]> selected = new ArrayList<String[]>();
        Random random = UnitTestHelper.getRandom();
        int i = Math.max(0, random.nextInt(ids.size() - OPEARTION_SIZE + 1));
        for (; i < OPEARTION_SIZE; i++) {
            selected.add(ids.get(i));
        }
        user.addToFavorite(recordClass, selected);
        return selected;
    }

    private List<String[]> getIds(RecordClass recordClass, int limit)
            throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        List<String[]> ids = new ArrayList<String[]>();
        Query query = recordClass.getAllRecordsQuery();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        int count = 0;
        if (query != null) {
            Map<String, String> params = new HashMap<String, String>();
            QueryInstance instance = query.makeInstance(user, params, true, 0);
            ResultList results = instance.getResults();
            while (results.next()) {
                String[] values = new String[pkColumns.length];
                for (int i = 0; i < pkColumns.length; i++) {
                    String value = results.get(pkColumns[i]).toString();
                    values[i] = value;
                }
                ids.add(values);
                count++;
                if (count >= limit) break;
            }
            results.close();
        } else {
            for (; count < limit; count++) {
                String[] values = new String[pkColumns.length];
                for (int i = 0; i < pkColumns.length; i++) {
                    values[i] = UnitTestHelper.getRandom().nextInt() + "";
                }
                ids.add(values);
            }
        }
        return ids;
    }
}
