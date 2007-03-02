package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.*;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * A wrapper on a {@link Question} that provides simplified access for
 * consumption by a view
 */
public class QuestionBean implements Serializable {

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
            if (params[i] instanceof FlatVocabParam) {
                paramBeans[i] = new FlatVocabParamBean(
                        (FlatVocabParam) params[i]);
            } else if (params[i] instanceof HistoryParam) {
                paramBeans[i] = new HistoryParamBean((HistoryParam) params[i]);
            } else if (params[i] instanceof DatasetParam) {
                paramBeans[i] = new DatasetParamBean((DatasetParam) params[i]);
            } else {
                paramBeans[i] = new ParamBean(params[i]);
            }
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

    public Map getAdditionalSummaryAttributesMap() {
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

    public BooleanQuestionLeafBean makeBooleanQuestionLeaf() {

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
    public AnswerBean makeAnswer(Map<String, Object> paramValues, int start,
            int end, Map<String, Boolean> sortingAttributes) throws WdkModelException, WdkUserException {
        return new AnswerBean(question.makeAnswer(paramValues, start, end, sortingAttributes));
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
}
