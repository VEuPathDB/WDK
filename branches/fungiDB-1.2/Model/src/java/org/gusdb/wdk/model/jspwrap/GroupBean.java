/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Group;

/**
 * @author: xingao
 * @created: Mar 2, 2007
 * @updated: Mar 2, 2007
 */
public class GroupBean {

    private Group group;

    public GroupBean(Group group) {
        this.group = group;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getDescription()
     */
    public String getDescription() {
        return group.getDescription();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getDisplayName()
     */
    public String getDisplayName() {
        return group.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getDisplayType()
     */
    public String getDisplayType() {
        return group.getDisplayType();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getGroupSet()
     */
    public GroupSetBean getGroupSet() {
        return new GroupSetBean(group.getGroupSet());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getName()
     */
    public String getName() {
        return group.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#getFullName()
     */
    public String getFullName() {
        return group.getFullName();
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof GroupBean) {
            GroupBean groupBean = (GroupBean) obj;
            String fullName = group.getFullName();
            return fullName.equals(groupBean.getFullName());
        } else return false;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return group.getFullName().hashCode();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Group#isVisible()
     */
    public boolean isVisible() {
        return group.isVisible();
    }
}
