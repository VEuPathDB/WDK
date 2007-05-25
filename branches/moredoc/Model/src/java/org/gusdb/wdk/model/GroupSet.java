/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author:	 xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
public class GroupSet implements ModelSetI {
    
    private String name;
    private Map<String, Group> groupSet;
    
    public GroupSet() {
        groupSet = new LinkedHashMap< String, Group >();
    }
    
    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.ModelSetI#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    public Group getGroup(String name) throws WdkModelException {
        Group group = groupSet.get( name );        
        if (group == null)
            throw new WdkModelException("Group Set " + getName()
                    + " does not include group " + name);
        return group;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.ModelSetI#getElement(java.lang.String)
     */
    public Object getElement( String elementName ) {
        return groupSet.get( elementName );
    }
    
    public Group[] getGroups() {
        Group[] groups = new Group[groupSet.size()];
        groupSet.values().toArray( groups );
        return groups;
    }
    
    public void addGroup(Group group) throws WdkModelException {
        if (groupSet.containsKey(group.getName())) 
            throw new WdkModelException("Group named " 
                    + group.getName() 
                    + " already exists in group set "
                    + name);
        groupSet.put( group.getName(), group );
    }
    
    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.ModelSetI#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    public void resolveReferences( WdkModel model ) throws WdkModelException {
        for (Group group : groupSet.values()) {
            group.resolveReferences( model );
        }
    }
    
    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.ModelSetI#setResources(org.gusdb.wdk.model.WdkModel)
     */
    public void setResources( WdkModel model ) throws WdkModelException {
        for (Group group : groupSet.values()) {
            group.setResources( model );
        }
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = new StringBuffer("GroupSet: name='" + name 
                       + "'");
       buf.append( newline );
       for (Group group : groupSet.values()) {
        buf.append( newline );
        buf.append( ":::::::::::::::::::::::::::::::::::::::::::::" );
        buf.append( newline );
       buf.append(group).append( newline );
       }

       return buf.toString();
    }

}
