package org.gusdb.wdk.model;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModelException;

public class NestedRecord extends WdkModelBase {

    protected String questionTwoPartName;
    protected Question question;

    //todo:
    //validate links between nested record query and parent record instance

    public void setQuestionRef(String questionTwoPartName) {
        this.questionTwoPartName = questionTwoPartName;
    }
    
    public String getTwoPartName() {
        return questionTwoPartName;
    }

    public Question getQuestion() {
        return this.question;
    }

    void resolveReferences(WdkModel model) throws WdkModelException {
        this.question = (Question) model.resolveReference(questionTwoPartName);
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // do nothing
    }
}
