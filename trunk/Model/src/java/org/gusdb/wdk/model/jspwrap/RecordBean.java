package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.PrimaryKeyValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A wrapper on a {@link RecordInstance} that provides simplified access for 
 * consumption by a view
 */ 
public class RecordBean {

    RecordInstance recordInstance;

    public RecordBean(RecordInstance recordInstance) {
	this.recordInstance = recordInstance;
    }

    /**
     * modified by Jerric
     * @return  
     */
    public PrimaryKeyValue getPrimaryKey() {
	return recordInstance.getPrimaryKey();
    }

    public RecordClassBean getRecordClass() {
	return new RecordClassBean(recordInstance.getRecordClass());
    }

    public String[] getSummaryAttributeNames() {
	Map attribs = getAttributes();
	Iterator ai = attribs.keySet().iterator();
	Vector v = new Vector();
	while (ai.hasNext()) {
	    String attribName = (String)ai.next();
	    if (recordInstance.isSummaryAttribute(attribName)) {
		v.add(attribName);
	    }
	}
	int size = v.size();
	String[] sumAttribNames = new String[size];
	v.copyInto(sumAttribNames);
	return sumAttribNames;
    }

    public Map getNestedRecords() {
		
	Map nri = null;
	Map nriBeans = null;
	try {
	    nriBeans = new LinkedHashMap();
	    nri = recordInstance.getNestedRecordInstances();
	    Iterator recordNames = nri.keySet().iterator();
	    while (recordNames.hasNext()){
		String nextRecordName = (String)recordNames.next();
		RecordInstance nextNr = (RecordInstance)nri.get(nextRecordName);
		RecordBean nextNrBean = new RecordBean(nextNr);
		nriBeans.put(nextRecordName, nextNrBean);
	    }
	}
	catch (WdkModelException e){
	    throw new RuntimeException(e);
	}
	catch (WdkUserException e){
	    throw new RuntimeException(e);
	}
	return nriBeans;
    }

    public Map getNestedRecordLists() {

	Map nrl = null;
	Map nrlBeans = null;
	try {
	    nrlBeans = new LinkedHashMap();
	    nrl = recordInstance.getNestedRecordInstanceLists();
	    Iterator recordNames = nrl.keySet().iterator();
	    while (recordNames.hasNext()){
		String nextRecordName = (String)recordNames.next();
		RecordInstance nextNrl[] = (RecordInstance[])nrl.get(nextRecordName);
		System.err.println("RecordBean: after retrieving, nrl " + nextRecordName + " has " + nextNrl.length + " entries");
		RecordBean[] nextNrBeanList = new RecordBean[nextNrl.length];
		for (int i = 0; i < nextNrl.length; i++){
		    RecordInstance nextRi = nextNrl[i];
		    System.err.println("adding nextRi to list");
		    RecordBean nextNrBean = new RecordBean(nextRi);
		    nextNrBeanList[i] = nextNrBean;
		}
		System.err.println("RecordBean.getNestedRecordLists: adding nrl " + nextRecordName + " to list, list has " + nextNrBeanList.length);
		
		nrlBeans.put(nextRecordName, nextNrBeanList);
	    }
	}
	catch (WdkModelException e){
	    throw new RuntimeException(e);
	}
	catch (WdkUserException e){
	    throw new RuntimeException(e);
	}
	return nrlBeans;
    }

    /**
     * @return Map of attributeName --> {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map getAttributes() {
	return recordInstance.getAttributes();
    }

    /**
     * @return Map of attributeName --> {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map getSummaryAttributes() {
	return recordInstance.getSummaryAttributes();
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.TableFieldValue}
     */
    public Map getTables() {
	return recordInstance.getTables();
    }

    /**
     * used by the controller. Modified by Jerric
     * @param projectID
     * @param key
     * @throws WdkModelException 
     */
    public void assignPrimaryKey(String projectID, String key) throws WdkModelException {
	recordInstance.setPrimaryKey(projectID, key);
    }
}
