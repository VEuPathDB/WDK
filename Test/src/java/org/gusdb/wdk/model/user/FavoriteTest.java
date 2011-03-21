/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    static final int OPEARTION_SIZE = 5;

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
        for (RecordClass rc : recordClasses) {
            if (rc.getFavoriteNoteField() != null) {
                recordClass = rc;
                break;
            }
        }

        user.clearFavorite();
        List<Map<String, Object>> added = addSomeRecords(user, recordClass);

        String rcName = recordClass.getFullName();
        Assert.assertEquals(added.size(), user.getFavoriteCount());
        List<Favorite> favorites = user.getFavorites().get(recordClass);
        Assert.assertEquals(added.size(), favorites.size());
        for (Favorite favorite : favorites) {
            RecordClass actual = favorite.getRecordInstance().getRecordClass();
            Assert.assertEquals(rcName, actual.getFullName());
        }
    }

    @Test
    public void testRemoveFromFavorite() throws Exception {
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);
        User user = UnitTestHelper.getRegisteredUser();

        List<Map<String, Object>> added = addSomeRecords(user, recordClass);
        // add more records, but those are not deleted
        List<Map<String, Object>> more = addSomeRecords(user, recordClass);
        user.removeFromFavorite(recordClass, added);

        Map<RecordClass, List<Favorite>> favorites = user.getFavorites();
        Assert.assertTrue(favorites.containsKey(recordClass));
        List<Favorite> list = favorites.get(recordClass);
        for (Favorite favorite : list) {
            RecordInstance instance = favorite.getRecordInstance();
            Map<String, String> values = instance.getPrimaryKey().getValues();

            for (Map<String, Object> add : more) {
                Assert.assertEquals(add.size(), values.size());
                for (String column : add.keySet()) {
                    String expected = add.get(column).toString();
                    Assert.assertEquals(expected, values.get(column));
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
        List<Map<String, Object>> added1 = addSomeRecords(user, recordClass);

        // now add some records of other record types
        while (true) {
            int idx = UnitTestHelper.getRandom().nextInt(recordClasses.size());
            if (idx != index) {
                index = idx;
                break;
            }
        }
        recordClass = recordClasses.get(index);
        List<Map<String, Object>> added2 = addSomeRecords(user, recordClass);

        int expected = added1.size() + added2.size();
        Assert.assertEquals(expected, user.getFavoriteCount());
    }

    @Test
    public void testSetNote() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);

        user.clearFavorite();

        List<Map<String, Object>> added = addSomeRecords(user, recordClass);
        Assert.assertEquals(added.size(), user.getFavoriteCount());

        Random random = UnitTestHelper.getRandom();
        String note = "note " + random.nextInt();
        user.setFavoriteNotes(recordClass, added, note);

        List<Favorite> favorites = user.getFavorites().get(recordClass);
        for (Favorite favorite : favorites) {
            Assert.assertEquals(note, favorite.getNote());
        }
    }

    @Test
    public void testSetGroup() throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        int index = UnitTestHelper.getRandom().nextInt(recordClasses.size());
        RecordClass recordClass = recordClasses.get(index);

        user.clearFavorite();

        List<Map<String, Object>> added = addSomeRecords(user, recordClass);
        Assert.assertEquals(added.size(), user.getFavoriteCount());

        Random random = UnitTestHelper.getRandom();
        String group = "group " + random.nextInt();
        user.setFavoriteGroups(recordClass, added, group);

        List<Favorite> favorites = user.getFavorites().get(recordClass);
        for (Favorite favorite : favorites) {
            Assert.assertEquals(group, favorite.getGroup());
        }
    }

    private List<Map<String, Object>> addSomeRecords(User user,
            RecordClass recordClass) throws Exception {
        // get a list of record ids
        List<Map<String, Object>> ids = getIds(recordClass, POOL_SIZE);
        // randomly pick up 5 ids from the list, and add them into basket
        Map<Integer, Map<String, Object>> selected = new HashMap<Integer, Map<String, Object>>();
        Random random = UnitTestHelper.getRandom();
        int count = 0;
        while (count < OPEARTION_SIZE) {
            int i = random.nextInt(ids.size());
            if (selected.containsKey(i))
                continue;
            selected.put(i, ids.get(i));
            count++;
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
                selected.values());
        user.addToFavorite(recordClass, list);
        return list;
    }

    static List<Map<String, Object>> getIds(RecordClass recordClass, int limit)
            throws Exception {
        User user = UnitTestHelper.getRegisteredUser();
        List<Map<String, Object>> ids = new ArrayList<Map<String, Object>>();
        Query query = recordClass.getAllRecordsQuery();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField()
                .getColumnRefs();
        int count = 0;
        if (query != null) {
            Map<String, String> params = new HashMap<String, String>();
            QueryInstance instance = query.makeInstance(user, params, true, 0,
                    new LinkedHashMap<String, String>());
            ResultList results = instance.getResults();
            while (results.next()) {
                Map<String, Object> values = new HashMap<String, Object>();
                for (int i = 0; i < pkColumns.length; i++) {
                    String value = results.get(pkColumns[i]).toString();
                    values.put(pkColumns[i], value);
                }
                ids.add(values);
                count++;
                if (count >= limit)
                    break;
            }
            results.close();
        } else {
            for (; count < limit; count++) {
                Map<String, Object> values = new HashMap<String, Object>();
                for (int i = 0; i < pkColumns.length; i++) {
                    String value = UnitTestHelper.getRandom().nextInt() + "";
                    values.put(pkColumns[i], value);
                }
                ids.add(values);
            }
        }
        return ids;
    }
}
