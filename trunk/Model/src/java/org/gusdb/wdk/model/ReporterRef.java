/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xingao
 * 
 */
public class ReporterRef {
    
    private String name;
    private String displayName;
    private String implementation;
    private boolean inReportMaker = true;
    private Map< String, String > properties = new LinkedHashMap< String, String >();
    
    /**
     * @return the implementation
     */
    public String getImplementation() {
        return implementation;
    }
    
    /**
     * @param implementation
     *            the implementation to set
     */
    public void setImplementation( String implementation ) {
        this.implementation = implementation;
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
     * @return the inReportMaker
     */
    public boolean isInReportMaker() {
        return inReportMaker;
    }
    
    /**
     * @param inReportMaker
     *            the inReportMaker to set
     */
    public void setInReportMaker( boolean inReportMaker ) {
        this.inReportMaker = inReportMaker;
    }
    
    public void addProperty( ReporterProperty property ) {
        this.properties.put( property.getName(), property.getValue() );
    }
    
    public Map< String, String > getProperties() {
        return new LinkedHashMap< String, String >( this.properties );
    }
}
