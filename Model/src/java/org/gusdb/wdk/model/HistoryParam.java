/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;

import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class HistoryParam extends Param {

    private UserFactory factory;

    private String recordClassRef;
    private RecordClass recordClass;

    public HistoryParam() {}

    public HistoryParam(HistoryParam param) {
        super(param);
        this.factory = param.factory;
        this.recordClassRef = param.recordClassRef;
        this.recordClass = param.recordClass;
    }

    /**
     * @param recordClassRef
     *                the recordClassRef to set
     */
    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public String validateValue(Object value) throws WdkModelException {
        String compound = value.toString();
        String[] parts = compound.split(":");
        if (parts.length != 2)
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;

        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();
        if (strHistId.matches("\\d+")) {
            int historyId = Integer.parseInt(strHistId);
            try {
                User user = factory.loadUserBySignature(signature);
                user.getHistory(historyId);
            } catch (WdkUserException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            }
            return null;
        } else {
            return "Invalid value format of HistoryParam " + name + ": "
                    + value;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        if (recordClass == null)
            recordClass = (RecordClass) model.resolveReference(recordClassRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void setResources(WdkModel model) throws WdkModelException {
        super.setResources(model);
        try {
            factory = model.getUserFactory();
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    public String getInternalValue(Object value) throws WdkModelException {
        try {
            History history = getHistory((String) value);
            // return history.getCacheFullTable();
            return null;
        } catch (WdkUserException ex) {
            throw new WdkModelException(ex);
        }
    }

    public History getHistory(String combinedId) throws WdkUserException,
            WdkModelException {
        String[] parts = combinedId.split(":");
        // the input have a valid user id and history id
        String signature = parts[0].trim();
        String strHistId = parts[1].trim();

        int historyId = Integer.parseInt(strHistId);
        User user = factory.loadUserBySignature(signature);
        return user.getHistory(historyId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    @Override
    public String compressValue(Object value) throws WdkModelException,
            NoSuchAlgorithmException {
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            StringBuffer sb = new StringBuffer();
            for (String strVal : array) {
                if (sb.length() > 0) sb.append(",");
                sb.append(strVal);
            }
            value = sb.toString();
        }
        return super.compressValue(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new HistoryParam(this);
    }

    public History[] getHistories(User user) throws WdkUserException,
            WdkModelException {
        return user.getHistories(recordClassRef);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) {
    // TODO Auto-generated method stub

    }
}
