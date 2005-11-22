package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.Query;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.ResultList;

import java.net.URL;
import java.net.MalformedURLException;

public class WSQuery extends Query {
    
    URL serviceUrl;
    String processName;

    public WSQuery () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setServiceUrl(String name) throws WdkModelException {
	try {
	    serviceUrl = new URL(name);
	} catch (MalformedURLException e) {
	    throw new WdkModelException(e);
	}
    }

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
    }

    URL getServiceUrl() {
	return serviceUrl;
    }

    String getProcessName() {
	return processName;
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = super.formatHeader();
       buf.append("  serviceUrl='" + serviceUrl + "'" + newline);
       buf.append("  processName='" + processName + "'" + newline);
       return buf;
    }
 }
