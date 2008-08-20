package org.gusdb.wdk.model.test;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

/**
 * SanityRecord.java
 * 
 * Object used in running a sanity test; represents a record in a wdk model.
 * 
 * Created: Mon August 23 12:00:00 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2006-01-13 13:40:47 -0500 (Fri, 13 Jan
 *          2006) $Author$
 */

public class SanityRecord extends WdkModelBase implements SanityElementI {

    // ------------------------------------------------------------------
    // Instance variables
    // ------------------------------------------------------------------

    /**
     * Name of the wdk record (in recordSetName.recordName format) represented
     * by this SanityRecord.
     */
    protected String twoPartName;

    /**
     * Modified by Jerric Primary key of the element that this record
     * represents.
     */
    protected String projectId;
    protected String primaryKey;

    // protected PrimaryKeyValue primaryKey;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public SanityRecord() {

    }

    // ------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------
    public void setRef(String twoPartName) {
        this.twoPartName = twoPartName;
    }

    public String getRef() {
        return this.twoPartName;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    // Added by Jerric
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public String toString() {
        return "SanityRecord twoPartName = " + twoPartName + " pk = "
                + primaryKey;
    }

    // ------------------------------------------------------------------
    // SanityElementI
    // ------------------------------------------------------------------

    public String getName() {
        return twoPartName;
    }

    public String getType() {
        return "record";
    }

    public String getCommand(String globalArgs) throws WdkModelException {

        String projectId = getProjectId();
        String pk = getPrimaryKey();

        StringBuffer command = new StringBuffer("wdkRecord " + globalArgs);

        command.append(" -record " + getRef() + " -primaryKey " + pk);

        if (projectId != null) command.append(" -project " + projectId);

        return command.toString();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
    // no nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
    // do nothing
    }

}
