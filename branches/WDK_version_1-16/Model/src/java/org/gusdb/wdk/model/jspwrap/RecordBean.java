package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.PrimaryKeyValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

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
     * 
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
        List<String> v = new ArrayList<String>();
        while (ai.hasNext()) {
            String attribName = (String) ai.next();
            if (recordInstance.isSummaryAttribute(attribName)) {
                v.add(attribName);
            }
        }
        int size = v.size();
        String[] sumAttribNames = new String[size];
        v.toArray(sumAttribNames);
        return sumAttribNames;
    }

    public Map<String, RecordBean> getNestedRecords()
            throws WdkModelException, WdkUserException {
        Map<String, RecordInstance> nri = recordInstance.getNestedRecordInstances();
        Map<String, RecordBean> nriBeans = new LinkedHashMap<String, RecordBean>();
        for (String recordName : nri.keySet()) {
            RecordBean nextNrBean = new RecordBean(nri.get(recordName));
            nriBeans.put(recordName, nextNrBean);
        }
        return nriBeans;
    }

    public Map<String, RecordBean[]> getNestedRecordLists()
            throws WdkModelException, WdkUserException {
        Map<String, RecordInstance[]> nrl = recordInstance.getNestedRecordInstanceLists();
        Map<String, RecordBean[]> nrlBeans = new LinkedHashMap<String, RecordBean[]>();
        for (String recordName : nrl.keySet()) {
            RecordInstance nextNrl[] = nrl.get(recordName);
            RecordBean[] nextNrBeanList = new RecordBean[nextNrl.length];
            for (int i = 0; i < nextNrl.length; i++) {
                nextNrBeanList[i] = new RecordBean(nextNrl[i]);
            }
            nrlBeans.put(recordName, nextNrBeanList);
        }
        return nrlBeans;
    }

    /**
     * @return Map of attributeName -->
     * {@link org.gusdb.wdk.model.AttributeFieldValue}
     */
    public Map getAttributes() {
        return recordInstance.getAttributes();
    }

    /**
     * @return Map of attributeName -->
     * {@link org.gusdb.wdk.model.AttributeFieldValue}
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
}
