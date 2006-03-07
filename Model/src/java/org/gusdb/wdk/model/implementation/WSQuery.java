package org.gusdb.wdk.model.implementation;

import java.util.Set;

import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class WSQuery extends Query {
    
    String processName;
    String webServiceUrl;

    public WSQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setProcessName(String name) {
	processName = name;
    }

    public QueryInstance makeInstance() {
	return new WSQueryInstance(this);
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void setResources(WdkModel model) throws WdkModelException {
	super.setResources(model);
	this.webServiceUrl = model.getWebServiceUrl();
    }

    String getProcessName() {
	return processName;
    }

    String getWebServiceUrl() {
	return webServiceUrl;
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = super.formatHeader();
       buf.append("  processName='" + processName + "'" + newline);
       return buf;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Query#getBaseQuery(java.util.Set)
     */
    @Override
    public Query getBaseQuery(Set<String> excludedColumns) {
        WSQuery query = new WSQuery();
        // clone the base part
        clone(query, excludedColumns);
        // clone the members belongs to itself
        query.processName = this.processName;
        query.webServiceUrl = this.webServiceUrl;
        return query;
    }
 }
