package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeCategoryTree;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * A wrapper on a {@link AnswerValue} that provides simplified access for
 * consumption by a view
 */
public class AnswerValueBean {

    private class RecordBeanList implements Iterator<RecordBean> {

        private RecordInstance[] instances;
        private int position = 0;

        private RecordBeanList(RecordInstance[] instances) {
            this.instances = instances;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return position < instances.length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        public RecordBean next() {
            return new RecordBean(answerValue.getUser(), instances[position++]);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }

    }

    private static Logger logger = Logger.getLogger(AnswerValueBean.class);

    private AnswerValue answerValue;
    Map<?, ?> downloadConfigMap = null;

    String customName = null;

    public AnswerValueBean(AnswerValue answerValue) {
        this.answerValue = answerValue;
    }
    
    public AnswerValue getAnswerValue() {
        return answerValue;
    }

    /**
     * @return A Map of param displayName --> param value.
     */
    public Map<String, String> getParams() {
        return answerValue.getParamDisplays();
    }

    public Map<String, String> getInternalParams() {
        return answerValue.getIdsQueryInstance().getValues();
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException, WdkUserException,
            SQLException {
        return answerValue.getChecksum();
    }

    /**
     * @return opertation for boolean answer
     */
    public String getBooleanOperation() {
        if (!getIsBoolean()) {
            throw new RuntimeException("getBooleanOperation can not be called"
                    + " on simple AnswerBean");
        }
        Map<String, String> params = answerValue.getIdsQueryInstance().getValues();
        return params.get(BooleanQuery.OPERATOR_PARAM);
    }

    /**
     * @return first child answer for boolean answer, null if it is an answer
     *         for a simple question.
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public AnswerValueBean getFirstChildAnswer()
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        if (!getIsCombined()) {
            throw new RuntimeException("getFirstChildAnswer can not be called"
                    + " on simple AnswerBean");
        }
        AnswerParam param = null;
        Map<String, String> params = answerValue.getIdsQueryInstance().getValues();
        if (getIsBoolean()) {
            BooleanQuery query = (BooleanQuery) answerValue.getIdsQueryInstance().getQuery();
            param = query.getLeftOperandParam();
        } else {
            Map<String, Param> paramMap = answerValue.getIdsQueryInstance().getQuery().getParamMap();
            for (Param p : paramMap.values()) {
                if (p instanceof AnswerParam) {
                    param = (AnswerParam) p;
                    break;
                }
            }
            if (param == null)
                throw new RuntimeException(
                        "combined question has no AnswerParam.");
        }
        String dependentValue = params.get(param.getName());
        User user = answerValue.getUser();
        return new AnswerValueBean(param.getAnswerValue(user, dependentValue));
    }

    /**
     * @return second child answer for boolean answer, null if it is an answer
     *         for a simple question.
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public AnswerValueBean getSecondChildAnswer()
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        if (!getIsBoolean()) {
            throw new RuntimeException("getSecondChildAnswer can not be called"
                    + " on simple AnswerBean");
        }
        BooleanQuery query = (BooleanQuery) answerValue.getIdsQueryInstance().getQuery();
        Map<String, String> params = answerValue.getIdsQueryInstance().getValues();
        AnswerParam param = query.getRightOperandParam();
        String dependentValue = params.get(param.getName());
        User user = answerValue.getUser();
        return new AnswerValueBean(param.getAnswerValue(user, dependentValue));
    }

    public int getPageSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return answerValue.getPageSize();
    }

    public int getPageCount() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return answerValue.getPageCount();
    }

    public int getResultSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return answerValue.getResultSize();
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        return answerValue.getResultSizesByProject();
    }

    public boolean getIsBoolean() {
        return answerValue.getIdsQueryInstance().getQuery().isBoolean();
    }

    public boolean getIsCombined() {
        return answerValue.getIdsQueryInstance().getQuery().isCombined();
    }

    public boolean getIsTransform() {
        return answerValue.getIdsQueryInstance().getQuery().isTransform();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(answerValue.getQuestion().getRecordClass());
    }

    public QuestionBean getQuestion() {
        return new QuestionBean(answerValue.getQuestion());
    }

    /**
     * @return A list of {@link RecordBean}s.
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Iterator<RecordBean> getRecords() throws Exception {
        try {
            return new RecordBeanList(answerValue.getRecordInstances());
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        }
    }

    public void setDownloadConfigMap(Map<?, ?> downloadConfigMap) {
        this.downloadConfigMap = downloadConfigMap;
    }

    public AttributeFieldBean[] getSummaryAttributes() {
        Map<String, AttributeField> attribs = answerValue.getSummaryAttributeFieldMap();
        AttributeFieldBean[] beans = new AttributeFieldBean[attribs.size()];
        int index = 0;
        for (AttributeField field : attribs.values()) {
            beans[index++] = new AttributeFieldBean(field);
        }

        return beans;
    }

    public String[] getSummaryAttributeNames() {
        AttributeFieldBean[] sumAttribs = getSummaryAttributes();
        String[] names = new String[sumAttribs.length];
        for (int i = 0; i < sumAttribs.length; i++) {
            names[i] = sumAttribs[i].getName();
        }
        return names;
    }

    public AttributeFieldBean[] getDownloadAttributes() {
        AttributeFieldBean[] sumAttribs = getSummaryAttributes();
        if (downloadConfigMap == null || downloadConfigMap.size() == 0) {
            return sumAttribs;
        }

        AttributeFieldBean[] rmAttribs = getAllReportMakerAttributes();
        Vector<AttributeFieldBean> v = new Vector<AttributeFieldBean>();
        for (int i = 0; i < rmAttribs.length; i++) {
            String attribName = rmAttribs[i].getName();
            Object configStatus = downloadConfigMap.get(attribName);
            // System.err.println("DEBUG AnswerBean: configStatus for " +
            // attrName + " is " + configStatus);
            if (configStatus != null) {
                v.add(rmAttribs[i]);
            }
        }
        int size = v.size();
        AttributeFieldBean[] downloadAttribs = new AttributeFieldBean[size];
        v.copyInto(downloadAttribs);
        return downloadAttribs;
    }

    public AttributeFieldBean[] getAllReportMakerAttributes() {
        Question question = answerValue.getQuestion();
        Map<String, AttributeField> attribs = question.getAttributeFieldMap(FieldScope.REPORT_MAKER);
        Iterator<String> ai = attribs.keySet().iterator();
        Vector<AttributeFieldBean> v = new Vector<AttributeFieldBean>();
        while (ai.hasNext()) {
            String attribName = ai.next();
            v.add(new AttributeFieldBean(attribs.get(attribName)));
        }
        int size = v.size();
        AttributeFieldBean[] rmAttribs = new AttributeFieldBean[size];
        v.toArray(rmAttribs);
        return rmAttribs;
    }

    public TableFieldBean[] getAllReportMakerTables() {
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        Map<String, TableField> tables = recordClass.getTableFieldMap(FieldScope.REPORT_MAKER);
        Iterator<String> ti = tables.keySet().iterator();
        Vector<TableFieldBean> v = new Vector<TableFieldBean>();
        while (ti.hasNext()) {
            String tableName = ti.next();
            v.add(new TableFieldBean(tables.get(tableName)));
        }
        int size = v.size();
        TableFieldBean[] rmTables = new TableFieldBean[size];
        v.toArray(rmTables);
        return rmTables;
    }

    public String[] getDownloadAttributeNames() {
        AttributeFieldBean[] downloadAttribs = getDownloadAttributes();
        Vector<String> v = new Vector<String>();
        for (int i = 0; i < downloadAttribs.length; i++) {
            v.add(downloadAttribs[i].getName());
        }
        int size = v.size();
        String[] downloadAttribNames = new String[size];
        v.copyInto(downloadAttribNames);
        return downloadAttribNames;
    }
    
    public void setCustomName(String name) {
        customName = name;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean getIsDynamic() {
        return answerValue.isDynamic();
    }

    /**
     * for controller: reset counter for download purpose
     */
    public void resetAnswerRowCursor() {
        logger.debug("reset answer cursor");
        int startIndex = answerValue.getStartIndex();
        int endIndex = answerValue.getEndIndex();
        answerValue = new AnswerValue(answerValue, startIndex, endIndex);
    }

    /**
     * for controller: reset counter for download purpose
     */
    public boolean getResetAnswerRowCursor() {
        resetAnswerRowCursor();
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Answer#getResultMessage()
     */
    public String getResultMessage() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        String message = answerValue.getResultMessage();
        System.out.println("Result message from AnswerBean: " + message);
        return message;
    }

    /**
     * @param reporterName
     * @param config
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @see org.gusdb.wdk.model.AnswerValue#getReport(java.lang.String,
     *      java.util.Map)
     */
    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        return answerValue.createReport(reporterName, config);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerValue#getSortingAttributeNames()
     */
    public String[] getSortingAttributeNames() {
        Map<String, Boolean> sortingFields = answerValue.getSortingMap();
        String[] array = new String[sortingFields.size()];
        sortingFields.keySet().toArray(array);
        return array;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerValue#getSortingAttributeOrders()
     */
    public boolean[] getSortingAttributeOrders() {
        Map<String, Boolean> sortingFields = answerValue.getSortingMap();
        boolean[] array = new boolean[sortingFields.size()];
        int index = 0;
        for (boolean order : sortingFields.values()) {
            array[index++] = order;
        }
        return array;
    }

    public AttributeFieldBean[] getDisplayableAttributes() {
        List<AttributeField> fields = answerValue.getDisplayableAttributes();
        AttributeFieldBean[] fieldBeans = new AttributeFieldBean[fields.size()];
        int index = 0;
        for (AttributeField field : fields) {
            fieldBeans[index] = new AttributeFieldBean(field);
            index++;
        }
        return fieldBeans;
    }

    public AttributeCategoryTree getDisplayableAttributeTree() {
    	return answerValue.getDisplayableAttributeTree();
    }

    public AttributeCategoryTree getReportMakerAttributeTree() {
    	return answerValue.getReportMakerAttributeTree();
    }
    
    /**
     * @param attributeName
     * @see org.gusdb.wdk.model.AnswerValue#addSumaryAttribute(java.lang.String)
     */
    public void setSumaryAttribute(String[] attributeNames) {
        answerValue.setSummaryAttributes(attributeNames);
    }

    public void setFilter(String filterName) throws WdkModelException {
        answerValue.setFilter(filterName);
    }

    public int getFilterSize(String filterName)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        return answerValue.getFilterSize(filterName);
    }

    public AnswerFilterInstanceBean getFilter() {
        AnswerFilterInstance filter = answerValue.getFilter();
        if (filter == null) return null;
        return new AnswerFilterInstanceBean(filter);
    }

    public List<String[]> getAllIds() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return answerValue.getAllIds();
    }

    /**
     * @return
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.AnswerValue#getAllPkValues()
     */
    public String getAllIdList() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        List<String[]> pkValues = answerValue.getAllIds();
        StringBuffer buffer = new StringBuffer();
        for (String[] pkValue : pkValues) {
            if (buffer.length() > 0) buffer.append("\n");
            for (int i = 0; i < pkValue.length; i++) {
                if (i > 0) buffer.append(", ");
                buffer.append(pkValue[i]);
            }
        }
        return buffer.toString();
    }

    public AnswerValueBean makeAnswerValue(int pageStart, int pageEnd) {
        AnswerValue answerValue = new AnswerValue(this.answerValue, pageStart,
                pageEnd);
        return new AnswerValueBean(answerValue);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerValue#getEndIndex()
     */
    public int getEndIndex() {
        return answerValue.getEndIndex();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.AnswerValue#getStartIndex()
     */
    public int getStartIndex() {
        return answerValue.getStartIndex();
    }

    /**
     * @param startIndex
     * @param endIndex
     * @see org.gusdb.wdk.model.AnswerValue#setPageIndex(int, int)
     */
    public void setPageIndex(int startIndex, int endIndex) {
        answerValue.setPageIndex(startIndex, endIndex);
    }
    
    /**
     * Temporary method to allow easy on/off of attribute tree
     * for attribute selection in both results page and report
     * config pages.
     * 
     * @return whether attribute tree should be used (columns otherwise)
     */
    public boolean getUseAttributeTree() {
    	return true;
    }
}
