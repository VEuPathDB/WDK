/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public class ProcessQuery extends Query {

    private String processName;
    private String webServiceUrl;
    private boolean local = false;

    public ProcessQuery() {
        super();
    }

    private ProcessQuery(ProcessQuery query) {
        super(query);
        this.processName = query.processName;
        this.webServiceUrl = query.webServiceUrl;
        this.local = query.local;
    }

    /**
     * @return the processClass
     */
    public String getProcessName() {
        return this.processName;
    }

    /**
     * @param processClass
     *            the processClass to set
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * @return the webServiceUrl
     */
    public String getWebServiceUrl() {
        return this.webServiceUrl;
    }

    public void setWebServiceUrl(String webServiceUrl) {
        this.webServiceUrl = webServiceUrl;
    }

    /**
     * @return the local
     */
    public boolean isLocal() {
        return this.local;
    }

    /**
     * @param local
     *            the local to set
     */
    public void setLocal(boolean local) {
        this.local = local;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.Query#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveQueryReferences(WdkModel wdkModel) throws WdkModelException {
        if (webServiceUrl == null)
            webServiceUrl = wdkModel.getModelConfig().getWebServiceUrl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#makeInstance()
     */
    @Override
    public QueryInstance makeInstance(User user, Map<String, String> values,
            boolean validate, int assignedWeight, Map<String, String> context)
            throws WdkModelException, WdkUserException {
        return new ProcessQueryInstance(user, this, values, validate,
                assignedWeight, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsQuery, boolean extra)
            throws JSONException {
        if (extra) {
            jsQuery.put("process", this.processName);
            if (!local)
                jsQuery.put("url", this.webServiceUrl);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#clone()
     */
    @Override
    public Query clone() {
        return new ProcessQuery(this);
    }

    /**
     * Process Query is always cached.
     */
    @Override
    public boolean isCached() {
        return true;
    }
}
