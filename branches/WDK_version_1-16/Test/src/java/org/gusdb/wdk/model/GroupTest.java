/**
 * 
 */
package org.gusdb.wdk.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jerric
 * 
 */
public class GroupTest extends WdkModelTestBase {

    public static final String SAMPLE_GROUP_SET = "sampleGroups";
    public static final String SAMPLE_GROUP = "sampleGroup";
    
    /**
     * test getting all groups from model
     */
    @Test
    public void testGetGroups() {
        GroupSet[] groupSets = wdkModel.getAllGroupSets();
        Assert.assertTrue(groupSets.length > 0);
        for (GroupSet groupSet : groupSets) {
            Assert.assertNotNull(groupSet);
            String setName = groupSet.getName();
            Assert.assertTrue(setName.trim().length() > 0);

            // validate each group
            Group[] groups = groupSet.getGroups();
            Assert.assertTrue(groups.length > 0);
            for (Group group : groups) {
                Assert.assertNotNull(group);
                Assert.assertTrue(group.getFullName().startsWith(setName));
                Assert.assertTrue(group.getDisplayName().trim().length() > 0);
            }
        }
    }
    
    /**
     * @throws WdkModelException
     */
    @Test
    public void testGetGroup() throws WdkModelException {
        // get the group from the set
        GroupSet groupSet = wdkModel.getGroupSet(SAMPLE_GROUP_SET);
        Group group1 = groupSet.getGroup(SAMPLE_GROUP);
        Assert.assertNotNull(group1);
        
        // get the group from wdkModel directly
        Group group2 = (Group)wdkModel.resolveReference(SAMPLE_GROUP_SET + "." + SAMPLE_GROUP);
        Assert.assertSame(group1, group2);
    }
}
