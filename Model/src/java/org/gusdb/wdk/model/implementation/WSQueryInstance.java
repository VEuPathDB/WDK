package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.QueryInstance;
import org.gusdb.wdk.model.ResultList;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.Column;

import org.gusdb.wdk.model.process.WdkProcessClient;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

public class WSQueryInstance extends QueryInstance  {

    public WSQueryInstance (WSQuery query) {
        super(query);
    }

    public String getLowLevelQuery() throws WdkModelException {
	return null;
    }

    protected ResultList getNonpersistentResult() throws WdkModelException {
	WSQuery wsQuery = (WSQuery)query;

	try {
	    WdkProcessClient client = 
		new WdkProcessClient(wsQuery.getServiceUrl());

	    Map valMap = getValuesMap();
	    Set keys = valMap.keySet();
	    String[] paramNames = new String[keys.size()];
	    String[] paramVals = new String[keys.size()];
	    Iterator iter = keys.iterator();
	    int i=0;
	    while (iter.hasNext()) {
		String key = (String)iter.next();
		paramNames[i] = key;
		paramVals[i++] = (String)valMap.get(key);
	    }


	    Column[] columns = query.getColumns();
	    String[] columnNames = new String[columns.length];
	    i=0;
	    for (Column column : columns) {
		columnNames[i++] = column.getName();
	    }
	
	    String[][] result = client.invoke(wsQuery.getProcessName(), 
					      paramNames, 
					      paramVals, 
					      columnNames);

	    return new WSResultList(this, result);

	} catch (RemoteException e) {
	    throw new WdkModelException(e);
	} catch (ServiceException e) {
	    throw new WdkModelException(e);
	}
    }

    protected void writeResultToTable(String resultTableName, 
            ResultFactory rf) throws WdkModelException {
    }

}
