/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
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
     *                the processClass to set
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

    /**
     * @return the local
     */
    public boolean isLocal() {
        return this.local;
    }

    /**
     * @param local
     *                the local to set
     */
    public void setLocal(boolean local) {
        this.local = local;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        super.resolveReferences(wdkModel);
        webServiceUrl = wdkModel.getWebServiceUrl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#makeInstance()
     */
    @Override
    public QueryInstance makeInstance(Map<String, Object> values)
            throws WdkModelException {
        return new ProcessQueryInstance(this, values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.query.Query#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsQuery) throws JSONException {
        jsQuery.put("process", this.processName);
        if (!local) jsQuery.put("url", this.webServiceUrl);
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
}
