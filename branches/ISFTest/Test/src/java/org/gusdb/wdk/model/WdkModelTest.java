/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class WdkModelTest {

    private WdkModel wdkModel;

    public WdkModelTest() throws Exception {
        wdkModel = UnitTestHelper.getModel();
    }

    /**
     * get model name, display name, and version
     */
    @org.junit.Test
    public void testGetModelInfo() {
        String name = wdkModel.getProjectId();
        Assert.assertTrue("the model name is not set",
                name != null && name.length() > 0);

        String displayName = wdkModel.getDisplayName();
        Assert.assertTrue("the model display name is not set",
                displayName != null && displayName.length() > 0);

        String version = wdkModel.getVersion();
        Assert.assertTrue("the model version is not set", version != null
                && version.length() > 0);
    }

    /**
     * test getting default property lists
     */
    @org.junit.Test
    public void testGetDefaultPropertyList() {
        Map<String, String[]> propLists = wdkModel.getDefaultPropertyLists();
        // Assert.assertTrue("model doesn't have default property lists",
        // propLists != null && propLists.size() > 0);
        for (String plName : propLists.keySet()) {
            Assert.assertNotNull("property list name should not be null",
                    plName);
            String[] values = propLists.get(plName);
            Assert.assertTrue("property list should have some values",
                    values.length > 0);
        }
    }
}
