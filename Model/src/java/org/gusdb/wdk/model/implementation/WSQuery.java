package org.gusdb.wdk.model.implementation;

import java.io.Serializable;
import java.util.Set;

import org.gusdb.wdk.model.ParamReference;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class WSQuery extends Query implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3057750262476024684L;
    private String processName;
    private String webServiceUrl;

    /**
     * If the process query is from local. If it is local, WDK will invoke the
     * process query directly (WDK and the process implementation (the
     * WSFPlugin) must be deployed into the same webapp; otherwise, WDK will
     * invoke the process query via web service. Default is not local.
     */
    private boolean local = false;

    public WSQuery() {
        super();
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setProcessName(String name) {
        processName = name;
        signature = null;
    }

    public QueryInstance makeInstance() {
        return new WSQueryInstance(this);
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Protected ////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    protected void setResources(WdkModel model) throws WdkModelException {
        super.setResources(model);
        this.webServiceUrl = model.getWebServiceUrl();
        signature = null;
    }

    public String getProcessName() {
        return processName;
    }

    public String getWebServiceUrl() {
        return webServiceUrl;
    }

    protected StringBuffer formatHeader() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = super.formatHeader();
        buf.append("  processName='" + processName + "'" + newline);
        return buf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#getBaseQuery(java.util.Set)
     */
    @Override
    public Query getBaseQuery(Set<String> excludedColumns)
            throws WdkModelException {
        WSQuery query = new WSQuery();
        // clone the base part
        clone(query, excludedColumns);
        // clone the members belongs to itself
        query.processName = this.processName;
        query.webServiceUrl = this.webServiceUrl;
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#getSignatureData()
     */
    @Override
    protected String getSignatureData() {
        return processName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Query#addParamRef(org.gusdb.wdk.model.ParamReference)
     */
    @Override
    public void addParamRef(ParamReference paramRef) {
        // force the default value of quote to be false in WsQuery
        paramRef.setQuote(false);
        super.addParamRef(paramRef);
    }

    /**
     * @return the local
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * @param local the local to set
     */
    public void setLocal(boolean local) {
        this.local = local;
    }
}
