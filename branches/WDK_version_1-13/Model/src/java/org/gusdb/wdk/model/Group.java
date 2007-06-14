/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author: xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
public class Group {
    
    private static Group empty;
    
    private String name;
    private String displayName;
    private String description;
    private String displayType;
    
    private GroupSet groupSet;
    
    public static Group Empty() {
        if (empty == null) {
            empty = new Group();
            empty.displayType = "empty";
        }
        return empty;
    }
    
    public Group( ) {
        // initialize an empty group
        name = "empty";
        displayName = "";
        description = "";
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description
     *            the description to set
     */
    public void setDescription( String description ) {
        this.description = description;
    }
    
    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName( String displayName ) {
        this.displayName = displayName;
    }
    
    /**
     * @return the groupSet
     */
    public GroupSet getGroupSet() {
        return groupSet;
    }
    
    /**
     * @param groupSet
     *            the groupSet to set
     */
    public void setGroupSet( GroupSet groupSet ) {
        this.groupSet = groupSet;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getFullName() {
        if ( groupSet != null ) return groupSet.getName() + "." + name;
        else return name;
    }
    
    /**
     * @return the displayType
     */
    public String getDisplayType() {
        return displayType;
    }
    
    /**
     * @param displayType
     *            the displayType to set
     */
    public void setDisplayType( String displayType ) {
        this.displayType = displayType;
    }
    
    public void resolveReferences( WdkModel model ) throws WdkModelException {
    // do nothing
    }
    
    public void setResources( WdkModel model ) throws WdkModelException {
    // do nothing
    }
}
