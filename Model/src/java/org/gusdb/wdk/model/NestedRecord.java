package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;

public class NestedRecord extends WdkModelBase {

    private RecordClass parentRecordClass;
    private String questionTwoPartName;
    private Question question;

    // todo:
    // validate links between nested record query and parent record instance

    public void setQuestionRef(String questionTwoPartName) {
        this.questionTwoPartName = questionTwoPartName;
    }

    public String getTwoPartName() {
        return questionTwoPartName;
    }

    public Question getQuestion() {
        return this.question;
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        this.question = (Question) model.resolveReference(questionTwoPartName);
        question.resolveReferences(model);

        // validate the query
        Query query = question.getQuery();
        query.resolveReferences(model);
        parentRecordClass.validateQuery(query);

        // prepare the query and add primary key params
        query = parentRecordClass.prepareQuery(query);
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
}
