/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.Serializable;

/**
 * @author Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -7829729638618781482L;
    private String defaultValue;
    private boolean allowNull;
    private String nullValue;
    
    /**
     * 
     */
    public ParamReference( ) {
        super();
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public ParamReference( String twoPartName ) throws WdkModelException {
        super( twoPartName );
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @return Returns the defaultValue.
     */
    public String getDefault() {
        return this.defaultValue;
    }
    
    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefault( String defaultValue ) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * @return the allowNull
     */
    public boolean isAllowNull() {
        return allowNull;
    }
    
    /**
     * @param allowNull
     *            the allowNull to set
     */
    public void setAllowNull( boolean allowNull ) {
        this.allowNull = allowNull;
    }
    
    /**
     * @return the nullValue
     */
    public String getNullValue() {
        return nullValue;
    }
    
    /**
     * @param nullValue
     *            the nullValue to set
     */
    public void setNullValue( String nullValue ) {
        this.nullValue = nullValue;
    }
}
