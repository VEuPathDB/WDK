/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerFilterInstance extends WdkModelBase {

    private String name;
    private String displayName;
    private boolean isDefault;
    private boolean isBooleanExpansion;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<AnswerFilterInstanceParam> paramValueList = new ArrayList<AnswerFilterInstanceParam>();
    private Map<String, Object> paramValueMap = new LinkedHashMap<String, Object>();

    private RecordClass recordClass;
    private Query filterQuery;
    private AnswerParam answerParam;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the isDefault
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * @param isDefault
     *            the isDefault to set
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return the isBooleanExpansion
     */
    public boolean isBooleanExpansion() {
        return isBooleanExpansion;
    }

    /**
     * @param isBooleanExpansion
     *            the isBooleanExpansion to set
     */
    public void setBooleanExpansion(boolean isBooleanExpansion) {
        this.isBooleanExpansion = isBooleanExpansion;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public void addParamValue(AnswerFilterInstanceParam param) {
        this.paramValueList.add(param);
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClass
     *            the recordClass to set
     */
    void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    /**
     * @return the filterQuery
     */
    public Query getFilterQuery() {
        return filterQuery;
    }

    /**
     * @param filterQuery
     *            the filterQuery to set
     */
    void setFilterQuery(Query filterQuery) {
        this.filterQuery = filterQuery;
    }

    /**
     * @return the answerParam
     */
    public AnswerParam getAnswerParam() {
        return answerParam;
    }

    /**
     * @param answerParam
     *            the answerParam to set
     */
    void setAnswerParam(AnswerParam answerParam) {
        this.answerParam = answerParam;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the descriptions
        for (WdkModelText text : descriptionList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                if (description != null)
                    throw new WdkModelException("Description of "
                            + "answerFilterInstance '" + name + "' in "
                            + recordClass.getFullName()
                            + " is included more than once.");
                this.description = text.getText();
            }
        }
        descriptionList = null;

        // exclude the param values
        for (AnswerFilterInstanceParam param : paramValueList) {
            if (param.include(projectId)) {
                param.excludeResources(projectId);
                String paramName = param.getName();
                String paramValue = param.getText().trim();

                if (paramValueMap.containsKey(paramName))
                    throw new WdkModelException("The param [" + paramName
                            + "] for answerFilterInstance [" + name
                            + "] of type " + recordClass.getFullName()
                            + "  is included more than once.");
                paramValueMap.put(paramName, paramValue);
            }
        }
        paramValueList = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;
        // make sure the params provides match with those in the filter query
        Map<String, Param> params = filterQuery.getParamMap();
        for (String paramName : paramValueMap.keySet()) {
            if (!params.containsKey(paramName))
                throw new WdkModelException("The param [" + paramName
                        + "] declared in answerFilterInstance [" + name
                        + "] of type " + recordClass.getFullName()
                        + " does not exist in the" + " filter query ["
                        + filterQuery.getFullName() + "]");
        }
        resolved = true;
    }

    public QueryInstance makeQueryInstance(Answer answer)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        Map<String, Object> values = new LinkedHashMap<String, Object>(
                paramValueMap);
        values.put(answerParam.getName(),
                answer.getAnswerInfo().getAnswerChecksum());
        return filterQuery.makeInstance(values);
    }
}
