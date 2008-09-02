package org.gusdb.wdk.model.jspwrap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.report.Reporter;
import org.json.JSONException;

/**
 * A wrapper on a {@link Answer} that provides simplified access for consumption
 * by a view
 */
public class AnswerBean {

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
            return new RecordBean(instances[position++]);
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

    private static Logger logger = Logger.getLogger(AnswerBean.class);
    Answer answer;
    Map downloadConfigMap = null;

    String customName = null;

    public AnswerBean(Answer answer) {
        this.answer = answer;
    }

    /**
     * @return A Map of param displayName --> param value.
     */
    public Map<String, Object> getParams() {
        return answer.getDisplayParams();
    }

    public Map<String, Object> getInternalParams() {
        return answer.getIdsQueryInstance().getValues();
    }

    public String getQuestionUrlParams() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        Map<String, Object> params = getInternalParams();
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName).toString();

            // check if the parameter is multipick param
            Param param = answer.getQuestion().getParamMap().get(paramName);

            // check if it's dataset param, if so remove user signature
            if (param instanceof DatasetParam) {
                int pos = paramValue.indexOf(":");
                if (pos >= 0)
                    paramValue = paramValue.substring(pos + 1).trim();
            }
            String[] values = { paramValue };
            if (param instanceof FlatVocabParam) {
                FlatVocabParam fvParam = (FlatVocabParam) param;
                if (fvParam.getMultiPick()) values = paramValue.split(",");
            }
            // URL encode the values
            for (String value : values) {
                try {
                    sb.append("&" + paramName + "="
                            + URLEncoder.encode(value.trim(), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }
        }
        return sb.toString();
    }

    public String getSummaryUrlParams() throws WdkModelException {
        StringBuffer sb = new StringBuffer();
        Map<String, Object> params = getInternalParams();
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName).toString();

            // check if it's dataset param, if so remove user signature
            Param param = answer.getQuestion().getParamMap().get(paramName);
            if (param instanceof DatasetParam) {
                int pos = paramValue.indexOf(":");
                if (pos >= 0)
                    paramValue = paramValue.substring(pos + 1).trim();
            }

            try {
                paramName = URLEncoder.encode("myProp(" + paramName + ")",
                        "UTF-8");
                paramValue = URLEncoder.encode(paramValue, "UTF-8");
                sb.append("&" + paramName + "=" + paramValue);
            } catch (UnsupportedEncodingException ex) {
                throw new WdkModelException(ex);
            }
        }
        return sb.toString();
    }

    public String getChecksum() throws WdkModelException,
            NoSuchAlgorithmException, JSONException {
        return answer.getChecksum();
    }

    /**
     * @return opertation for boolean answer
     */
    public String getBooleanOperation() {
        if (!getIsBoolean()) {
            throw new RuntimeException("getBooleanOperation can not be called"
                    + " on simple AnswerBean");
        }
        Map<String, Object> params = answer.getIdsQueryInstance().getValues();
        return (String) params.get(BooleanQuery.OPERATOR_PARAM);
    }

    /**
     * @return first child answer for boolean answer, null if it is an answer
     *         for a simple question.
     */
    public AnswerBean getFirstChildAnswer() {
        if (!getIsBoolean()) {
            throw new RuntimeException("getFirstChildAnswer can not be called"
                    + " on simple AnswerBean");
        }
        Map<String, Object> params = answer.getIdsQueryInstance().getValues();
        Object childAnswer = params.get(BooleanQuery.LEFT_OPERAND_PARAM_PREFIX);
        return new AnswerBean((Answer) childAnswer);
    }

    /**
     * @return second child answer for boolean answer, null if it is an answer
     *         for a simple question.
     */
    public AnswerBean getSecondChildAnswer() {
        if (!getIsBoolean()) {
            throw new RuntimeException("getSecondChildAnswer can not be called"
                    + " on simple AnswerBean");
        }
        Map<String, Object> params = answer.getIdsQueryInstance().getValues();
        Object childAnswer = params.get(BooleanQuery.RIGHT_OPERAND_PARAM_PREFIX);
        return new AnswerBean((Answer) childAnswer);
    }

    public int getPageSize() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return answer.getPageSize();
    }

    public int getPageCount() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return answer.getPageCount();
    }

    public int getResultSize() throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        return answer.getResultSize();
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        return answer.getResultSizesByProject();
    }

    public boolean getIsBoolean() {
        return answer.getIsBoolean();
    }

    public boolean getIsCombinedAnswer() {
        return answer.getIsBoolean();
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(answer.getQuestion().getRecordClass());
    }

    public QuestionBean getQuestion() {
        return new QuestionBean(answer.getQuestion());
    }

    /**
     * @return A list of {@link RecordBean}s.
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Iterator<RecordBean> getRecords() throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        return new RecordBeanList(answer.getRecordInstances());
    }

    public void setDownloadConfigMap(Map downloadConfigMap) {
        this.downloadConfigMap = downloadConfigMap;
    }

    public AttributeFieldBean[] getSummaryAttributes() {
        Map<String, AttributeField> attribs = answer.getSummaryAttributes();
        Iterator<String> ai = attribs.keySet().iterator();
        Vector<AttributeFieldBean> v = new Vector<AttributeFieldBean>();
        while (ai.hasNext()) {
            String attribName = ai.next();
            v.add(new AttributeFieldBean(attribs.get(attribName)));
        }
        int size = v.size();
        AttributeFieldBean[] sumAttribs = new AttributeFieldBean[size];
        v.copyInto(sumAttribs);
        return sumAttribs;
    }

    public String[] getSummaryAttributeNames() {
        AttributeFieldBean[] sumAttribs = getSummaryAttributes();
        Vector<String> v = new Vector<String>();
        for (int i = 0; i < sumAttribs.length; i++) {
            String attribName = sumAttribs[i].getName();
            v.add(attribName);
        }
        int size = v.size();
        String[] sumAttribNames = new String[size];
        v.copyInto(sumAttribNames);
        return sumAttribNames;
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
        Question question = answer.getQuestion();
        Map<String, AttributeField> attribs = question.getAttributeFields(FieldScope.ReportMaker);
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
        RecordClass recordClass = answer.getQuestion().getRecordClass();
        Map<String, TableField> tables = recordClass.getTableFieldMap(FieldScope.ReportMaker);
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
        return answer.isDynamic();
    }

    /**
     * for controller: reset counter for download purpose
     */
    public void resetAnswerRowCursor() {
        int startIndex = answer.getStartIndex();
        int endIndex = answer.getEndIndex();
        answer = new Answer(answer, startIndex, endIndex);
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
    public String getResultMessage() {
        // TEST
        System.out.println("Result message from AnswerBean: "
                + answer.getResultMessage());

        return answer.getResultMessage();
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
     * @see org.gusdb.wdk.model.Answer#getReport(java.lang.String,
     *      java.util.Map)
     */
    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        return answer.createReport(reporterName, config);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#getSortingAttributeNames()
     */
    public String[] getSortingAttributeNames() {
        Map<String, Boolean> sortingFields = answer.getSortingAttributes();
        String[] array = new String[sortingFields.size()];
        sortingFields.keySet().toArray(array);
        return array;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#getSortingAttributeOrders()
     */
    public boolean[] getSortingAttributeOrders() {
        Map<String, Boolean> sortingFields = answer.getSortingAttributes();
        boolean[] array = new boolean[sortingFields.size()];
        int index = 0;
        for (boolean order : sortingFields.values()) {
            array[index++] = order;
        }
        return array;
    }

    public AttributeFieldBean[] getDisplayableAttributes() {
        List<AttributeField> fields = answer.getDisplayableAttributes();
        AttributeFieldBean[] fieldBeans = new AttributeFieldBean[fields.size()];
        int index = 0;
        for (AttributeField field : fields) {
            fieldBeans[index] = new AttributeFieldBean(field);
            index++;
        }
        return fieldBeans;
    }

    /**
     * @param attributeName
     * @see org.gusdb.wdk.model.Answer#addSumaryAttribute(java.lang.String)
     */
    public void setSumaryAttribute(String[] attributeNames) {
        answer.setSumaryAttributes(attributeNames);
    }

    public int getFilterSize(String filterName)
            throws NoSuchAlgorithmException, SQLException, WdkModelException,
            JSONException, WdkUserException {
        return answer.getFilterSize(filterName);
    }
}
