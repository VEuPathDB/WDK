/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.query.Query;

/**
 * @author Jerric Gao
 * 
 */
public class SummaryView extends WdkModelBase {

    private SummaryTable summaryTable;

    private String rowTerm;
    private String columnTerm;
    private String summaryQueryRef;
    private Query summaryQuery;
    private boolean isDefault = false;
    private boolean isBooleanDefault = false;
    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;
    private AnswerParam answerParam;

    /**
     * @return the summaryTable
     */
    public SummaryTable getSummaryTable() {
        return summaryTable;
    }

    /**
     * @param summaryTable
     *            the summaryTable to set
     */
    void setSummaryTable(SummaryTable summaryTable) {
        this.summaryTable = summaryTable;
    }

    /**
     * @return the isDefault
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * @param isDefault
     *            the isDefault to set
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return the isBooleanDefault
     */
    public boolean isBooleanDefault() {
        return this.isBooleanDefault;
    }

    /**
     * @param isBooleanDefault
     *            the isBooleanDefault to set
     */
    public void setBooleanDefault(boolean isBooleanDefault) {
        this.isBooleanDefault = isBooleanDefault;
    }

    /**
     * @return the summaryQuery
     */
    public Query getSummaryQuery() {
        return this.summaryQuery;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param rowTerms
     *            the rowTerms to set
     */
    public void setRowTerm(String rowTerm) {
        this.rowTerm = rowTerm;
    }

    public String getRowTerm() {
        return rowTerm;
    }

    /**
     * @param columnTerms
     *            the columnTerms to set
     */
    public void setColumnTerm(String columnTerm) {
        this.columnTerm = columnTerm;
    }

    public String getColumnTerm() {
        return columnTerm;
    }

    /**
     * @param summaryQueryRef
     *            the summaryQueryRef to set
     */
    public void setSummaryQueryRef(String summaryQueryRef) {
        this.summaryQueryRef = summaryQueryRef;
    }

    public void addDescription(WdkModelText description) {
        descriptionList.add(description);
    }

    /**
     * @return the answerParam
     */
    public AnswerParam getAnswerParam() {
        return answerParam;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        for (WdkModelText desc : descriptionList) {
            if (desc.include(projectId)) {
                desc.excludeResources(projectId);
                this.description = desc.getText();
                break;
            }
        }
        descriptionList = null;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        summaryQuery = (Query) wdkModel.resolveReference(summaryQueryRef);
        Param columnParam = summaryTable.getColumnParam();
        Param rowParam = summaryTable.getRowParam();

        // the summary query should have three params: one AnswerParam, one
        // "column" StringParam, one "row" StringParam
        Map<String, Param> params = summaryQuery.getParamMap();
        if (params.size() != 3)
            throw new WdkModelException("Summary view query " + summaryQueryRef
                    + " must have exactly 3 params, one of which is an "
                    + "AnswerParam, one FlatVocab/EnumParam named "
                    + columnParam.getFullName() + ", and one "
                    + "FlatVocab/EnumParam named " + rowParam.getFullName());
        if (!params.containsKey(columnParam.getName())
                || (params.get(columnParam.getName()) instanceof StringParam))
            throw new WdkModelException("Summary view query " + summaryQueryRef
                    + " must have a FlatVocab/EnumParam named "
                    + columnParam.getFullName());
        if (!params.containsKey(rowParam.getName())
                || (params.get(rowParam.getName()) instanceof StringParam))
            throw new WdkModelException("Summary view query " + summaryQueryRef
                    + " must have a FlatVocab/EnumParam named "
                    + rowParam.getFullName());

        params.remove(columnParam.getName());
        params.remove(rowParam.getName());
        Param param = params.values().iterator().next();
        if (!(param instanceof AnswerParam))
            throw new WdkModelException("Summary view query " + summaryQueryRef
                    + " must have a AnswerParam");
        AnswerParam answerParam = (AnswerParam) param;

        // check if the answer param matches the parent record class type
        RecordClass recordClass = summaryTable.getRecordClass();
        if (!answerParam.getRecordClass().getFullName().equals(
                recordClass.getFullName()))
            throw new WdkModelException("The answerParam in summary view "
                    + "query " + summaryQuery.getFullName() + " should be of "
                    + "type " + recordClass.getFullName() + ", but it is "
                    + answerParam.getRecordClassRef());

        // verify the summary query to make sure it returns all primary key
        // columns
        Map<String, Column> columns = summaryQuery.getColumnMap();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        for (String pkColumn : pkColumns) {
            if (!columns.containsKey(pkColumn))
                throw new WdkModelException("The summary query "
                        + summaryQueryRef + " does not return the required "
                        + "primary key column " + pkColumn);
        }
        this.answerParam = answerParam;
    }
}
