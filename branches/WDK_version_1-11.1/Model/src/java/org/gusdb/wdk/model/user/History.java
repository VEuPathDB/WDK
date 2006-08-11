/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author xingao
 *
 */
public class History {

    private User user;
    private int historyId;
    private String fullName;
    private Date createdTime;
    private String customName;
    private Map<String, String> params;
    
    History(User user, int historyId) {
        this.historyId = historyId;
        params = new LinkedHashMap<String, String>();
    }

    
    /**
     * @return Returns the createTime.
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    
    /**
     * @param createTime The createTime to set.
     */
    void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    
    /**
     * @return Returns the customName.
     */
    public String getCustomName() {
        return customName;
    }
    
    /**
     * @param customName The customName to set.
     */
    public void setCustomName(String customName) {
        this.customName = customName;
        // also, save the history into database
        user.saveHistory(this);
    }

    /**
     * called by UserFactory when loading and filling in the data
     * @param customName
     */
    void setCustomTimeNoUp(String customName) {
        this.customName = customName;
    }
    
    /**
     * @return Returns the fullName.
     */
    public String getFullName() {
        return fullName;
    }

    
    /**
     * @param fullName The fullName to set.
     */
    void setFullName(String fullName) {
        this.fullName = fullName;
    }

    
    /**
     * @return Returns the historyId.
     */
    public int getHistoryId() {
        return historyId;
    }
    
    public void addParam(String paramName, String paramValue) {
        params.put(paramName, paramValue);
    }
    
    public Map<String, String> getParams() {
        return new LinkedHashMap<String, String>(params);
    }
    
    public String getParam(String paramName) {
        return params.get(paramName);
    }
}
