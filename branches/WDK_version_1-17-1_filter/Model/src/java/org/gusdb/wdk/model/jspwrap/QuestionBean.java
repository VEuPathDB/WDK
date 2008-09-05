package org.gusdb.wdk.model.jspwrap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AbstractEnumParam;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.EnumParam;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * A wrapper on a {@link Question} that provides simplified access for
 * consumption by a view
 */
public class QuestionBean {

    /**
     * Added by Jerric - to make QuestionBean serializable
     */
    private static final long serialVersionUID = 6353373897551871273L;

    Question question;

    public QuestionBean(Question question) {
        this.question = question;
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(question.getRecordClass());
    }

    public ParamBean[] getParams() {
        Param[] params = question.getParams();
        ParamBean[] paramBeans = new ParamBean[params.length];
        for (int i = 0; i < params.length; i++) {
            paramBeans[i] = getParam(params[i]);
        }
        return paramBeans;
    }

    public Map<String, ParamBean> getParamsMap() {
        ParamBean[] paramBeans = getParams();
        Map<String, ParamBean> pMap = new LinkedHashMap<String, ParamBean>();
        for (int i = 0; i < paramBeans.length; i++) {
            ParamBean p = paramBeans[i];
            pMap.put(p.getName(), p);
        }
        return pMap;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Question#getParamMapByGroups()
     */
    public Map<GroupBean, Map<String, ParamBean>> getParamMapByGroups() {
        Map<Group, Map<String, Param>> paramGroups = question.getParamMapByGroups();
        Map<GroupBean, Map<String, ParamBean>> paramGroupBeans = new LinkedHashMap<GroupBean, Map<String, ParamBean>>();
        for (Group group : paramGroups.keySet()) {
            GroupBean groupBean = new GroupBean(group);
            Map<String, Param> paramGroup = paramGroups.get(group);
            Map<String, ParamBean> paramGroupBean = new LinkedHashMap<String, ParamBean>();
            for (String paramName : paramGroup.keySet()) {
                Param param = paramGroup.get(paramName);
                paramGroupBean.put(paramName, getParam(param));
            }
            paramGroupBeans.put(groupBean, paramGroupBean);
        }
        return paramGroupBeans;
    }

    /**
     * @param displayType
     * @return
     * @see org.gusdb.wdk.model.Question#getParamMapByGroups(java.lang.String)
     */
    public Map<GroupBean, Map<String, ParamBean>> getParamMapByGroups(
            String displayType) {
        Map<Group, Map<String, Param>> paramGroups = question.getParamMapByGroups(displayType);
        Map<GroupBean, Map<String, ParamBean>> paramGroupBeans = new LinkedHashMap<GroupBean, Map<String, ParamBean>>();
        for (Group group : paramGroups.keySet()) {
            GroupBean groupBean = new GroupBean(group);
            Map<String, Param> paramGroup = paramGroups.get(groupBean);
            Map<String, ParamBean> paramGroupBean = new LinkedHashMap<String, ParamBean>();
            for (String paramName : paramGroup.keySet()) {
                Param param = paramGroup.get(paramName);
                paramGroupBean.put(paramName, getParam(param));
            }
        }
        return paramGroupBeans;
    }

    private ParamBean getParam(Param param) {
        if (param instanceof AbstractEnumParam) {
            return new EnumParamBean((AbstractEnumParam) param);
        } else if (param instanceof HistoryParam) {
            return new HistoryParamBean((HistoryParam) param);
        } else if (param instanceof DatasetParam) {
            return new DatasetParamBean((DatasetParam) param);
        } else {
            return new ParamBean(param);
        }
    }

    public Map<String, AttributeFieldBean> getSummaryAttributesMap() {
        Map<String, AttributeField> attribs = question.getSummaryAttributes();
        Iterator<String> ai = attribs.keySet().iterator();

        Map<String, AttributeFieldBean> saMap = new LinkedHashMap<String, AttributeFieldBean>();
        while (ai.hasNext()) {
            String attribName = ai.next();
            saMap.put(attribName, new AttributeFieldBean(
                    attribs.get(attribName)));
        }
        return saMap;
    }

    public Map<String, AttributeFieldBean> getReportMakerAttributesMap() {
        Map<String, AttributeField> attribs = question.getReportMakerAttributeFields();
        Iterator<String> ai = attribs.keySet().iterator();

        Map<String, AttributeFieldBean> rmaMap = new LinkedHashMap<String, AttributeFieldBean>();
        while (ai.hasNext()) {
            String attribName = ai.next();
            rmaMap.put(attribName, new AttributeFieldBean(
                    attribs.get(attribName)));
        }
        return rmaMap;
    }

    public Map<String, TableFieldBean> getReportMakerTablesMap() {
        Map<String, TableField> tables = question.getReportMakerTableFields();
        Iterator<String> ti = tables.keySet().iterator();

        Map<String, TableFieldBean> rmtMap = new LinkedHashMap<String, TableFieldBean>();
        while (ti.hasNext()) {
            String tableName = ti.next();
            rmtMap.put(tableName, new TableFieldBean(tables.get(tableName)));
        }
        return rmtMap;
    }

    public Map<String, FieldBean> getReportMakerFieldsMap() {
        Map<String, Field> fields = question.getReportMakerFields();
        Iterator<String> fi = fields.keySet().iterator();

        Map<String, FieldBean> rmfMap = new LinkedHashMap<String, FieldBean>();
        while (fi.hasNext()) {
            String fieldName = fi.next();
            Field field = fields.get(fieldName);
            if (field instanceof AttributeField) {
                rmfMap.put(fieldName, new AttributeFieldBean(
                        (AttributeField) field));
            } else if (field instanceof TableField) {
                rmfMap.put(fieldName, new TableFieldBean((TableField) field));
            }
        }
        return rmfMap;
    }

    public Map<String, AttributeFieldBean> getAdditionalSummaryAttributesMap() {
        Map<String, AttributeFieldBean> all = getReportMakerAttributesMap();
        Map<String, AttributeFieldBean> dft = getSummaryAttributesMap();
        Map<String, AttributeFieldBean> opt = new LinkedHashMap<String, AttributeFieldBean>();
        Iterator<String> ai = all.keySet().iterator();
        while (ai.hasNext()) {
            String attribName = ai.next();
            if (dft.get(attribName) == null) {
                opt.put(attribName, all.get(attribName));
            }
        }
        return opt;
    }

    public String getName() {
        return question.getName();
    }

    public String getFullName() {
        return question.getFullName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Question#getQuestionSetName()
     */
    public String getQuestionSetName() {
        return question.getQuestionSetName();
    }

    public String getDisplayName() {
        return question.getDisplayName();
    }

    public String getHelp() {
        return question.getHelp();
    }

    public BooleanQuestionLeafBean makeBooleanQuestionLeaf()
            throws WdkModelException {
        BooleanQuestionNode bqn = new BooleanQuestionNode(this.question, null);
        BooleanQuestionLeafBean leaf = new BooleanQuestionLeafBean(bqn, null);
        return leaf;

    }

    /**
     * Called by the controller
     * 
     * @param paramValues
     *            Map of paramName-->value
     * @param start
     *            Index of the first record to include in the answer
     * @param end
     *            Index of the last record to include in the answer
     */
    public AnswerValueBean makeAnswerValue(Map<String, Object> paramValues, int start,
            int end, Map<String, Boolean> sortingAttributes)
            throws WdkModelException, WdkUserException {
        return new AnswerValueBean(question.makeAnswerValue(paramValues, start, end,
                sortingAttributes));
    }

    public String getDescription() {
        return question.getDescription();
    }

    public String getSummary() {
        return question.getSummary();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Question#getCategory()
     */
    public String getCategory() {
        return question.getCategory();
    }

    /**
     * A indicator to the controller whether this question bean should make 
     * answer beans that contains all records in one page or not.
     * 
     * @return
     * @see org.gusdb.wdk.model.Question#isFullAnswerValue()
     */
    public boolean isFullAnswerValue() {
        return question.isFullAnswerValue();
    }

    /**
     * make an answer bean that returns all record beans in one page.
     * 
     * @param paramValues
     * @param sortingAttributes
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Question#makeAnswerValue(java.util.Map,
     *      java.util.Map)
     */
    public AnswerValueBean makeAnswerValue(Map<String, Object> paramValues,
            Map<String, Boolean> sortingAttributes) throws WdkUserException,
            WdkModelException {
        return new AnswerValueBean(question.makeAnswerValue(paramValues,
                sortingAttributes));
    }

    /**
     * make an answer bean that returns all record beans in one page, sorted by 
     * the given attribute list.
     * 
     * @param paramValues
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Question#makeAnswerValue(java.util.Map)
     */
    public AnswerValueBean makeAnswerValue(Map<String, Object> paramValues)
            throws WdkUserException, WdkModelException {
        return new AnswerValueBean(question.makeAnswerValue(paramValues));
    }

    /**
     * @param propertyListName
     * @return
     * @see org.gusdb.wdk.model.Question#getPropertyList(java.lang.String)
     */
    public String[] getPropertyList(String propertyListName) {
        return question.getPropertyList(propertyListName);
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Question#getPropertyLists()
     */
    public Map<String, String[]> getPropertyLists() {
        return question.getPropertyLists();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Question#isNoSummaryOnSingleRecord()
     */
    public boolean isNoSummaryOnSingleRecord() {
        return question.isNoSummaryOnSingleRecord();
    }

    public String getShortDisplayName() {
        return question.getShortDisplayName();
    }
}
