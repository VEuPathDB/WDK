package org.gusdb.wdk.model.jspwrap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.BooleanQuery;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.SubType;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.report.Reporter;

import sun.util.logging.resources.logging;

/**
 * A wrapper on a {@link Answer} that provides simplified access for consumption
 * by a view
 */
public class AnswerBean {

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
        return answer.getParams();
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
        composeSubTypeUrl(sb);
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
        composeSubTypeUrl(sb);
        return sb.toString();
    }

    private void composeSubTypeUrl(StringBuffer sb) throws WdkModelException {
        SubType subType = answer.getQuestion().getRecordClass().getSubType();

        logger.debug("SubType: '" + subType + "', value '"
                + answer.getSubTypeValue() + "'");

        if (subType != null && answer.getSubTypeValue() != null) {
            String subTypeName = subType.getSubTypeParam().getName();
            String subTypeValue = (String) answer.getSubTypeValue();
            try {
                subTypeName = URLEncoder.encode("myProp(" + subTypeName + ")",
                        "UTF-8");
                subTypeValue = URLEncoder.encode(subTypeValue, "UTF-8");
                sb.append("&" + subTypeName + "=" + subTypeValue);
            } catch (UnsupportedEncodingException ex) {
                throw new WdkModelException(ex);
            }
        }
    }

    public Integer getDatasetId() throws WdkModelException {
        return answer.getDatasetId();
    }

    /**
     * @return opertation for boolean answer
     */
    public String getBooleanOperation() {
        System.err.println("the param map is: " + answer.getParams());
        if (!getIsBoolean()) {
            throw new RuntimeException(
                    "getBooleanOperation can not be called on simple AnswerBean");
        }
        return (String) answer.getParams().get(
                BooleanQuery.OPERATION_PARAM_NAME);
    }

    /**
     * @return first child answer for boolean answer, null if it is an answer
     *         for a simple question.
     */
    public AnswerBean getFirstChildAnswer() {
        if (!getIsBoolean()) {
            throw new RuntimeException(
                    "getFirstChildAnswer can not be called on simple AnswerBean");
        }
        Object childAnswer = answer.getParams().get(
                BooleanQuery.FIRST_ANSWER_PARAM_NAME);
        return new AnswerBean((Answer) childAnswer);
    }

    /**
     * @return second child answer for boolean answer, null if it is an answer
     *         for a simple question.
     */
    public AnswerBean getSecondChildAnswer() {
        if (!getIsBoolean()) {
            throw new RuntimeException(
                    "getSecondChildAnswer can not be called on simple AnswerBean");
        }
        Object childAnswer = answer.getParams().get(
                BooleanQuery.SECOND_ANSWER_PARAM_NAME);
        return new AnswerBean((Answer) childAnswer);
    }

    public int getPageSize() {
        return answer.getPageSize();
    }

    public int getPageCount() throws WdkModelException {
        return answer.getPageCount();
    }

    public int getResultSize() throws WdkModelException {
        return answer.getResultSize();
    }

    public Map<String, Integer> getResultSizesByProject()
            throws WdkModelException {
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
     */
    public Iterator getRecords() {
        return new RecordBeanList();
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
        Map<String, AttributeField> attribs = answer.getReportMakerAttributeFields();
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
        Map<String, TableField> tables = answer.getReportMakerTableFields();
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
        try {
            answer = answer.newAnswer();
        } catch (WdkModelException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
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
     * @see org.gusdb.wdk.model.Answer#getReport(java.lang.String,
     *      java.util.Map)
     */
    public Reporter createReport(String reporterName, Map<String, String> config)
            throws WdkModelException {
        return answer.createReport(reporterName, config);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#getSortingAttributeNames()
     */
    public String[] getSortingAttributeNames() {
        return answer.getSortingAttributeNames();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#getSortingAttributeOrders()
     */
    public boolean[] getSortingAttributeOrders() {
        return answer.getSortingAttributeOrders();
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

    /**
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Answer#getAllIds()
     */
    public String getAllIdList() throws WdkModelException {
        String[] ids = answer.getAllIds();
        StringBuffer sbIds = new StringBuffer();
        for (String id : ids)
            sbIds.append(id + " ");
        return sbIds.toString().trim();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#getSubTypeValues()
     */
    public Object getSubTypeValue() {
        return answer.getSubTypeValue();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Answer#isExpandSubType()
     */
    public boolean isExpandSubType() {
        return answer.isExpandSubType();
    }

    /**
     * @param expandSubType
     * @see org.gusdb.wdk.model.Answer#setExpandSubType(boolean)
     */
    public void setExpandSubType(boolean expandSubType) {
        answer.setExpandSubType(expandSubType);
    }

    /**
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Answer#getCacheTableName()
     */
    public String getCacheTableName() throws WdkModelException {
        return answer.getCacheTableName();
    }

    // //////////////////////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////////////////////

    class RecordBeanList implements Iterator {

        public int getSize() {
            return answer.getPageSize();
        }

        public boolean hasNext() {
            try {
                return answer.hasMoreRecordInstances();
            } catch (WdkModelException exp) {
                throw new RuntimeException(exp);
            }
        }

        public Object next() {
            try {
                return new RecordBean(answer.getNextRecordInstance());
            } catch (WdkModelException exp) {
                throw new RuntimeException(exp);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "remove isn't allowed on this iterator");
        }

    }

}
