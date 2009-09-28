/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONException;

/**
 * @author Jerric
 * 
 */
public abstract class WdkModelBase {

    /**
     * exclude the resources the object hold which are not included in the
     * current project
     * 
     * @param projectId
     * @throws WdkModelException
     */
    public abstract void excludeResources(String projectId)
            throws WdkModelException;

    public abstract void resolveReferences(WdkModel wodkModel)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException;

    private Set<String> includeProjects;
    private Set<String> excludeProjects;

    protected boolean resolved = false;

    public WdkModelBase() {
        includeProjects = new LinkedHashSet<String>();
        excludeProjects = new LinkedHashSet<String>();
    }

    /**
     * @param excludeProjects
     *            the excludeProjects to set
     */
    public void setExcludeProjects(String excludeProjects) {
        excludeProjects = excludeProjects.trim();
        if (excludeProjects.length() == 0) return;

        String[] projects = excludeProjects.split(",");
        for (String project : projects) {
            this.excludeProjects.add(project.trim());
        }
    }

    /**
     * @param includeProjects
     *            the includeProjects to set
     */
    public void setIncludeProjects(String includeProjects) {
        includeProjects = includeProjects.trim();
        if (includeProjects.length() == 0) return;

        String[] projects = includeProjects.split(",");
        for (String project : projects) {
            this.includeProjects.add(project.trim());
        }
    }

    /**
     * 
     * @param projectId
     * @return true if the object is included in the current project
     */
    public boolean include(String projectId) {
        if (includeProjects.isEmpty()) { // no inclusions assigned
            return !excludeProjects.contains(projectId);
        } else { // has inclusions
            return includeProjects.contains(projectId);
        }
    }

    /**
     * @return the resolved
     */
    public boolean isResolved() {
        return resolved;
    }
}
