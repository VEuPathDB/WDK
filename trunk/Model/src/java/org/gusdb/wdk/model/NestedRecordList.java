package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;

public class NestedRecordList extends WdkModelBase {

    private RecordClass parentRecordClass;
    private String questionTwoPartName;
    private Question question;

    // todo:
    // validate links between nested record query and parent record instance

    public NestedRecordList() {

    }

    public void setQuestionRef(String questionTwoPartName) {
        this.questionTwoPartName = questionTwoPartName;
    }

    public String getTwoPartName() {
        return questionTwoPartName;
    }

    public Question getQuestion() {
        return this.question;
    }

    /**
     * @return the parentRecordClass
     */
    public RecordClass getParentRecordClass() {
        return parentRecordClass;
    }

    /**
     * @param parentRecordClass
     *            the parentRecordClass to set
     */
    public void setParentRecordClass(RecordClass parentRecordClass) {
        this.parentRecordClass = parentRecordClass;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        this.question = (Question) wdkModel.resolveReference(questionTwoPartName);
        question.resolveReferences(wdkModel);

        // validate the query
        Query query = question.getQuery();
        query.resolveReferences(wdkModel);
        parentRecordClass.validateQuery(query);
        String[] paramNames = parentRecordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // prepare the query and add primary key params
        query = RecordClass.prepareQuery(wdkModel, query, paramNames);
        question.setQuery(query);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
    // do nothing
    }
}
