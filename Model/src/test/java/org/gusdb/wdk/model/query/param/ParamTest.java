/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.json.JSONException;
import org.junit.Assert;
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
    public void testClone() throws JSONException {
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
            Assert.assertEquals(param.getXmlDefault(), clone.getXmlDefault());
            Assert.assertEquals(param.getEmptyValue(),
                    clone.getEmptyValue());
            Assert.assertEquals(param.getFullName(), clone.getFullName());
            Assert.assertEquals(param.getGroup(), clone.getGroup());
            Assert.assertEquals(param.getHelp(), clone.getHelp());
            Assert.assertEquals(param.getId(), clone.getId());
            Assert.assertEquals(param.getChecksumJSON(true).toString(),
                    clone.getChecksumJSON(true).toString());
            Assert.assertEquals(param.getName(), clone.getName());
            Assert.assertEquals(param.getPrompt(), clone.getPrompt());
            Assert.assertEquals(param.isAllowEmpty(), clone.isAllowEmpty());
            Assert.assertEquals(param.isReadonly(), clone.isReadonly());
            Assert.assertEquals(param.isResolved(), clone.isResolved());
            Assert.assertEquals(param.toString(), clone.toString());
        }
    }

    @Test
    public void testReplaceSql() {
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                String defaultValue = param.getXmlDefault();

                // skip the param if it doesn't have a default value
                if (defaultValue == null) continue;

                String key = "$$" + param.getName() + "$$";
                String sql = "SELECT nothing FROM " + key + " WHERE 1 = 2";

                sql = param.replaceSql(sql, defaultValue);

                Assert.assertFalse("key replaced", sql.contains(key));
            }
        }
    }
}
