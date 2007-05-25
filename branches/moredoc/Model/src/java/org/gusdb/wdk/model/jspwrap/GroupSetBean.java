/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.GroupSet;
import org.gusdb.wdk.model.WdkModelException;


/**
 * @author:	 xingao
 * @created: Mar 2, 2007
 * @updated: Mar 2, 2007
 */
public class GroupSetBean {

    private GroupSet groupSet;
    
    public GroupSetBean(GroupSet groupSet) {
        this.groupSet = groupSet;
    }

    /**
     * @param name
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.GroupSet#getGroup(java.lang.String)
     */
    public GroupBean getGroup( String name ) throws WdkModelException {
        return new GroupBean(groupSet.getGroup( name ));
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.GroupSet#getGroups()
     */
    public GroupBean[ ] getGroups() {
        Group[] groups = groupSet.getGroups();
        GroupBean[] groupBeans = new GroupBean[groups.length];
        for (int i =0; i< groups.length; i++) {
            groupBeans[i] = new GroupBean(groups[i]);
        }
        return groupBeans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.GroupSet#getName()
     */
    public String getName() {
        return groupSet.getName();
    }
}
