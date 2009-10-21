/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.JSONException;

/**
 * @author Jerric
 * 
 */
public class WdkModelName extends WdkModelBase {

    private String displayName;
    private String version;
    private String releaseDate;

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the releaseDate
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate
     *            the releaseDate to set
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // no resources held by ModelName. do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
    // nothing to do
    }
}
