package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.AttributeValue;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableValue;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

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
    public PrimaryKeyAttributeValue getPrimaryKey() {
        return recordInstance.getPrimaryKey();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(recordInstance.getRecordClass());
    }

    public String[] getSummaryAttributeNames() {
        return recordInstance.getSummaryAttributeNames();
    }

    public Map<String, RecordBean> getNestedRecords() throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        Map<String, RecordInstance> nri = recordInstance.getNestedRecordInstances();
        Map<String, RecordBean> nriBeans = new LinkedHashMap<String, RecordBean>();
        for (String recordName : nri.keySet()) {
            RecordBean nextNrBean = new RecordBean(nri.get(recordName));
            nriBeans.put(recordName, nextNrBean);
        }
        return nriBeans;
    }

    public Map<String, RecordBean[]> getNestedRecordLists()
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
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
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, AttributeValue> getAttributes()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        return recordInstance.getAttributeValueMap();
    }

    /**
     * @return Map of attributeName -->
     *         {@link org.gusdb.wdk.model.AttributeFieldValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, AttributeValue> getSummaryAttributes()
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException {
        return recordInstance.getSummaryAttributeValueMap();
    }

    /**
     * @return Map of tableName --> {@link org.gusdb.wdk.model.TableValue}
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Map<String, TableValue> getTables() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return recordInstance.getTables();
    }
}
