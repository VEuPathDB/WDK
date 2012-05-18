/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.json.JSONException;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class ParamTest {

    private WdkModel wdkModel;

    public ParamTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();
    }

    @Test
    public void testClone() throws NoSuchAlgorithmException, WdkModelException,
            SQLException, JSONException, WdkUserException {
        List<Param> params = new ArrayList<Param>();
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                params.add(param);
            }
        }
        Random random = UnitTestHelper.getRandom();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(params.size());
            Param param = params.get(index);
            Param clone = param.clone();

            // make sure everything is identical
            Assert.assertEquals(param.getDefault(), clone.getDefault());
            Assert.assertEquals(param.getEmptyValue(),
                    clone.getEmptyValue());
            Assert.assertEquals(param.getFullName(), clone.getFullName());
            Assert.assertEquals(param.getGroup(), clone.getGroup());
            Assert.assertEquals(param.getHelp(), clone.getHelp());
            Assert.assertEquals(param.getId(), clone.getId());
            Assert.assertEquals(param.getJSONContent(true).toString(),
                    clone.getJSONContent(true).toString());
            Assert.assertEquals(param.getName(), clone.getName());
            Assert.assertEquals(param.getPrompt(), clone.getPrompt());
            Assert.assertEquals(param.isAllowEmpty(), clone.isAllowEmpty());
            Assert.assertEquals(param.isReadonly(), clone.isReadonly());
            Assert.assertEquals(param.isResolved(), clone.isResolved());
            Assert.assertEquals(param.toString(), clone.toString());
        }
    }

    @Test
    public void testReplaceSql() throws NoSuchAlgorithmException, SQLException,
            WdkModelException, JSONException, WdkUserException {
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                String defaultValue = param.getDefault();

                // skip the param if it doesn't have a default value
                if (defaultValue == null) continue;

                String key = "$$" + param.getName() + "$$";
                String sql = "SELECT nothing FROM " + key + " WHERE 1 = 2";

                sql = param.replaceSql(sql, defaultValue);

                Assert.assertFalse("key replaced", sql.contains(key));
            }
        }
    }

    @Test
    public void testCompress() throws NoSuchAlgorithmException,
            WdkModelException, WdkUserException {
        // generate random input
        StringBuffer buffer = new StringBuffer();
        Random rand = UnitTestHelper.getRandom();
        for (int i = 0; i < 4000; i++) {
            int code = rand.nextInt(36);
            int base = (code < 0) ? '0' : 'a';
            int ch = (char) (code + base);
            buffer.append(ch);
        }
        String origin = buffer.toString();

        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                // compress
                String compressed = param.compressValue(origin);
                Assert.assertTrue("compress",
                        compressed.length() < origin.length());

                // compress again, should be identical
                String compressed2 = param.compressValue(origin);
                Assert.assertEquals("compress again", compressed, compressed2);

                // decompress
                String decompressed = param.decompressValue(compressed);
                Assert.assertEquals("decompress", origin, decompressed);

                // only needs to test once
                return;
            }
        }

    }
}
