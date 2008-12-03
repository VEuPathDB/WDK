/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

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
        for (ParamSet paramSet : wdkModel.getAllParamSets()) {
            for (Param param : paramSet.getParams()) {
                Param clone = param.clone();

                // make sure everything is identical
                Assert.assertEquals(param.getDefault(), clone.getDefault());
                Assert.assertEquals(param.getEmptyValue(),
                        clone.getEmptyValue());
                Assert.assertEquals(param.getFullName(), clone.getFullName());
                Assert.assertEquals(param.getGroup(), clone.getGroup());
                Assert.assertEquals(param.getHelp(), clone.getHelp());
                Assert.assertEquals(param.getId(), clone.getId());
                Assert.assertEquals(param.getJSONContent().toString(),
                        clone.getJSONContent().toString());
                Assert.assertEquals(param.getName(), clone.getName());
                Assert.assertEquals(param.getPrompt(), clone.getPrompt());
                Assert.assertEquals(param.isAllowEmpty(), clone.isAllowEmpty());
                Assert.assertEquals(param.isReadonly(), clone.isReadonly());
                Assert.assertEquals(param.isResolved(), clone.isResolved());
                Assert.assertEquals(param.toString(), clone.toString());
            }
        }
    }
    
    @Test
    public void testDatasetParamGetId() {
        
    }
}
